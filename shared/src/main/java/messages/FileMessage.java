package messages;

import utils.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {
    private long fileSize;
    private byte[] data;
    private Item clientDirectoryItem;
    private Item storageDirectoryItem;
    private Item item;
    private String newName;

    public FileMessage(Item storageDirectoryItem, Item item, long fileSize) {
        this.storageDirectoryItem = storageDirectoryItem;
        this.item = item;
        this.fileSize = fileSize;
    }

    public FileMessage(Item storageDirectoryItem, Item clientDirectoryItem, Item item) {
        this.storageDirectoryItem = storageDirectoryItem;
        this.clientDirectoryItem = clientDirectoryItem;
        this.item = item;
    }

    public FileMessage(Item storageDirectoryItem, Item clientDirectoryItem, Item item, long fileSize) {
        this.storageDirectoryItem = storageDirectoryItem;
        this.clientDirectoryItem = clientDirectoryItem;
        this.item = item;
        this.fileSize = fileSize;
    }

    public FileMessage(Item storageDirectoryItem, Item item, String newName) {
        this.storageDirectoryItem = storageDirectoryItem;
        this.item = item;
        this.newName = newName;
    }

    public FileMessage(Item storageDirectoryItem, Item item) {
        this.storageDirectoryItem = storageDirectoryItem;
        this.item = item;
    }

    public void readFileData(String itemPathname) throws IOException {
        this.data = Files.readAllBytes(Paths.get(itemPathname));
    }

    public Item getClientDirectoryItem() {
        return clientDirectoryItem;
    }

    public Item getStorageDirectoryItem() {
        return storageDirectoryItem;
    }

    public Item getItem() {
        return item;
    }

    public String getNewName() {
        return newName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }
}
