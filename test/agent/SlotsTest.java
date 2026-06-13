package agent;

import main.Simulation;
import org.junit.jupiter.api.Test;
import utility.Utility;

import static org.junit.jupiter.api.Assertions.*;

class SlotsTest {

    @Test
    void add() {
        int capacity = Utility.random.nextInt(100);
        var slots = new Slots(capacity);

        var sim = new Simulation("input/settings.json", "no_conflict");

        for (int i = 0; i < capacity; i++) {
            var agent = new Agent(1, 2, sim.getSlots(), sim, "no_conflict");
            assertTrue(slots.add(agent));
        }
        var agent = new Agent(1, 2, sim.getSlots(), sim, "no_conflict");
        assertFalse(slots.add(agent));
    }
}