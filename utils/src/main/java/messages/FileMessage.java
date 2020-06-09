package messages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {
    private String fromDir;
    private String toDir;
    private String filename;
    private long fileSize;
    private byte[] data;

    public FileMessage(String fromDir, String toDir, String filename) {
        this.fromDir = fromDir;
        this.toDir = toDir;
        this.filename = filename;
    }

    public FileMessage(String fromDir, String toDir, String filename, long fileSize) {
        this.fromDir = fromDir;
        this.toDir = toDir;
        this.filename = filename;
        this.fileSize = fileSize;
    }

    public void readFileData() throws IOException {
        this.data = Files.readAllBytes(Paths.get(fromDir, filename));
    }

    public void readFileData(String fromDir) throws IOException {
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
