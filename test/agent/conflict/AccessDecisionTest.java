package agent.conflict;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessDecisionTest {
    @Test
    void isGranted() {
        var ad  = new AccessDecision(true, false, 0, false, null);
        assertTrue(ad.isGranted());
    }

    @Test
    void isWaited() {
        var ad  = new AccessDecision(true, true, 0, false, null);
        assertTrue(ad.isWaited());
    }

    @Test
    void isInterrupted() {
        var ad  = new AccessDecision(false, false, 0, true, null);
        assertTrue(ad.isInterrupted());
    }

    @Test
    void getCycleDescription() {
        var ad  = new AccessDecision(false, false, 0, false, "cycle description 123");
        assertEquals("cycle description 123", ad.getCycleDescription());
    }
}