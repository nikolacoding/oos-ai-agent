package agent;

import java.util.*;

public final class Slots extends ArrayList<Agent> {

    private final int maxCapacity;

    public Slots(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean add(Agent agent) {
        if (this.size() < maxCapacity) {
            System.out.printf("slot[%d] <- A%d\n", this.size(), agent.getAgentId());
            return super.add(agent);
        } else {
            return false;
        }
    }
}
