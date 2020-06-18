package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import utils.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandHandlerServer extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext context;
    private final StorageServer storageServer;

    private Path userStorageRoot;
    private FileUtils fileUtils;
    private int command;

    public CommandHandlerServer(StorageServer storageServer) {
        this.storageServer = storageServer;
        fileUtils = storageServer.getFileUtils();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        this.context = context;
        CommandMessage commandMessage = (CommandMessage) msg;
        switch (commandMessage.getCommand()) {
            case Commands.SERVER_REQUEST_ITEMS_LIST:
                onDirectoryItemsListClientRequest(commandMessage);
                break;
            case Commands.SERVER_REQUEST_FILE_UPLOAD:
                onUploadItemClientRequest(commandMessage);
                break;
            case Commands.SERVER_REQUEST_DOWNLOAD_FILE:
                onDownloadItemClientRequest(commandMessage);
                break;
            case Commands.SERVER_REQUEST_FILE_FRAG_UPLOAD:
                onUploadFileFragClientRequest(commandMessage);
                break;
            case Commands.SERVER_REQUEST_RENAME_ITEM:
                onRenameItemClientRequest(commandMessage);
                break;
            case Commands.SERVER_REQUEST_DELETE_ITEM:
                onDeleteItemClientRequest(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_OK:
                onAuthClientRequest(commandMessage);
                break;
        }
    }

    private void onDirectoryItemsListClientRequest(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        Item storageDirItem = storageServer.createStorageDirectoryItem(
                directoryMessage.getDirectoryPathname(), userStorageRoot);
        sendItemsList(storageDirItem, Commands.SERVER_RESPONSE_ITEMS_LIST_OK);
    }

    private void onUploadItemClientRequest(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        if(storageServer.uploadItem(fileMessage.getStorageDirectoryItem(), fileMessage.getItem(),
                fileMessage.getData(), fileMessage.getFileSize(), userStorageRoot)){
            command = Commands.SERVER_RESPONSE_FILE_UPLOAD_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR;
        }
        sendItemsList(fileMessage.getStorageDirectoryItem(), command);
    }

    private void onDownloadItemClientRequest(CommandMessage commandMessage) throws IOException {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        storageServer.downloadItem(fileMessage, userStorageRoot, context);
    }

    private void onUploadFileFragClientRequest(CommandMessage commandMessage) {
        FileFragmentMessage fileFragMsg = (FileFragmentMessage) commandMessage.getMessageObject();
        if(storageServer.uploadItemFragment(fileFragMsg, userStorageRoot)){
            command = Commands.SERVER_RESPONSE_FILE_FRAG_UPLOAD_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_FILE_FRAG_UPLOAD_ERROR;
        }
        if(fileFragMsg.isFinalFileFragment()){
            if(storageServer.compileItemFragments(fileFragMsg, userStorageRoot)){
                command = Commands.SERVER_RESPONSE_FILE_FRAGS_UPLOAD_OK;
            } else {
                printMsg("[server]" + fileUtils.getMsg());
                command = Commands.SERVER_RESPONSE_FILE_FRAGS_UPLOAD_ERROR;
            }
            sendItemsList(fileFragMsg.getToDirectoryItem(), command);
        }
    }

    private void onRenameItemClientRequest(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        if(storageServer.renameStorageItem(fileMessage.getItem(), fileMessage.getNewName(), userStorageRoot)){
            command = Commands.SERVER_RESPONSE_RENAME_ITEM_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_RENAME_ITEM_ERROR;
        }
        sendItemsList(fileMessage.getStorageDirectoryItem(), command);
    }

    private void onDeleteItemClientRequest(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        if(storageServer.deleteClientItem(fileMessage.getItem(), userStorageRoot)){
            command = Commands.SERVER_RESPONSE_DELETE_ITEM_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_DELETE_ITEM_ERROR;
        }
        sendItemsList(fileMessage.getStorageDirectoryItem(), command);
    }

    private void onAuthClientRequest(CommandMessage commandMessage) {
        userStorageRoot = Paths.get(commandMessage.getDirectory());
        Item storageDirItem = new Item(storageServer.getSTORAGE_DEFAULT_DIR());
        sendItemsList(storageDirItem, commandMessage.getCommand());
        printMsg("[server]CommandHandlerServer.onAuthClientRequest() - " +
                "removed pipeline: " + context.channel().pipeline().remove(AuthGateway.class));
    }

    private void sendItemsList(Item storageDirItem, int command) {
        DirectoryMessage directoryMessage = new DirectoryMessage(storageDirItem,
                storageServer.storageItemsList(storageDirItem, userStorageRoot));
        context.writeAndFlush(new CommandMessage(command, directoryMessage));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }

    public void printMsg(String msg){
        storageServer.printMsg(msg);
    }
}