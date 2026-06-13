package exceptions;

public final class MountingExceptions {

    public static final class UnreadableSettingsFileException extends RuntimeException {
        public UnreadableSettingsFileException(String path) {
            super(String.format("Navedeni settings fajl (%s) nije moguce procitati.", path));
        }
    }

    public static final class EmptySettingsFileException extends RuntimeException {
        public EmptySettingsFileException(String path) {
            super(String.format("Navedeni settings fajl (%s) je prazan.", path));
        }
    }

    public static final class IllegalMaxRunningAgentsException extends RuntimeException {
        public IllegalMaxRunningAgentsException(String path){
            super(String.format("Navedeni settings fajl (%s) nema validan settings.max_running_agents zapis.", path));
        }
    }

    public static final class IllegalMountsException  extends RuntimeException {
        public IllegalMountsException(String path){
            super(String.format("Navedeni settings fajl (%s) nema validan vfs.mounts zapis.", path));
        }
    }

    public static final class IllegalMountStructureException extends RuntimeException {
        public IllegalMountStructureException(String path){
            super(String.format("Navedeni settings fajl (%s) ima neispravnu strukturu nekog od vfs.mounts zapisa.", path));
        }
    }
}
