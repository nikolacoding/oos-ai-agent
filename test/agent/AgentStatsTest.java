package agent;

import org.junit.jupiter.api.Test;
import utility.Utility;

import static org.junit.jupiter.api.Assertions.*;

class AgentStatsTest {

    @Test
    void getAgentId() {
        var as = new AgentStats(1, 2, 3);
        assertEquals(1, as.getAgentId());
    }

    @Test
    void getPriority() {
        var as = new AgentStats(1, 2, 3);
        assertEquals(2, as.getPriority());
    }

    @Test
    void getArrival() {
        var as = new AgentStats(1, 2, 3);
        assertEquals(3, as.getArrival());
    }

    @Test
    void getStartedAtMs() {
        var as = new AgentStats(1, 2, 3);

        long mockStarted = Utility.random.nextLong();
        as.setStartedAtMs(mockStarted);
        assertEquals(mockStarted, as.getStartedAtMs());
    }

    @Test
    void setStartedAtMs() {
        var as = new AgentStats(1, 2, 3);

        long mockStarted1 = Utility.random.nextLong();
        as.setStartedAtMs(mockStarted1);
        assertEquals(mockStarted1, as.getStartedAtMs());

        long mockStarted2 = Utility.random.nextLong();
        as.setStartedAtMs(mockStarted2);
        assertEquals(mockStarted2, as.getStartedAtMs());
    }

    @Test
    void getFinishedAtMs() {
        var as = new AgentStats(1, 2, 3);

        long mockFinished = Utility.random.nextLong();
        as.setFinishedAtMs(mockFinished);
        assertEquals(mockFinished, as.getFinishedAtMs());
    }

    @Test
    void setFinishedAtMs() {
        var as = new AgentStats(1, 2, 3);

        long mockFinished1 = Utility.random.nextLong();
        as.setFinishedAtMs(mockFinished1);
        assertEquals(mockFinished1, as.getFinishedAtMs());

        long mockFinished2 = Utility.random.nextLong();
        as.setFinishedAtMs(mockFinished2);
        assertEquals(mockFinished2, as.getFinishedAtMs());
    }

    @Test
    void getWaitedMs() {
        var as = new AgentStats(1, 2, 3);

        long mockWaited = Utility.random.nextLong();
        as.setWaitedMs(mockWaited);
        assertEquals(mockWaited, as.getWaitedMs());
    }

    @Test
    void setWaitedMs() {
        var as = new AgentStats(1, 2, 3);

        long mockWaited1 = Utility.random.nextLong();
        as.setWaitedMs(mockWaited1);
        assertEquals(mockWaited1, as.getWaitedMs());

        long mockWaited2 = Utility.random.nextLong();
        as.setWaitedMs(mockWaited2);
        assertEquals(mockWaited2, as.getWaitedMs());
    }

    @Test
    void getBlockedDenials() {
        var as = new AgentStats(1, 2, 3);

        int mockBlockedDenials = Utility.random.nextInt();
        as.setBlockedDenials(mockBlockedDenials);
        assertEquals(mockBlockedDenials, as.getBlockedDenials());
    }

    @Test
    void setBlockedDenials() {
        var as = new AgentStats(1, 2, 3);

        int mockBlockedDenials1 = Utility.random.nextInt();
        as.setBlockedDenials(mockBlockedDenials1);
        assertEquals(mockBlockedDenials1, as.getBlockedDenials());

        int mockBlockedDenials2 = Utility.random.nextInt();
        as.setBlockedDenials(mockBlockedDenials2);
        assertEquals(mockBlockedDenials2, as.getBlockedDenials());
    }

    @Test
    void getPreemptions() {
        var as = new AgentStats(1, 2, 3);

        int mockPreemptions = Utility.random.nextInt();
        as.setPreemptions(mockPreemptions);
        assertEquals(mockPreemptions, as.getPreemptions());
    }

    @Test
    void setPreemptions() {
        var as = new AgentStats(1, 2, 3);

        int mockPreemptions1 = Utility.random.nextInt();
        as.setPreemptions(mockPreemptions1);
        assertEquals(mockPreemptions1, as.getPreemptions());

        int mockPreemptions2 = Utility.random.nextInt();
        as.setPreemptions(mockPreemptions2);
        assertEquals(mockPreemptions2, as.getPreemptions());
    }
}