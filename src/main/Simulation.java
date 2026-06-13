package main;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.*;

import agent.AgentStats;
import agent.conflict.AccessDecision;
import agent.Agent;
import agent.Slots;
import exceptions.MountingExceptions;
import utility.Utility;
import vfs.*;

public class Simulation extends Thread {

    private final String preset;
    private final String settingsPath;

    private Slots slots;
    private int maxRunningAgents;
    private final List<VFS> mounts = new ArrayList<>();
    private final List<Agent> agents = new ArrayList<>();
    private final Map<VFSFile, Integer> fileOwners = new HashMap<>();
    private final Map<Integer, Integer> waitFor = new HashMap<>();
    private final Map<Integer, AgentStats> agentStats = new HashMap<>();
    private final List<String> deniedLockEvents = new ArrayList<>();

    private final long startTime;

    public Simulation(String settingsPath, String preset) {
        this.preset = preset;
        this.settingsPath = settingsPath;
        this.startTime = System.currentTimeMillis();

        System.out.printf("> Pripremljena simulacija sa presetom '%s'.\n", preset);
    }

    private long getStartTime(){ return this.startTime; }
    public Slots getSlots(){ return this.slots; }

    public VFSFile resolveFile(String path) {
        String fileName = new File(path).getName();

        for (VFS mount : this.mounts) {
            VFSFile file = mount.findFile(fileName);
            if (file != null) {
                return file;
            }
        }

        return null;
    }

    public synchronized Integer getOwnerId(VFSFile file) { return this.fileOwners.get(file); }

