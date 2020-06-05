package utils.handlers;

import messages.FileFragmentMessage;
import messages.FileMessage;
import tcp.TCPServer;

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

    public boolean saveUploadedFile(TCPServer server, String toDir, FileMessage fileMessage) {
        System.out.println("(Server)FileCommandHandler.saveDownloadedFile - fileMessage.getFilename(): " +
                fileMessage.getFilename() +
                ". Arrays.toString(fileMessage.getData()): " +
                Arrays.toString(fileMessage.getData()));
        try {
            Files.write(Paths.get(toDir, fileMessage.getFilename()),
                    fileMessage.getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            server.printMsg("FileCommandHandler.saveUploadedFile() - Something wrong with the directory or the file!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean downloadFile(TCPServer server, FileMessage fileMessage) {

        System.out.println("(Server)FileCommandHandler.downloadFile - fileMessage.getFilename(): " +
                fileMessage.getFilename() +
                ". Arrays.toString(fileMessage.getData()): " +
                Arrays.toString(fileMessage.getData()));

        try {
            fileMessage.readFileData();
        } catch (IOException e) {
            server.printMsg("FileCommandHandler.downloadFile() - Something wrong with the directory or the file!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
