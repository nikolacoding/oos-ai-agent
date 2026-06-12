package main;

import utility.Constants;

// main.Simulation is in the default package, same as main.Main
public class Main {
    public static void main(String[] args) {

        if (args[0].toLowerCase().equals("no_conflict") ||
                args[0].toLowerCase().equals("preemptive") ||
                    args[0].toLowerCase().equals("cycle")) {
        } else {
            System.out.println("Nije odabran validan preset (no_conflict | preemptive | cycle).");
            return;
        }

        Simulation sim = new Simulation(Constants.SETTINGS_FILE_PATH, args[0]);
        sim.start();
    }

    // ULAZI:
    // postoje 3 preseta:
    // 1. no_conflict - nema konflikta i oba agenta rade disjunktno
    // 2. preemptive - ako se agentu 2 potrefi bolji prioritet (niza vrijednost agentPriority), preuzece fajl od agenta 1,
    //      a u suprotnom ce biti blokiran taj pokusaj
    // 3. cycle - A2 sa odgovarajucim prioritetom ce pokusati da ceka A1 koji ceka A2, pa ce nastati ciklus (zastoj) koji ce biti detektovan, odbijen i logovan
}
