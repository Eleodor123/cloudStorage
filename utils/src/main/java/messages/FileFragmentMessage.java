package messages;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileFragmentMessage extends AbstractMessage {
    public static final int CONST_FRAG_SIZE = 1024 * 1024 * 10;
    private String fromDir;
    private String toDir;
    private String filename;
    private long fullFileSize;
    private String[] fragsNames;
    private int fileFragmentSize;
    private byte[] data;
    private int currentFragNumber;
    private int totalFragsNumber;
    private String toTempDir;

    public FileFragmentMessage(
            String fromDir,
            String toDir,
            String filename,
            long fullFileSize,
            int currentFragNumber,
            int totalFragsNumber,
            int fileFragmentSize,
            String[] fragsNames,
            byte[] data) {
        this.fromDir = fromDir;
        this.toDir = toDir;
        this.filename = filename;
        this.fullFileSize = fullFileSize;
        this.currentFragNumber = currentFragNumber;
        this.totalFragsNumber = totalFragsNumber;
        this.fileFragmentSize = fileFragmentSize;
        this.fragsNames = fragsNames;
        this.data = data;
        fragsNames[currentFragNumber - 1] = filename;
        fragsNames[currentFragNumber - 1] = fragsNames[currentFragNumber - 1].concat(".frg")
                .concat(String.valueOf(currentFragNumber))
                .concat("-").concat(String.valueOf(totalFragsNumber));

        toTempDir = toDir;
        toTempDir = toTempDir.concat("/").concat(filename)
                .concat("-temp-").concat(String.valueOf(fullFileSize));
    }

    public void readFileDataToFragment(String fromDir, String filename, long startByte) throws IOException {
        String path = fromDir;
        path = path.concat("/").concat(filename);
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        BufferedInputStream bis = new BufferedInputStream(Channels.newInputStream(raf.getChannel()));
        raf.seek(startByte);
        bis.read(data);
        raf.close();
        bis.close();
    }

    public String getToDir() {
        return toDir;
    }

    public String getFilename() {
        return filename;
    }

    public long getFullFileSize() {
        return fullFileSize;
    }

    public byte[] getData() {
        return data;
    }

    public String getToTempDir() {
        return toTempDir;
    }

    public int getFileFragmentSize() {
        return fileFragmentSize;
    }

    public boolean isFinalFileFragment(){
        return currentFragNumber == totalFragsNumber;
    }

    public int getTotalFragsNumber() {
        return totalFragsNumber;
    }

    public String[] getFragsNames() {
        return fragsNames;
    }

    public int getCurrentFragNumber() {
        return currentFragNumber;
    }


//    private static final int CONST_FRAG_SIZE = 1024 * 1024 * 10;
//    private String filename;
//    private byte[] data;
//    private int currentFragNumber;
//    private int totalFragsNumber;
//    public FileFragmentMessage(String root, String filename) throws IOException {
//        this.filename = filename;
//        this.data = Files.readAllBytes(Paths.get(root, filename));
//    }
//
//    public String getFilename() {
//        return filename;
//    }
//
//    public byte[] getData() {
//        return data;
//    }
//
//    @Override
//    public String toString() {
//        return "FileFragment{" +
//                "filename='" + filename + '\'' +
//                ", data=" + Arrays.toString(data) +
//                '}';
//    }
}
