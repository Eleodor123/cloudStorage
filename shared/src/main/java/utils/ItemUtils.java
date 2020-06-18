package utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ItemUtils {
    private static ItemUtils ownObject = new ItemUtils();

    public static ItemUtils getOwnObject() {
        return ownObject;
    }

    public Item createDirectoryItem(String directoryPathname, Item defaultDirItem, Path rootPath) {
        if(directoryPathname.equals(defaultDirItem.getItemPathname())){
            return defaultDirItem;
        } else {
            String directoryName = getRealPath(directoryPathname, rootPath).getFileName().toString();
            Path parentPath = getParentPath(directoryPathname, rootPath);
            String parentName = parentPath.getFileName().toString();
            return new Item(directoryName, parentName,
                    directoryPathname, parentPath.toString(), true);
        }
    }

    public Item[] getItemsList(Item directoryItem, Path rootPath) {
        File dirFileObject = new File(getRealPath(directoryItem.getItemPathname(), rootPath).toString());
        File[] files = dirFileObject.listFiles();
        assert files != null;
        Item[] items = new Item[files.length];
        for (int i = 0; i < files.length; i++) {
            String itemName = files[i].getName();
            String itemPathname = getItemPathname(files[i].getPath(), rootPath);
            items[i] = new Item(itemName, directoryItem.getItemName(), itemPathname,
                    directoryItem.getItemPathname(), files[i].isDirectory());
        }
        return items;
    }

    private String getItemPathname(String realItemPathname, Path rootPath) {
        return rootPath.relativize(Paths.get(realItemPathname)).toString();
    }

    public Path getRealPath(String itemPathname, Path rootPath) {
        return Paths.get(rootPath.toString(), itemPathname);
    }

    public Item getParentDirItem(Item directoryItem, Item defaultDirItem, Path rootPath) {
        if(directoryItem.isDefaultDirectory() ||
                directoryItem.getParentName().equals(defaultDirItem.getItemName())){
            return defaultDirItem;
        } else {
            Path parentPath = getParentPath(directoryItem.getParentPathname(),
                    rootPath);
            String parentName = parentPath.getFileName().toString();
            return new Item(directoryItem.getParentName(), parentName,
                    directoryItem.getParentPathname(), parentPath.toString(), true);
        }
    }

    private Path getParentPath(String itemPathname, Path rootPath) {
        return rootPath.relativize(getRealPath(itemPathname, rootPath).getParent());
    }

}
