package control;

import io.netty.channel.ChannelHandlerContext;
import javafx.Controller;
import messages.AuthMessage;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import netty.NettyClient;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageControl {
    private Controller Controller;
    ChannelHandlerContext context;
    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8190;
    private final PrintStream log = System.out;

    public static final Path CLIENT_ROOT = Paths.get("storage","client_storage");
    private FileUtils fileUtils = FileUtils.getOwnObject();
    private final ItemUtils itemUtils = ItemUtils.getOwnObject();

    private final String login = "login1";
    private final String password = "pass1";

    public StorageControl(Controller Controller) {
        this.Controller = Controller;
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

    public void demandUploadItem(Item storageToDirItem, Item clientItem) throws IOException {
        if(clientItem.isDirectory()){
            return;
        }
        Path realClientItemPath = itemUtils.getRealPath(clientItem.getItemPathname(), CLIENT_ROOT);
        long fileSize = Files.size(realClientItemPath);
        if(fileSize > FileFragmentMessage.CONST_FRAG_SIZE){
            uploadFileByFrags(storageToDirItem, clientItem, fileSize);
        } else {
            uploadEntireFile(storageToDirItem, clientItem, fileSize);
        }
    }

    private void uploadFileByFrags(Item storageToDirItem, Item clientItem, long fullFileSize) throws IOException {
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
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    storageToDirItem, clientItem, fullFileSize, i, totalFragsNumber,
                    FileFragmentMessage.CONST_FRAG_SIZE, data);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(clientItem.getItemPathname(), CLIENT_ROOT).toString(),
                    startByte);
            startByte += FileFragmentMessage.CONST_FRAG_SIZE;
            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_FRAG_UPLOAD,
                    fileFragmentMessage));
        }

        System.out.println("StorageControl.uploadFileByFrags() - currentFragNumber: " + totalFragsNumber);
        System.out.println("StorageControl.uploadFileByFrags() - finalFileFragmentSize: " + finalFileFragmentSize);

        if(totalFragsNumber > totalEntireFragsNumber){
            byte[] dataFinal = new byte[finalFileFragmentSize];
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    storageToDirItem, clientItem, fullFileSize, totalFragsNumber,
                    totalFragsNumber, finalFileFragmentSize, dataFinal);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(clientItem.getItemPathname(), CLIENT_ROOT).toString(),
                    startByte);
            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_FRAG_UPLOAD,
                    fileFragmentMessage));
        }
        long finish = System.currentTimeMillis() - start;
        System.out.println("StorageControl.uploadFileByFrags() - duration(mc): " + finish);
    }

    private void uploadEntireFile(Item storageToDirItem, Item clientItem, long fileSize) {
        FileMessage fileMessage = new FileMessage(storageToDirItem,
                clientItem, fileSize);
        if(fileUtils.readFile(itemUtils.getRealPath(clientItem.getItemPathname(), CLIENT_ROOT),
                fileMessage)){
            context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_UPLOAD,
                    fileMessage));
        } else {
            printMsg("[client]" + fileUtils.getMsg());
        }
    }

    public void demandDownloadItem(Item storageFromDirItem, Item clientToDirItem, Item storageItem){
        FileMessage fileMessage = new FileMessage(storageFromDirItem, clientToDirItem, storageItem);
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_FILE_UPLOAD,
                fileMessage));
    }

    public boolean downloadItem(Item clientToDirItem, Item item, byte[] data, long fileSize){
        String realDirPathname = itemUtils.getRealPath(clientToDirItem.getItemPathname(), CLIENT_ROOT).toString();
        Path realNewToItemPath = Paths.get(realDirPathname, item.getItemName());
        return fileUtils.saveFile(realNewToItemPath, data, fileSize);
    }

    public boolean downloadItemFragment(FileFragmentMessage fileFragMsg) {
        Path realToTempDirPath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getToTempDirName()).toString(),
                CLIENT_ROOT);
        Path realToFragPath = Paths.get(
                realToTempDirPath.toString(), fileFragMsg.getFragName());
        return fileUtils.saveFileFragment(realToTempDirPath, realToFragPath, fileFragMsg);
    }

    public boolean compileItemFragments(FileFragmentMessage fileFragMsg) {
        Path realToTempDirPath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getToTempDirName()).toString(),
                CLIENT_ROOT);
        Path realToFilePath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getItem().getItemName()).toString(),
                CLIENT_ROOT);
        return fileUtils.compileFileFragments(realToTempDirPath, realToFilePath, fileFragMsg);
    }

    public boolean renameClientItem(Item origin, String newName) {
        Path originPath = itemUtils.getRealPath(origin.getItemPathname(), CLIENT_ROOT);
        File originFileObject = new File(originPath.toString());
        Path newPath = Paths.get(originFileObject.getParent(), newName);
        File newFileObject = new File(newPath.toString());
        return originFileObject.renameTo(newFileObject);
    }

    public void demandRenameItem(Item storageDirectoryItem, Item storageOriginItem, String newName) {
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_RENAME_FILE,
                new FileMessage(storageDirectoryItem, storageOriginItem, newName)));
    }

    public boolean deleteClientItem(Item item) {
        File fileObject = new File(itemUtils.getRealPath(item.getItemPathname(), CLIENT_ROOT).toString());
        return fileUtils.deleteFileObject(fileObject);
    }

    public void demandDeleteItem(Item storageDirectoryItem, Item item) {
        context.writeAndFlush(new CommandMessage(Commands.REQUEST_SERVER_DELETE_FILE,
                new FileMessage(storageDirectoryItem, item)));
    }

    public Item getParentDirItem(Item directoryItem, Item defaultDirItem, Path rootPath) {
        return itemUtils.getParentDirItem(directoryItem, defaultDirItem,
                rootPath);
    }

    public Item[] clientItemsList(Item clientCurrentDirItem) {
        return itemUtils.getItemsList(clientCurrentDirItem, CLIENT_ROOT);
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