package agent;

// holder klasa za statistiku pojedinacnih agenata
public class AgentStats {
    private final int agentId;
    private final int priority;
    private final int arrival;
    private long startedAtMs;
    private long finishedAtMs;
    private long waitedMs;
    private int blockedDenials;
    private int preemptions;

    public AgentStats(int agentId, int priority, int arrival) {
        this.agentId = agentId;
        this.priority = priority;
        this.arrival = arrival;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getArrival() {
        return this.arrival;
    }

    public long getStartedAtMs() {
        return this.startedAtMs;
    }

    public void setStartedAtMs(long startedAtMs) {
        this.startedAtMs = startedAtMs;
    }

    public long getFinishedAtMs() {
        return this.finishedAtMs;
    }

    public void setFinishedAtMs(long finishedAtMs) {
        this.finishedAtMs = finishedAtMs;
    }

    public long getWaitedMs() {
        return this.waitedMs;
    }

    public void setWaitedMs(long waitedMs) {
        this.waitedMs = waitedMs;
    }

    public int getBlockedDenials() {
        return this.blockedDenials;
    }

    public void setBlockedDenials(int blockedDenials) {
        this.blockedDenials = blockedDenials;
    }

    public int getPreemptions() {
        return this.preemptions;
    }

    public void setPreemptions(int preemptions) {
        this.preemptions = preemptions;
    }
}
