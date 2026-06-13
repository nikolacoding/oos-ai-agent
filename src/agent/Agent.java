package agent;

import java.util.*;
import java.io.*;

import utility.Constants;
import main.Simulation;
import agent.operations.*;
import vfs.VFSFile;
import agent.conflict.AccessDecision;

public class Agent extends Thread {
    private static int numInstances = 0;

    private final int agentId;
    private final int agentPriority;
    private final int agentArrival;
    // (prefiksovani sa 'agent' zbog konflikata sa Thread klasom)

    private Queue<Operation> operations = new LinkedList<>();
    private final Slots slotsRef;
    private final Simulation simulationRef;

    // pracenje otvorenih fajlova za svakog individualnog agenta jer mogu da dijele razlicite fajlove sa razlicitim rezimima i alijasima
    private static final class OpenFile {
        private final VFSFile file;
        private final String mode;

        private OpenFile(VFSFile file, String mode) {
            this.file = file;
            this.mode = mode;
        }
    }

    private final Map<String, OpenFile> openedFiles = new HashMap<>();

    public Agent(int priority, int arrival, Slots slotsRef, Simulation simulationRef, String preset) {
        this.agentId = ++numInstances;
        this.agentPriority = priority;
        this.agentArrival = arrival;
        this.slotsRef = slotsRef;
        this.simulationRef = simulationRef;
        this.operations = OperationsParser.parseOperations(new File(String.format("%s%s/%d.txt", Constants.AGENT_OPERATIONS_ROOT, preset,  agentId)));
    }

    public int getAgentId() { return agentId; }
    public int getAgentPriority() { return agentPriority; }
    public int getAgentArrival() { return agentArrival; }

