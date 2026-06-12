package main;

// holder klasa za pracenje odluka o preuzimanju odnosno blokiranju
public class AccessDecision {
    private final boolean granted;
    private final boolean waited;
    private final int blockingAgentId;
    private final boolean interrupted;
    private final String cycleDescription;

    private AccessDecision(boolean granted, boolean waited, int blockingAgentId, boolean interrupted, String cycleDescription) {
        this.granted = granted;
        this.waited = waited;
        this.blockingAgentId = blockingAgentId;
        this.interrupted = interrupted;
        this.cycleDescription = cycleDescription;
    }

    public static AccessDecision granted(boolean waited, int blockingAgentId) {
        return new AccessDecision(true, waited, blockingAgentId, false, null);
    }

    public static AccessDecision denied(int blockingAgentId, String cycleDescription) {
        return new AccessDecision(false, false, blockingAgentId, false, cycleDescription);
    }

    public static AccessDecision interrupted(int blockingAgentId) {
        return new AccessDecision(false, false, blockingAgentId, true, null);
    }

    public boolean isGranted() { return granted; }
    public boolean isWaited() { return waited; }
    public int getBlockingAgentId() { return blockingAgentId; }
    public boolean isInterrupted() { return interrupted; }
    public String getCycleDescription() { return cycleDescription; }
}
