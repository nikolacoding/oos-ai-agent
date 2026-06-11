import agent.Agent;
import agent.Slots;

import java.util.*;

public class Simulation extends Thread {

    private final Slots slots = new Slots(2);

    @Override
    public void run(){
        System.out.println("Simulacija pocinje");
        Agent a1 = new Agent(0, 0, slots);
        Agent a2 = new Agent(1, 3, slots);

        a1.start();
        a2.start();
    }
}
