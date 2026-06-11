package agent;

import agent.Slots;
import agent.operations.*;
import java.util.*;
import java.io.*;

public class Agent extends Thread {
    private static int numInstances = 0;

    private final int agentId;
    private final int agentPriority;
    private final int agentArrival;
    private Queue<Operation> operations = new LinkedList<>();

    private final Slots slotsRef;
    // (prefiksovani sa 'agent' zbog konflikata sa Thread klasom)

    public Agent(int priority, int arrival, Slots slotsRef) {
        this.agentId = ++numInstances;
        this.agentPriority = priority;
        this.agentArrival = arrival;
        this.slotsRef = slotsRef;
        this.operations = OperationsParser.parseOperations(new File(String.format("agent_operations/%d.txt", agentId)));
    }

    public int getAgentId() { return agentId; }
    public int getAgentPriority() { return agentPriority; }
    public int getAgentArrival() { return agentArrival; }

    @Override
    public void run(){
        try {
            Thread.sleep(agentArrival * 1000L);
            System.out.printf("Agent %d (A%d) je stigao [prioritet -- %d]%n", agentId, agentId, agentPriority);
            this.slotsRef.add(this);

            while (!operations.isEmpty()) {
                Operation operation = operations.poll();
                this.runOperation(operation);
            }

            System.out.printf("A%d nema vise operacija i implicitno zavrsava sa radom.\n", agentId);

        } catch (InterruptedException e) { }
    }

    private void runOperation(Operation operation) {
        switch (operation.getType()) {
            case "THINK":
                int duration = Integer.parseInt(operation.getArgs().getFirst());
                System.out.printf("Agent A%s razmislja... (%dsec)\n", agentId, duration);
                this.think(duration);
                break;
            case "OPEN":
                this.open(operation.getArgs().get(0), operation.getArgs().get(2), operation.getArgs().get(1));
                break;
            case "READ":
                this.read(operation.getArgs().getFirst());
                break;
        }
    }

    public void think(int duration){
        try {
            Thread.sleep(duration * 1000L);
        } catch (InterruptedException e) {

        }
    }

    public void open(String path, String alias, String mode){
        // otvaranje
    }

    public void read(String alias){
        // citanje
    }
}
