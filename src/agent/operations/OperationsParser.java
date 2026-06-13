package agent.operations;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

import utility.Utility;

// >>> primjer ulaznog fajla sa komandama:
// THINK 3
// OPEN /shared/a.txt read as f
// READ f
// CLOSE f
// OPEN /work/result/txt append as out
// APPEND out "agent A"
// CLOSE out

public class OperationsParser {
    public static Queue<Operation> parseOperations(File agentOperationsFile) {
        Queue<Operation> operations = new LinkedList<>();
        try {
            List<String> lines = Files.readAllLines(agentOperationsFile.toPath());
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    operations.add(new Operation(line));
                }
            }
        } catch (IOException _) {
            // debug linija
            // System.out.printf("Navedeni fajl sa operacijama agenta (%s) se ne moze procitati. Generisu se nasumicne operacije.\n", agentOperationsFile.getPath());

            return generateNRandomOperations(5);
        }
        return operations;
    }

    public static Queue<Operation> generateNRandomOperations(int n) {
        Queue<Operation> operations = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            int opType = Utility.random.nextInt(5);
            switch (opType) {
                case 0:
                    operations.add(new Operation(String.format("THINK %d", Utility.random.nextInt(5) + 1)));
                    break;
                case 1:
                    // TODO: namjestiti putanju konacnu
                    operations.add(new Operation(String.format("OPEN /shared/file%d.txt read as f%d", Utility.random.nextInt(10), i)));
                    break;
                case 2:
                    operations.add(new Operation(String.format("READ f%d", i)));
                    break;
                case 3:
                    operations.add(new Operation(String.format("CLOSE f%d", i)));
                    break;
                case 4:
                    operations.add(new Operation(String.format("APPEND out \"agent A%d\"", i)));
                    break;
            }
        }
        return operations;
    }
}