    @Override
    public void run() {
        this.parseSettings();
        System.out.println("=== Simulacija je pocela ===");
        for (int i = 0; i < maxRunningAgents; i++) {
            Agent agent = new Agent(Utility.random.nextInt(4), i + Utility.random.nextInt(5) * i, this.slots, this, this.preset);
            this.agents.add(agent);
        }

        this.agents.forEach(Agent::start);
        this.agents.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException _) { }
        });

        System.out.println("=== Simulacija je zavrsila ===");
        this.displayStats();
    }

    public synchronized void registerAgent(Agent agent) {
        this.agentStats.put(agent.getAgentId(), new AgentStats(agent.getAgentId(), agent.getAgentPriority(), agent.getAgentArrival()));
    }

    public synchronized void markAgentStarted(int agentId) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.setStartedAtMs(stats.getArrival());
            return stats;
        });
    }

    public synchronized void markAgentFinished(int agentId) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.setFinishedAtMs(System.currentTimeMillis());
            return stats;
        });
    }

    public synchronized void recordLockWait(int agentId, long waitedMs) {
        if (waitedMs <= 0) {
            return;
        }

        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.setWaitedMs(stats.getWaitedMs() + waitedMs);
            return stats;
        });
    }

    public synchronized void recordDeniedLock(int agentId, String path, String cycleDescription) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.setBlockedDenials(stats.getBlockedDenials() + 1);
            return stats;
        });
        this.deniedLockEvents.add(String.format("[%d] A%d nije dobio zakljucavanje nad %s zbog ciklusa %s",
                this.getCurrentSimulationRuntime(), agentId, path, cycleDescription));
    }

    public synchronized AccessDecision requestAccess(VFSFile file, Agent agent, String path, String mode) {
        int requesterId = agent.getAgentId();

        Integer ownerId = this.fileOwners.get(file);
        if (ownerId == null) {
            this.fileOwners.put(file, requesterId);
            return AccessDecision.granted(false, -1);
        }

        if (ownerId == requesterId) {
            return AccessDecision.granted(false, ownerId);
        }

        // preuzimanje u slucaju viseg prioriteta
        // TODO: edge case sa istim prioritetom
        if ("preemptive".equalsIgnoreCase(this.preset)) {
            Agent ownerAgent = null;
            for (Agent a : this.agents) {
                if (a.getAgentId() == ownerId) { ownerAgent = a; break; }
            }
            int ownerPriority = ownerAgent == null ? Integer.MAX_VALUE : ownerAgent.getAgentPriority();
            if (agent.getAgentPriority() < ownerPriority) {
                this.fileOwners.put(file, requesterId);
                this.agentStats.computeIfPresent(requesterId, (id, stats) -> { stats.setPreemptions(stats.getPreemptions() + 1); return stats; });
                System.out.printf("[%d] Agent A%d preuzima '%s' od A%d <- zakljucano.\n",
                        this.getCurrentSimulationRuntime(), requesterId, path, ownerId);

                if (ownerAgent != null) {
                    ownerAgent.notifyPreempted(file, requesterId);
                }
                return AccessDecision.granted(false, -1);
            }
        }

        while (true) {
            this.waitFor.put(requesterId, ownerId);
            String cycleDescription = buildCycleDescription(requesterId);
            if (cycleDescription != null) {
                this.waitFor.remove(requesterId);
                return AccessDecision.denied(ownerId, cycleDescription);
            }

            System.out.printf("[%d] Agent A%d ceka na '%s' jer ga koristi A%d.\n",
                    this.getCurrentSimulationRuntime(), requesterId, path, ownerId);

            try {
                wait();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                this.waitFor.remove(requesterId);
                return AccessDecision.interrupted(ownerId);
            }

            ownerId = this.fileOwners.get(file);
            if (ownerId == null) {
                this.fileOwners.put(file, requesterId);
                this.waitFor.remove(requesterId);
                return AccessDecision.granted(true, -1);
            }

            if (ownerId == requesterId) {
                this.waitFor.remove(requesterId);
                return AccessDecision.granted(true, ownerId);
            }
        }
    }

    public synchronized void releaseAccess(VFSFile file, int agentId) {
        Integer ownerId = this.fileOwners.get(file);
        if (ownerId != null && ownerId == agentId) {
            this.fileOwners.remove(file);
            notifyAll();
        }
    }

    private String buildCycleDescription(int startAgentId) {
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Integer current = startAgentId;

        while (visited.add(current)) {
            path.add(current);
            Integer next = this.waitFor.get(current);
            if (next == null) {
                return null;
            }
            if (next == startAgentId) {
                path.add(next);
                return path.stream().map(id -> "A" + id).reduce((a, b) -> a + " -> " + b).orElse("A" + startAgentId);
            }
            current = next;
        }

        return null;
    }

    private void parseSettings() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(new File(this.settingsPath));

            if (root == null)
                throw new MountingExceptions.EmptySettingsFileException(this.settingsPath);

            JsonNode settingsNode = root.path("settings");
            JsonNode maxRunningAgentsNode = settingsNode.path("max_running_agents");

            if (settingsNode.isMissingNode() || !settingsNode.isObject() || maxRunningAgentsNode.isMissingNode() || !maxRunningAgentsNode.canConvertToInt())
                throw new MountingExceptions.IllegalMaxRunningAgentsException(this.settingsPath);

            this.maxRunningAgents = maxRunningAgentsNode.asInt();
            this.slots = new Slots(this.maxRunningAgents);

            JsonNode vfsNode = root.path("vfs");
            JsonNode mountsNode = vfsNode.path("mounts");

            if (vfsNode.isMissingNode() || !vfsNode.isObject() || mountsNode.isMissingNode() || !mountsNode.isArray())
                throw new MountingExceptions.IllegalMountsException(this.settingsPath);

            this.mounts.clear();
            for (JsonNode mountNode : mountsNode) {
                JsonNode pathNode = mountNode.path("path");
                JsonNode modeNode = mountNode.path("mode");

                if (pathNode.isMissingNode() || modeNode.isMissingNode() || !pathNode.isTextual() || !modeNode.isTextual())
                    throw new MountingExceptions.IllegalMountStructureException(this.settingsPath);

                var newMount = new VFS(pathNode.asText());
                this.mounts.add(newMount);
            }
        } catch (IOException _) {
            throw new MountingExceptions.UnreadableSettingsFileException("Navedeni settings fajl (%s) se ne moze procitati.");
        }
    }

    public long getCurrentSimulationRuntime(){
        return (System.currentTimeMillis() - this.getStartTime()) / 1000L;
    }

    public Map<Integer, AgentStats> getAgentStats() { return this.agentStats; }

    private void displayStats() {
        System.out.println("\n=== Gantova karta ===");
        for (int i = 0; i < this.slots.size(); i++) {
            Agent agent = this.slots.get(i);
            AgentStats stats = this.agentStats.get(agent.getAgentId());
            long start = stats.getStartedAtMs() == 0 ? agent.getAgentArrival() * 1000L : stats.getStartedAtMs() - this.startTime;
            long end = stats.getFinishedAtMs() == 0 ? getCurrentSimulationRuntime() * 1000L : stats.getFinishedAtMs() - this.startTime;
            System.out.printf("slot_%d: [%d,%d] A%d\n", i + 1, start / 1000L, end / 1000L, agent.getAgentId());
        }
        System.out.println();

        System.out.println("=== Zavrsno stanje agenata ===");
        System.out.println("Agent\tStatus\t\tDolazak\t\tPocetak\t\tKraj\tCekanje\t\tBlokiran\tPreuzimanja");
        for (AgentStats stats : this.agentStats.values()) {
            long start = stats.getArrival();
            long end = stats.getFinishedAtMs() == 0 ? 0 : (stats.getFinishedAtMs() - this.startTime) / 1000L;

            // upitno je da li ova indentacija radi pravilno na svim sistemima i nacinima pokretanja
            System.out.printf("A%d\t\t%s\t\t%d\t\t\t%d\t\t\t%d\t\t%.2f\t\t%d\t\t\t%d\n",
                    stats.getAgentId(),
                    stats.getFinishedAtMs() > 0 ? "zavrsen" : "u radu",
                    stats.getArrival(),
                    start,
                    end,
                    stats.getWaitedMs() / 1000.0,
                    stats.getBlockedDenials(),
                    stats.getPreemptions());
        }
        System.out.println();

        if (!this.deniedLockEvents.isEmpty()) {
            System.out.println("=== Odbijena zakljucavanja/preuzimanja ===");
            this.deniedLockEvents.forEach(System.out::println);
        }

        System.out.println("=== Zavrsno stanje VFS-a ===");
        for (VFS mount : this.mounts) {
            for (VFSFile file : mount.getFiles()) {
                System.out.printf("%s:\n %s \n", file.getName(), file.getContentFormatted("\t"));
            }
        }

        double avgWaiting = this.agentStats.values().stream().mapToDouble(s -> s.getWaitedMs() / 1000.0).average().orElse(0.0);
        double avgBlocking = this.agentStats.values().stream().mapToDouble(AgentStats::getBlockedDenials).average().orElse(0.0);

        System.out.println("=== Statistika konflikta ===");
        System.out.printf("Broj sprijecenih zastoja: %d\n", this.deniedLockEvents.size());
        System.out.printf("Prosjecno vrijeme cekanja: %.2f\n", avgWaiting);
        System.out.printf("Prosjecno vrijeme blokiranja: %.2f\n", avgBlocking);
    }
}
