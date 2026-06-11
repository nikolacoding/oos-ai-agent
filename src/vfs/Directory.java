package vfs;

import utility.UserInput;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Directory {
    private List<VFSFile> files = new ArrayList<>();
    public Directory(String path){
        // ucitavanje fajlova iz direktorijuma path (moutovanje) u memoriju
        // promjene fajlova u memoriji se nece odraziti na fajlove na disku
        File dir = new File(path);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    try {
                        // ponovo, pretpostavljamo da su svi fajlovi tekstualni i da ce svaki prepisan bajt biti validan char
                        String content = new String(Files.readAllBytes(file.toPath()));
                        System.out.println("Odaberite mod pristupa za fajl " + file.getName() + " (ro -- read-only  |  rw -- read-write):");
                        String mode = UserInput.userInputScanner.nextLine();
                        files.add(new VFSFile(file.getName(), content, mode));
                    } catch (IOException e) {
                        System.out.printf("Navedeni direktorijum (%s) sadrzi fajl koji se ne moze procitati (%s)%n", path, file.getName());
                    }
                }
            }
        }
        else {
            System.out.printf("Navedeni direktorijum (%s) nije validan.%n", path);
        }
    }
}
