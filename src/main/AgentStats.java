package main;

// holder klasa za statistiku pojedinacnih agenata
public class AgentStats {
    final int agentId;
    final int priority;
    final int arrival;
    long startedAtMs;
    long finishedAtMs;
    long waitedMs;
    int blockedDenials;
    int preemptions;

    public AgentStats(int agentId, int priority, int arrival) {
        this.agentId = agentId;
        this.priority = priority;
        this.arrival = arrival;
    }
}
