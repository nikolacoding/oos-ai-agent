import utility.Constants;

public class Main {
    public static void main(String[] args) {
        Simulation sim = new Simulation(Constants.SETTINGS_FILE_PATH, "no_conflict");
        sim.start();
    }
}
