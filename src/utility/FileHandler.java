package utility;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public final class FileHandler {
    public static String getFileContent(String filePath, String linePrefix) {
        try {
            StringBuilder content = new StringBuilder();
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            lines.forEach(line -> {
                content.append(linePrefix).append(line).append("\n");
            });

            return content.toString().trim();
        } catch (IOException e) {
            System.out.printf("[!] Navedeni fajl (%s) se ne moze procitati.\n", filePath);
            return null;
        }
    }
}