    @Override
    public void run(){
        try {
            Thread.sleep(agentArrival * 1000L);
            this.simulationRef.registerAgent(this);
            this.simulationRef.markAgentStarted(this.agentId);
            System.out.printf("[%d] Agent %d (A%d) je stigao [prioritet -- %d]\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, agentId, agentPriority);
            this.slotsRef.add(this);

            while (!operations.isEmpty()) {
                Operation operation = operations.poll();
                this.runOperation(operation);
                Thread.sleep(Constants.OPERATION_DELAY); // pretpostavlja se da mora proci bar jedna jedinica vremena (1s) izmedju operacija kao na ispitnim primjerima
            }

            this.simulationRef.markAgentFinished(this.agentId);
            System.out.printf("[%d] A%d zavrsava sa radom.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId);

        } catch (InterruptedException _) { }
    }

    private void runOperation(Operation operation) {
        switch (operation.getType()) {
            case "THINK" -> {
                int duration = Integer.parseInt(operation.getArgs().getFirst());
                this.think(duration);
            }
            case "OPEN" -> {
                String name =  operation.getArgs().getFirst();
                String alias = operation.getArgs().get(3);
                String mode = operation.getArgs().get(1);
                this.open(name, alias, mode);
            }
            case "READ" -> {
                String alias = operation.getArgs().getFirst();
                this.read(alias);
            }
            case "WRITE" -> {
                String alias = operation.getArgs().getFirst();
                String text = joinArgs(operation.getArgs(), 1);
                this.write(alias, text);
            }
            case "APPEND" -> {
                String alias = operation.getArgs().getFirst();
                String text = joinArgs(operation.getArgs(), 1);
                this.append(alias, text);
            }
            case "CLOSE" -> {
                String alias  = operation.getArgs().getFirst();
                this.close(alias);
            }
        }
    }

    public void think(int duration){
        System.out.printf("[%d] Agent A%s razmislja... (%dsec)\n", this.simulationRef.getCurrentSimulationRuntime(), this.agentId, duration);
        try {
            Thread.sleep(duration * 1000L);
        } catch (InterruptedException _) { }
    }

    public void open(String path, String alias, String mode){
        VFSFile file = this.simulationRef.resolveFile(path);
        if (file == null) {
            System.out.printf("[%d] Agent A%s pokusava otvoriti nepostojeci fajl '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, path);
            return;
        }

        if (!file.canOpen(mode)) {
            System.out.printf("[%d] Agent A%s pokusava otvoriti fajl '%s' u neispravnom rezimu %s.\n",
                    this.simulationRef.getCurrentSimulationRuntime(), agentId, path, mode.toUpperCase());
            return;
        }

        long requestStart = System.currentTimeMillis();
        AccessDecision decision = this.simulationRef.requestAccess(file, this, path, mode);
        long waitedMs = System.currentTimeMillis() - requestStart;
        if (!decision.isGranted()) {
            if (decision.isInterrupted()) {
                System.out.printf("[%d] Agent A%d prekida cekanje na '%s'.\n",
                        this.simulationRef.getCurrentSimulationRuntime(), agentId, path);
            } else {
                this.simulationRef.recordDeniedLock(this.agentId, path, decision.getCycleDescription());
                System.out.printf("[%d] A%d nije dobio zakljucavanje nad %s zbog ciklusa %s\n",
                        this.simulationRef.getCurrentSimulationRuntime(), agentId, path, decision.getCycleDescription());
            }
            return;
        }

        if (decision.isWaited()) {
            this.simulationRef.recordLockWait(this.agentId, waitedMs);
        }

        if (decision.isWaited()) {
            System.out.printf("[%d] Agent A%d otvara '%s' u rezimu %s pod alijasom '%s' <- zakljucano.\n",
                    this.simulationRef.getCurrentSimulationRuntime(), agentId, path, mode.toUpperCase(), alias);
        } else {
            System.out.printf("[%d] Agent A%s otvara '%s' u rezimu %s pod alijasom '%s' <- zakljucano.\n",
                    this.simulationRef.getCurrentSimulationRuntime(), agentId, path, mode.toUpperCase(), alias);
        }
        this.openedFiles.put(alias, new OpenFile(file, mode));
    }

    public void read(String alias){
        final OpenFile openedFile = this.openedFiles.get(alias);
        if (openedFile != null) {
            final VFSFile file = openedFile.file;
            Integer owner = this.simulationRef.getOwnerId(file);
            if (owner == null || owner != this.agentId) {
                System.out.printf("[%d] A%d nema pristup '%s' jer ga je preuzeo A%d.\n", this.simulationRef.getCurrentSimulationRuntime(), this.agentId, alias, owner == null ? -1 : owner);
                this.openedFiles.remove(alias);
                return;
            }
            final String content = file.getContentFormatted("\t");
            System.out.printf("[%d] A%s iscitava '%s': \n<%s>\n%s</%s>\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias, alias, content, alias);
        }
        else {
            System.out.printf("[%d] A%s pokusava iscitati nepostojeci fajl '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
        }
    }

    public void write(String alias, String text) {
        final OpenFile openedFile = this.openedFiles.get(alias);
        if (openedFile == null) {
            System.out.printf("[%d] A%s pokusava pisati u nepostojeci fajl '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
            return;
        }

        Integer owner = this.simulationRef.getOwnerId(openedFile.file);
        if (owner == null || owner != this.agentId) {
            System.out.printf("[%d] A%d nema pristup '%s' za pisanje jer ga je preuzeo A%d.\n", this.simulationRef.getCurrentSimulationRuntime(), this.agentId, alias, owner == null ? -1 : owner);
            this.openedFiles.remove(alias);
            return;
        }

        if (openedFile.mode.equalsIgnoreCase("read")) {
            System.out.printf("[%d] A%s pokusava pisati u fajl '%s' koji nije otvoren u write rezimu.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
            return;
        }

        openedFile.file.writeContent(text);
        System.out.printf("[%d] A%s prepisuje '%s' sa: %s\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias, text);
    }

    public void append(String alias, String text) {
        final OpenFile openedFile = this.openedFiles.get(alias);
        if (openedFile == null) {
            System.out.printf("[%d] A%s pokusava dodavati u nepostojeci fajl '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
            return;
        }

        Integer owner = this.simulationRef.getOwnerId(openedFile.file); // wrapper zbog null provjere
        if (owner == null || owner != this.agentId) {
            System.out.printf("[%d] A%d nema pristup '%s' za dodavanje jer ga je preuzeo A%d.\n", this.simulationRef.getCurrentSimulationRuntime(), this.agentId, alias, owner == null ? -1 : owner);
            this.openedFiles.remove(alias);
            return;
        }

        if (openedFile.mode.equalsIgnoreCase("read")) {
            System.out.printf("[%d] A%s pokusava dodavati u fajl '%s' koji nije otvoren u append rezimu.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
            return;
        }

        openedFile.file.appendContent(text);
        System.out.printf("[%d] A%s dodaje u '%s': %s\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias, text);
    }

    public void close(String alias){
        final OpenFile openedFile = this.openedFiles.remove(alias);
        if (openedFile == null) {
            System.out.printf("[%d] A%s pokusava zatvoriti nepostojeci fajl '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
            return;
        }

        this.simulationRef.releaseAccess(openedFile.file, this.agentId);
        System.out.printf("[%d] A%s zatvara '%s'.\n", this.simulationRef.getCurrentSimulationRuntime(), agentId, alias);
    }

    private static String joinArgs(List<String> args, int fromIndex) {
        if (fromIndex >= args.size()) {
            return "";
        }

        return String.join(" ", args.subList(fromIndex, args.size()));
    }

    public synchronized void notifyPreempted(vfs.VFSFile file, int byAgentId) {
        List<String> removed = new ArrayList<>();
        Iterator<Map.Entry<String, OpenFile>> it = this.openedFiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OpenFile> e = it.next();
            if (e.getValue().file == file) {
                removed.add(e.getKey());
                it.remove();
            }
        }
        if (!removed.isEmpty()) {
            System.out.printf("[%d] A%d izgubio pristup '%s' jer ga je preuzeo A%d.\n",
                    this.simulationRef.getCurrentSimulationRuntime(), this.agentId, String.join(",", removed), byAgentId);
        }
    }
}

