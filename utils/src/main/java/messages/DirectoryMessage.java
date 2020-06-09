package messages;

import java.io.File;

public class DirectoryMessage extends AbstractMessage {
    private String directory;
    private File[] fileObjectsList;

    public DirectoryMessage(String directory) {
        this.directory = directory;
    }

    public void takeFileObjectsList(String directory){
        fileObjectsList = new File(directory).listFiles();
    }

    public String getDirectory() {
        return directory;
    }

    public File[] getFileObjectsList() {
        return fileObjectsList;
    }
}