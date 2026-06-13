package agent;

import main.Simulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    void getAgentId() {
        var sim = new Simulation("input/settings.json", "no_conflict");
        var a1 = new Agent(5, 6, sim.getSlots(), sim, "no_conflict");
        var a2 = new Agent(5, 6, sim.getSlots(), sim, "no_conflict");

        assertTrue(a1.getAgentId() > 0);
        assertTrue(a2.getAgentId() > a1.getAgentId());
        // ne mogu direktno porediti sa 1 i 2 jer se testovi vrse isprepletano, samim tim se instanciraju agenti isprepletano i ID-jevi nisu predvidivi
    }

    @Test
    void getAgentPriority() {
        var sim =  new Simulation("input/settings.json", "no_conflict");
        var a1 = new Agent(5, 6, sim.getSlots(), sim, "no_conflict");
        var a2 = new Agent(65, 6, sim.getSlots(), sim, "no_conflict");

        assertEquals(5, a1.getAgentPriority());
        assertEquals(65, a2.getAgentPriority());
    }

    @Test
    void getAgentArrival() {
        var sim =  new Simulation("input/settings.json", "no_conflict");
        var a1 = new Agent(5, 6, sim.getSlots(), sim, "no_conflict");
        var a2 = new Agent(6, 7, sim.getSlots(), sim, "no_conflict");
        assertEquals(6, a1.getAgentArrival());
        assertEquals(7, a2.getAgentArrival());
    }
}