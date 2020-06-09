package control;

import io.netty.channel.ChannelHandlerContext;
import javafx.Controller;
import messages.AuthMessage;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import netty.NettyClient;
import utils.CommandMessage;
import utils.Commands;
import utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageControl {
    private Controller Controller;
    ChannelHandlerContext context;
    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8190;
    private final PrintStream log = System.out;

    public static final String CLIENT_ROOT = "storage/client_storage";
    private final String defaultDirClient = "";
    private final String defaultDitServer = "";
    private FileUtils fileUtils;

    private final String login = "login1";
    private final String password = "pass1";

    public StorageControl(Controller Controller) {
        this.Controller = Controller;
        fileUtils = new FileUtils();
    }

    public void run() throws Exception {
        new NettyClient(this, IP_ADDR, PORT).run();
    }

    public void startAuthorization(ChannelHandlerContext context) {
        this.context = context;
        printMsg("***StorageControl.requestAuthorization() - has started***");
        requestAuthorization(login, password);
        printMsg("***StorageControl.requestAuthorization() - has finished***");
    }

    private void requestAuthorization(String login, String password) {
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_AUTH,
                new AuthMessage(login, password)));
    }

    public void demandDirectoryItemList(String directory) {
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_OBJECTS_LIST,
                new DirectoryMessage(directory)));
    }

    public void uploadFile(String fromDir, String toDir, String filename) throws IOException {
        printMsg("***StorageControl.uploadFile() - has started***");
        System.out.println("StorageControl.uploadFile - fromDir: " + fromDir +
                ", toDir: " + toDir + ", filename: " + filename);
        long fileSize = Files.size(Paths.get(fromDir, filename));
        if(fileSize > FileFragmentMessage.CONST_FRAG_SIZE){
            uploadFileByFrags(fromDir, toDir, filename, fileSize);
        } else {
            uploadEntireFile(fromDir, toDir, filename, fileSize);
        }
        printMsg("***StorageControl.uploadFile() - has finished***");
    }

    private void uploadFileByFrags(String fromDir, String toDir, String filename, long fullFileSize) throws IOException {
        long start = System.currentTimeMillis();

        int totalEntireFragsNumber = (int) fullFileSize / FileFragmentMessage.CONST_FRAG_SIZE;
        int finalFileFragmentSize = (int) fullFileSize - FileFragmentMessage.CONST_FRAG_SIZE * totalEntireFragsNumber;
        int totalFragsNumber = (finalFileFragmentSize == 0) ?
                totalEntireFragsNumber : totalEntireFragsNumber + 1;

        System.out.println("StorageControl.uploadFileByFrags() - fullFileSize: " + fullFileSize);
        System.out.println("StorageControl.uploadFileByFrags() - totalFragsNumber: " + totalFragsNumber);
        System.out.println("StorageControl.uploadFileByFrags() - totalEntireFragsNumber: " + totalEntireFragsNumber);

        long startByte = 0;
        byte[] data = new byte[FileFragmentMessage.CONST_FRAG_SIZE];
        String[] fragsNames = new String[totalFragsNumber];
        for (int i = 1; i <= totalEntireFragsNumber; i++) {
            FileFragmentMessage fileFragmentMessage =
                    new FileFragmentMessage(fromDir, toDir, filename, fullFileSize,
                            i, totalFragsNumber, FileFragmentMessage.CONST_FRAG_SIZE, fragsNames, data);
            fileFragmentMessage.readFileDataToFragment(fromDir, filename, startByte);
            startByte += FileFragmentMessage.CONST_FRAG_SIZE;

            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_FRAG_UPLOAD,
                    fileFragmentMessage));

        }

        System.out.println("StorageControl.uploadFileByFrags() - currentFragNumber: " + totalFragsNumber);
        System.out.println("StorageControl.uploadFileByFrags() - finalFileFragmentSize: " + finalFileFragmentSize);

        if(totalFragsNumber > totalEntireFragsNumber){
            byte[] dataFinal = new byte[finalFileFragmentSize];
            FileFragmentMessage fileFragmentMessage =
                    new FileFragmentMessage(fromDir, toDir, filename, fullFileSize,
                            totalFragsNumber, totalFragsNumber, finalFileFragmentSize, fragsNames, dataFinal);
            fileFragmentMessage.readFileDataToFragment(fromDir, filename, startByte);

            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_FRAG_UPLOAD,
                    fileFragmentMessage));
        }
        long finish = System.currentTimeMillis() - start;
        System.out.println("StorageControl.uploadFileByFrags() - duration(mc): " + finish);
    }

    private void uploadEntireFile(String fromDir, String toDir, String filename, long fileSize) {
        FileMessage fileMessage = new FileMessage(fromDir, toDir, filename, fileSize);

        System.out.println("StorageControl.uploadEntireFile() - fileUtils: " + fileUtils +
                ", fromDir: " + fromDir +
                ", toDir: " + toDir +
                ", fileMessage: " + fileMessage);

        if(fileUtils.readFile(fromDir, fileMessage)){
            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_UPLOAD,
                    fileMessage));
        } else {
            printMsg("[client]" + fileUtils.getMsg());
        }
    }

    public void downloadFile(String fromDir, String toDir, String filename){
        printMsg("***StorageControl.downloadFile() - has started***");
        FileMessage fileMessage = new FileMessage(fromDir, toDir, filename);
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_DOWNLOAD,
                fileMessage));
        printMsg("***StorageControl.downloadFile() - has finished***");
    }

    public void deleteFolder(File origin) {
        printMsg("***StorageControl.deleteFolder() - has finished*** - folder: " + origin);
    }

    public String getDefaultDitServer() {
        return defaultDitServer;
    }

    public String getDefaultDirClient() {
        return defaultDirClient;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public Controller getController() {
        return Controller;
    }

    public void printMsg(String msg){
        log.append(msg).append("\n");
    }
}