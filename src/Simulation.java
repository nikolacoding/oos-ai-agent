public class Simulation extends Thread {

    @Override
    public void run(){
        System.out.println("Simulacija pocinje");
        Agent a1 = new Agent(0, 0);
        Agent a2 = new Agent(1, 3);

        a1.start();
        a2.start();
    }
}
