package agent.operations;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

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
        } catch (IOException e) {
            System.out.printf("Navedeni fajl sa operacijama agenta (%s) se ne moze procitati.%n", agentOperationsFile.getPath());
        }
        return operations;
    }
}
