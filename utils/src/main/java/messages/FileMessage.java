package messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {
    private String fromDir;
    private String toDir;
    private String filename;
    private byte[] data;

    public FileMessage(String fromDir, String toDir, String filename) {
        this.fromDir = fromDir;
        this.toDir = toDir;
        this.filename = filename;
    }

    public void readFileData() throws IOException {
        this.data = Files.readAllBytes(Paths.get(fromDir, filename));
    }

    public String getFromDir() {
        return fromDir;
    }

    public String getToDir() {
        return toDir;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }
}
