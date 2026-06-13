package vfs;

import java.util.Arrays;
import java.util.Locale;

// zarad jednostavnosti pretpostavlja se da je svaki fajl tekstualni i da je njegov sadrzaj reprezentabilan iskljucivo Stringovima
public final class VFSFile {
    private final String name;
    private String content;

    public VFSFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public synchronized String getContentFormatted(String prefix) {
        StringBuilder formattedContent = new StringBuilder();
        String[] lines = content.split("\n");
        Arrays.stream(lines).forEach(line -> formattedContent.append(prefix).append(line).append("\n"));
        return formattedContent.toString();
    }

    public boolean canOpen(String requestedMode) {
        String normalizedMode = requestedMode.toLowerCase(Locale.ROOT);
        return normalizedMode.equals("read") || normalizedMode.equals("write") || normalizedMode.equals("append");
    }

    public synchronized void writeContent(String newContent) {
        this.content = newContent;
    }

    public synchronized void appendContent(String text) {
        this.content = this.content + text;
    }

    public String getName() { return name; }
}
