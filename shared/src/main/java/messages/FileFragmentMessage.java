package messages;

import utils.Item;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Paths;

public class FileFragmentMessage extends AbstractMessage {
    public static final int CONST_FRAG_SIZE = 1024 * 1024 * 10;
    private Item toDirectoryItem;
    private Item item;
    private long fullFileSize;
    private int fileFragmentSize;
    private byte[] data;
    private int currentFragNumber;
    private int totalFragsNumber;
    private String toTempDirName;
    private String fragName;

    public FileFragmentMessage(
            Item toDirectoryItem, Item item, long fullFileSize,
            int currentFragNumber, int totalFragsNumber, int fileFragmentSize, byte[] data) {
        this.toDirectoryItem = toDirectoryItem;
        this.item = item;
        this.fullFileSize = fullFileSize;
        this.currentFragNumber = currentFragNumber;
        this.totalFragsNumber = totalFragsNumber;
        this.fileFragmentSize = fileFragmentSize;
        this.data = data;
        fragName = constructFileFragName(item.getItemName(), currentFragNumber, totalFragsNumber);
        toTempDirName = constructTempDirectoryName(item.getItemName(), fullFileSize);
    }

    private String constructFileFragName(String itemName, int currentFragNumber, int totalFragsNumber) {
        int count = String.valueOf(totalFragsNumber).length() -
                String.valueOf(currentFragNumber).length();
        StringBuilder sb = new StringBuilder(itemName).append("$frg");
        for (int i = 0; i < count; i++) {
            sb.append("0");
        }
        sb.append(currentFragNumber).append("-").append(totalFragsNumber);
        return Paths.get(sb.toString()).toString();
    }

    private String constructTempDirectoryName(String itemName, long fullFileSize) {
        return itemName + "$temp-" + fullFileSize;
    }

    public void readFileDataToFragment(String realItemPathname, long startByte) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(realItemPathname, "r");
        BufferedInputStream bis = new BufferedInputStream(Channels.newInputStream(raf.getChannel()));
        raf.seek(startByte);
        int result = bis.read(data);
        raf.close();
        bis.close();
    }

    public Item getToDirectoryItem() {
        return toDirectoryItem;
    }

    public Item getItem() {
        return item;
    }

    public long getFullFileSize() {
        return fullFileSize;
    }

    public byte[] getData() {
        return data;
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

    public int getCurrentFragNumber() {
        return currentFragNumber;
    }

    public String getToTempDirName() {
        return toTempDirName;
    }

    public String getFragName() {
        return fragName;
    }
}