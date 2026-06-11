package agent;

import agent.Slots;
import java.util.*;

public class Agent extends Thread {
    private static int numInstances = 0;

    private final int agentId;
    private final int agentPriority;
    private final int agentArrival;
    private final Slots slotsRef;
    // (prefiksovani sa 'agent' zbog konflikata sa Thread klasom)

    public Agent(int priority, int arrival, Slots slotsRef) {
        this.agentId = ++numInstances;
        this.agentPriority = priority;
        this.agentArrival = arrival;
        this.slotsRef = slotsRef;
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

        } catch (InterruptedException e) {

        }
    }

    public void think(int durationMs){
        int current = 0;
        while (current++ < durationMs) {
            // razmisljanje
        }
    }

    public void open(String path, String alias, String mode){
        // otvaranje
    }

    public void read(String alias){
        // citanje
    }
}
