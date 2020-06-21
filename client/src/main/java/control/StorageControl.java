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
    private Controller controller;
    ChannelHandlerContext context;
    private static final String IP_ADDR = "localhost";
    private static final int PORT = 8190;
    private final PrintStream log = System.out;
    public static final Path CLIENT_ROOT = Paths.get("storage","client_storage");
    private FileUtils fileUtils = FileUtils.getOwnObject();
    private final ItemUtils itemUtils = ItemUtils.getOwnObject();

    public StorageControl(Controller controller) {
        this.controller = controller;
    }

    public void run() throws Exception {
        new NettyClient(this, IP_ADDR, PORT).run();
    }

    public void startAuthorization() {
        requestAuthorization(controller.getLogin(), controller.getPassword());
    }

    private void requestAuthorization(String login, String password) {
        context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_AUTH,
                new AuthMessage(login, password)));
    }

    public void demandDirectoryItemList(String directoryPathname) {
        context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_ITEMS_LIST,
                new DirectoryMessage(directoryPathname)));
    }

    public void demandUploadItem(Item storageToDirItem, Item clientItem) throws IOException {
        if(clientItem.isDirectory()){
            showTextInGUI("It is not allowed to upload a directory!");
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
        fileUtils.cutAndSendFileByFrags(storageToDirItem, clientItem, fullFileSize,
                CLIENT_ROOT, context, Commands.SERVER_REQUEST_FILE_FRAG_UPLOAD);
    }

    private void uploadEntireFile(Item storageToDirItem, Item clientItem, long fileSize) {
        FileMessage fileMessage = new FileMessage(storageToDirItem,
                clientItem, fileSize);
        if(fileUtils.readFile(itemUtils.getRealPath(clientItem.getItemPathname(), CLIENT_ROOT),
                fileMessage)){
            context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_FILE_UPLOAD,
                    fileMessage));
        } else {
            printMsg("[client]" + fileUtils.getMsg());
            showTextInGUI(fileUtils.getMsg());
        }
    }

    public void demandDownloadItem(Item storageFromDirItem, Item clientToDirItem, Item storageItem){
        FileMessage fileMessage = new FileMessage(storageFromDirItem, clientToDirItem, storageItem);
        context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_DOWNLOAD_FILE,
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
        context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_RENAME_ITEM,
                new FileMessage(storageDirectoryItem, storageOriginItem, newName)));
    }

    public boolean deleteClientItem(Item item) {
        File fileObject = new File(itemUtils.getRealPath(item.getItemPathname(), CLIENT_ROOT).toString());
        return fileUtils.deleteFileObject(fileObject);
    }

    public void demandDeleteItem(Item storageDirectoryItem, Item item) {
        context.writeAndFlush(new CommandMessage(Commands.SERVER_REQUEST_DELETE_ITEM,
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
        return controller;
    }

    public void setCtx(ChannelHandlerContext context) {
        this.context = context;
    }

    public void printMsg(String msg){
        log.append(msg).append("\n");
    }

    public void showTextInGUI(String text){
        controller.showTextInGUI(text);
    }
}
