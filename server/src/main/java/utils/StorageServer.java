package utils;

import io.netty.channel.ChannelHandlerContext;
import messages.FileFragmentMessage;
import messages.FileMessage;
import netty.NettyServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StorageServer {
    private final int PORT = 8190;
    private final PrintStream log = System.out;
    private static final Path STORAGE_ROOT_PATH = Paths.get("storage","server_storage");
    private final String STORAGE_DEFAULT_DIR = "";
    private Item storageDefaultDirItem;
    private Map<ChannelHandlerContext, String> authorizedUsers;
    private AuthorizationController usersAuthController;
    private FileUtils fileUtils = FileUtils.getOwnObject();
    private final ItemUtils itemUtils = ItemUtils.getOwnObject();

    public void run() throws Exception {
        authorizedUsers = new HashMap<>();
        usersAuthController = new AuthorizationController(this);
        storageDefaultDirItem = new Item(STORAGE_DEFAULT_DIR);
        new NettyServer(this, PORT).run();
    }

    public Item[] storageItemsList(Item storageDirItem, Path userStorageRoot) {
        return itemUtils.getItemsList(storageDirItem, userStorageRoot);
    }

    public Item createStorageDirectoryItem(String storageDirPathname, Path userStorageRoot) {
        return itemUtils.createDirectoryItem(storageDirPathname, storageDefaultDirItem, userStorageRoot);
    }

    public boolean uploadItem(Item storageToDirItem, Item item, byte[] data, long fileSize, Path userStorageRoot){
        Path realNewToItemPath = Paths.get(
                itemUtils.getRealPath(storageToDirItem.getItemPathname(), userStorageRoot).toString(),
                item.getItemName());
        return fileUtils.saveFile(realNewToItemPath, data, fileSize);
    }

    public boolean uploadItemFragment(FileFragmentMessage fileFragMsg, Path userStorageRoot) {
        Path realToTempDirPath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getToTempDirName()).toString(),
                userStorageRoot);
        Path realToFragPath = Paths.get(
                realToTempDirPath.toString(), fileFragMsg.getFragName());
        return fileUtils.saveFileFragment(realToTempDirPath, realToFragPath, fileFragMsg);
    }

    public boolean compileItemFragments(FileFragmentMessage fileFragMsg, Path userStorageRoot) {
        Path realToTempDirPath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getToTempDirName()).toString(),
                userStorageRoot);
        Path realToFilePath = itemUtils.getRealPath(
                Paths.get(
                        fileFragMsg.getToDirectoryItem().getItemPathname(),
                        fileFragMsg.getItem().getItemName()).toString(),
                userStorageRoot);
        return fileUtils.compileFileFragments(realToTempDirPath, realToFilePath, fileFragMsg);
    }

    public void downloadItem(FileMessage fileMessage, Path userStorageRoot,
                             ChannelHandlerContext ctx) throws IOException {
        if(fileMessage.getItem().isDirectory()){
            return;
        }
        Path realStorageItemPath = itemUtils.getRealPath(fileMessage.getItem().getItemPathname(), userStorageRoot);
        long fileSize = Files.size(realStorageItemPath);
        if(fileSize > FileFragmentMessage.CONST_FRAG_SIZE){
            downloadFileByFrags(fileMessage.getClientDirectoryItem(),
                    fileMessage.getItem(), fileSize, userStorageRoot, ctx);
        } else {
            downloadEntireFile(fileMessage.getClientDirectoryItem(), fileMessage.getItem(),
                    fileMessage.getItem(), fileSize, userStorageRoot, ctx);
        }
    }

    private void downloadFileByFrags(Item clientToDirItem, Item storageItem,
                                     long fullFileSize, Path userStorageRoot,
                                     ChannelHandlerContext ctx) throws IOException {
        long start = System.currentTimeMillis();
        int totalEntireFragsNumber = (int) fullFileSize / FileFragmentMessage.CONST_FRAG_SIZE;
        int finalFileFragmentSize = (int) fullFileSize - FileFragmentMessage.CONST_FRAG_SIZE * totalEntireFragsNumber;
        int totalFragsNumber = (finalFileFragmentSize == 0) ?
                totalEntireFragsNumber : totalEntireFragsNumber + 1;

        long startByte = 0;
        byte[] data = new byte[FileFragmentMessage.CONST_FRAG_SIZE];

        for (int i = 1; i <= totalEntireFragsNumber; i++) {
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    clientToDirItem, storageItem, fullFileSize, i, totalFragsNumber,
                    FileFragmentMessage.CONST_FRAG_SIZE, data);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(storageItem.getItemPathname(), userStorageRoot).toString(),
                    startByte);
            startByte += FileFragmentMessage.CONST_FRAG_SIZE;
            ctx.writeAndFlush(new CommandMessage(Commands.SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_OK,
                    fileFragmentMessage));
        }

        if(totalFragsNumber > totalEntireFragsNumber){
            byte[] dataFinal = new byte[finalFileFragmentSize];
            FileFragmentMessage fileFragmentMessage = new FileFragmentMessage(
                    clientToDirItem, storageItem, fullFileSize, totalFragsNumber,
                    totalFragsNumber, finalFileFragmentSize, dataFinal);
            fileFragmentMessage.readFileDataToFragment(
                    itemUtils.getRealPath(storageItem.getItemPathname(), userStorageRoot).toString(),
                    startByte);
            ctx.writeAndFlush(new CommandMessage(Commands.SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_OK,
                    fileFragmentMessage));
        }

        long finish = System.currentTimeMillis() - start;
        System.out.println("CloudStorageClient.uploadFileByFrags() - duration(mc): " + finish);
    }

    private void downloadEntireFile(Item clientToDirItem, Item storageItem, Item item,
                                    long fileSize, Path userStorageRoot, ChannelHandlerContext ctx){
        FileMessage fileMessage = new FileMessage(storageItem, clientToDirItem, item, fileSize);
        int command;
        if(fileUtils.readFile(itemUtils.getRealPath(storageItem.getItemPathname(), userStorageRoot),
                fileMessage)){
            command = Commands.SERVER_RESPONSE_DOWNLOAD_ITEM_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_DOWNLOAD_ITEM_ERROR;
        }
        ctx.writeAndFlush(new CommandMessage(command, fileMessage));
    }

    public boolean renameStorageItem(Item origin, String newName, Path userStorageRoot) {
        Path originPath = itemUtils.getRealPath(origin.getItemPathname(), userStorageRoot);
        File originFileObject = new File(originPath.toString());
        Path newPath = Paths.get(originFileObject.getParent(), newName);
        File newFileObject = new File(newPath.toString());
        return originFileObject.renameTo(newFileObject);
    }

    public boolean deleteClientItem(Item item, Path userStorageRoot) {
        File fileObject = new File(itemUtils.getRealPath(item.getItemPathname(),
                userStorageRoot).toString());
        return fileUtils.deleteFileObject(fileObject);
    }

    public Path getSTORAGE_ROOT_PATH() {
        return STORAGE_ROOT_PATH;
    }

    public String getSTORAGE_DEFAULT_DIR() {
        return STORAGE_DEFAULT_DIR;
    }

    public Map<ChannelHandlerContext, String> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public AuthorizationController getUsersAuthController() {
        return usersAuthController;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public void printMsg(String msg){
        log.append(msg).append("\n");
    }
}
