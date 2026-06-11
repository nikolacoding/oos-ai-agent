package vfs;

import utility.Constants;

// zarad jednostavnosti pretpostavlja se da je svaki fajl tekstualni i da je njegov sadrzaj reprezentabilan iskljucivo Stringovima
public final class VFSFile {
    private final String name;
    private final String content;
    private final String mode;

    public VFSFile(String name, String content, String mode) {
        this.name = name;
        this.content = content;

        if (mode.equals("ro") || mode.equals("rw"))
            this.mode = mode;
        else
            this.mode = Constants.DEFAULT_FILE_MODE;
    }

    public String getName() { return name; }
    public String getContent() { return content; }
    public String getMode() { return mode; }
}
