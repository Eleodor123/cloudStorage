package utils.handlers;

import messages.FileFragmentMessage;
import messages.FileMessage;
import tcp.TCPClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FileCommandHandler extends CommandHandler{
    private FileMessage fileMessage;
    private FileFragmentMessage fileFragmentMessage;

    public FileCommandHandler(FileMessage fileMessage) {
        this.fileMessage = fileMessage;
    }

    public FileCommandHandler(FileFragmentMessage fileFragmentMessage) {
        this.fileFragmentMessage = fileFragmentMessage;
    }

    public FileMessage getFileMessage() {
        return fileMessage;
    }

    public FileFragmentMessage getFileFragmentMessage() {
        return fileFragmentMessage;
    }

    public boolean saveDownloadedFile(TCPClient client, String toDir, FileMessage fileMessage) {
        System.out.println("(Client)FileCommandHandler.saveDownloadedFile - fileMessage.getFilename(): " +
                fileMessage.getFilename() +
                ". Arrays.toString(fileMessage.getData()): " +
                Arrays.toString(fileMessage.getData()));

        try {
            Files.write(Paths.get(toDir, fileMessage.getFilename()),
                    fileMessage.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            client.printMsg("FileCommandHandler.saveUploadedFile() - Something wrong with the directory or the file!");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}