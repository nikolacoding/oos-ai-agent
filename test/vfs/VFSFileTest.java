package vfs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VFSFileTest {

    @Test
    void getContentFormatted() {
        VFSFile file = new VFSFile("test.txt", "line1\nline2\nline3");
        String expected = "> line1\n> line2\n> line3\n";
        String actual = file.getContentFormatted("> ");
        assertEquals(expected, actual);
    }

    @Test
    void canOpen() {
        VFSFile file = new VFSFile("test.txt", "content");
        assertTrue(file.canOpen("read"));
        assertTrue(file.canOpen("write"));
        assertTrue(file.canOpen("append"));
        assertFalse(file.canOpen("invalid_mode"));
    }

    @Test
    void writeContent() {
        VFSFile file = new VFSFile("test.txt", "old content");
        file.writeContent("new content");
        assertEquals("new content\n", file.getContentFormatted(""));
    }

    @Test
    void appendContent() {
        VFSFile file = new VFSFile("test.txt", "initial content");
        file.appendContent(" appended");
        assertEquals("initial content appended\n", file.getContentFormatted(""));
    }

    @Test
    void getName() {
        VFSFile file = new VFSFile("test.txt", "content");
        assertEquals("test.txt", file.getName());
    }
}