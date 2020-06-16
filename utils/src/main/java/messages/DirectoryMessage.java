package messages;

import utils.Item;

import java.io.File;

public class DirectoryMessage extends AbstractMessage {
    private String directoryPathname;
    private Item directoryItem;
    private Item[] itemsList;

    public DirectoryMessage(String directoryPathname) {
        this.directoryPathname = directoryPathname;
    }

    public DirectoryMessage(Item directoryItem, Item[] itemsList) {
        this.directoryItem = directoryItem;
        this.itemsList = itemsList;
    }

    public String getDirectoryPathname() {
        return directoryPathname;
    }

    public Item getDirectoryItem() {
        return directoryItem;
    }

    public Item[] getItemsList() {
        return itemsList;
    }
}