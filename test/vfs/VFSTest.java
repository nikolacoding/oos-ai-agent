package vfs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VFSTest {

    @Test
    void getFiles() {
        var vfs = new VFS("test/vfs/test_dir");
        var files = vfs.getFiles();
        assertEquals(3, files.size());

        for (int i = 0; i < files.size(); i++)
            assertEquals(String.format("%d.txt", i), files.get(i).getName());
    }

    @Test
    void findFile() {
        var vfs = new VFS("test/vfs/test_dir");
        var files = vfs.getFiles();

        for (int i = 0; i < files.size(); i++) {
            var file = vfs.findFile(String.format("%d.txt", i));
            assertNotNull(file);
        }
    }
}