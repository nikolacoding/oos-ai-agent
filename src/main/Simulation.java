package main;

import com.fasterxml.jackson.databind.*;

import agent.Agent;
import agent.Slots;
import utility.Utility;
import vfs.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    private final long startTime = System.currentTimeMillis();

    public Simulation(String settingsPath, String preset) {
        this.preset = preset;
        this.settingsPath = settingsPath;
    }

    public List<VFS> getMounts() { return this.mounts; }
    private long getStartTime(){ return this.startTime; }
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
        System.out.println("== Simulacija je pocela ==");
        for (int i = 0; i < maxRunningAgents; i++) {
            Agent agent = new Agent(Utility.random.nextInt(3), i + Utility.random.nextInt(5) * i, this.slots, this, this.preset);
            this.agents.add(agent);
        }

        this.agents.forEach(Agent::start);
        this.agents.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException _) { }
        });

        System.out.println("== Simulacija je zavrsila ==");
        this.displayStats();
    }

    public boolean isFileInMounts(String name) {
        return this.resolveFile(name) != null;
    }

    public synchronized void registerAgent(Agent agent) {
        this.agentStats.put(agent.getAgentId(), new AgentStats(agent.getAgentId(), agent.getAgentPriority(), agent.getAgentArrival()));
    }

    public synchronized void markAgentStarted(int agentId) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.startedAtMs = System.currentTimeMillis();
            return stats;
        });
    }

    public synchronized void markAgentFinished(int agentId) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.finishedAtMs = System.currentTimeMillis();
            return stats;
        });
    }

    public synchronized void recordLockWait(int agentId, long waitedMs) {
        if (waitedMs <= 0) {
            return;
        }

        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.waitedMs += waitedMs;
            return stats;
        });
    }

    public synchronized void recordDeniedLock(int agentId, String path, String cycleDescription) {
        this.agentStats.computeIfPresent(agentId, (id, stats) -> {
            stats.blockedDenials++;
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

        // PREEMPTIVE: if preset allows and requester has better (lower) priority, take over immediately
        if ("preemptive".equalsIgnoreCase(this.preset)) {
            Agent ownerAgent = null;
            for (Agent a : this.agents) {
                if (a.getAgentId() == ownerId) { ownerAgent = a; break; }
            }
            int ownerPriority = ownerAgent == null ? Integer.MAX_VALUE : ownerAgent.getAgentPriority();
            if (agent.getAgentPriority() < ownerPriority) {
                // transfer ownership to requester
                this.fileOwners.put(file, requesterId);
                // record preemption stat
                this.agentStats.computeIfPresent(requesterId, (id, stats) -> { stats.preemptions++; return stats; });
                System.out.printf("[%d] Agent A%d preuzima '%s' od A%d <- zakljucano.\n",
                        this.getCurrentSimulationRuntime(), requesterId, path, ownerId);
                // notify displaced owner (so it can drop aliases)
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
            } catch (InterruptedException e) {
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

        while (current != null && visited.add(current)) {
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
            if (root == null) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) je prazan.", this.settingsPath));
            }

            JsonNode settingsNode = root.path("settings");
            JsonNode maxRunningAgentsNode = settingsNode.path("max_running_agents");
            if (settingsNode.isMissingNode() || !settingsNode.isObject() || maxRunningAgentsNode.isMissingNode() || !maxRunningAgentsNode.canConvertToInt()) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) nema validan settings.max_running_agents zapis.", this.settingsPath));
            }

            this.maxRunningAgents = maxRunningAgentsNode.asInt();
            this.slots = new Slots(this.maxRunningAgents);

            JsonNode vfsNode = root.path("vfs");
            JsonNode mountsNode = vfsNode.path("mounts");
            if (vfsNode.isMissingNode() || !vfsNode.isObject() || mountsNode.isMissingNode() || !mountsNode.isArray()) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) nema validan vfs.mounts zapis.", this.settingsPath));
            }

            this.mounts.clear();
            for (JsonNode mountNode : mountsNode) {
                JsonNode pathNode = mountNode.path("path");
                JsonNode modeNode = mountNode.path("mode");

                if (pathNode.isMissingNode() || modeNode.isMissingNode() || !pathNode.isTextual() || !modeNode.isTextual()) {
                    throw new IllegalStateException(String.format("Navedeni settings fajl (%s) sadrzi neispravan mount zapis.", this.settingsPath));
                }

                var newMount = new VFS(pathNode.asText(), modeNode.asText());
                this.mounts.add(newMount);
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Navedeni settings fajl (%s) se ne moze procitati.", this.settingsPath), e);
        }
    }

    public long getCurrentSimulationRuntime(){
        return (System.currentTimeMillis() - this.getStartTime()) / 1000L;
    }

    private void displayStats() {
        System.out.println("=== Gantova karta ===");
        for (int i = 0; i < this.slots.size(); i++) {
            Agent agent = this.slots.get(i);
            AgentStats stats = this.agentStats.get(agent.getAgentId());
            long start = stats.startedAtMs == 0 ? agent.getAgentArrival() * 1000L : stats.startedAtMs - this.startTime;
            long end = stats.finishedAtMs == 0 ? getCurrentSimulationRuntime() * 1000L : stats.finishedAtMs - this.startTime;
            System.out.printf("slot_%d: [%d,%d] A%d%n", i + 1, start / 1000L, end / 1000L, agent.getAgentId());
        }

        System.out.println("=== Zavrsno stanje agenata ===");
        System.out.println("Agent\tStatus\tDolazak\tPocetak\t\tKraj\tCekanje\tBlokiran\tPreuzimanja");
        for (AgentStats stats : this.agentStats.values()) {
            long start = stats.startedAtMs == 0 ? 0 : (stats.startedAtMs - this.startTime) / 1000L;
            long end = stats.finishedAtMs == 0 ? 0 : (stats.finishedAtMs - this.startTime) / 1000L;
            System.out.printf("A%d\t\t%s\t\t%d\t\t%d\t\t%d\t\t%.2f\t\t%d\t\t%d%n",
                    stats.agentId,
                    stats.finishedAtMs > 0 ? "zavrsen" : "u radu",
                    stats.arrival,
                    start,
                    end,
                    stats.waitedMs / 1000.0,
                    stats.blockedDenials,
                    stats.preemptions);
        }

        if (!this.deniedLockEvents.isEmpty()) {
            System.out.println("=== Odbijena zakljucavanja ===");
            this.deniedLockEvents.forEach(System.out::println);
        }

        System.out.println("=== Zavrsno stanje VFS-a ===");
        for (VFS mount : this.mounts) {
            for (VFSFile file : mount.getFiles()) {
                System.out.printf("%s:\n %s \n", file.getName(), file.getContentFormatted("\t"));
            }
        }

        double avgWaiting = this.agentStats.values().stream().mapToDouble(s -> s.waitedMs / 1000.0).average().orElse(0.0);
        double avgBlocking = this.agentStats.values().stream().mapToDouble(s -> s.blockedDenials).average().orElse(0.0);

        System.out.println("=== Statistika ===");
        System.out.printf("Broj sprijecenih zastoja: %d%n", this.deniedLockEvents.size());
        System.out.printf("Prosjecno vrijeme cekanja: %.2f%n", avgWaiting);
        System.out.printf("Prosjecno vrijeme blokiranja: %.2f%n", avgBlocking);
    }


}
