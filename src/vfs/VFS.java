package vfs;

import utility.Utility;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class VFS {
    private List<VFSFile> files = new ArrayList<>();
    public VFS(String path, String mode){
        // ucitavanje fajlova iz direktorijuma path (moutovanje) u memoriju
        // promjene fajlova u memoriji se nece odraziti na fajlove na disku
        File dir = new File(path);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    try {
                        // ponovo, pretpostavljamo da su svi fajlovi tekstualni i da ce svaki prepisan bajt biti validan char
                        String content = new String(Files.readAllBytes(file.toPath()));
                        files.add(new VFSFile(file.getName(), content, mode));
                    } catch (IOException e) {
                        // debug linija
                        System.out.printf("Navedeni direktorijum (%s) sadrzi fajl koji se ne moze procitati (%s)\n", path, file.getName());
                    }
                }
            }
            System.out.printf("> Mountovan direktorijum %s sa %d fajlova.\n", dir.getAbsolutePath(), files.size());
        }
        else {
            System.out.printf("[!] Navedeni direktorijum (%s) nije validan.\n", path);
        }
    }

    public List<VFSFile> getFiles() { return files; }

    public VFSFile findFile(String fileName) {
        for (VFSFile file : files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }

        return null;
    }
}
