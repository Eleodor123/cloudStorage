package netty;

import control.StorageControl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import utils.CommandMessage;
import utils.Commands;
import utils.FileUtils;
import javafx.Controller;

import java.nio.file.Paths;
import java.util.Arrays;

public class CommandHandlerClient extends ChannelInboundHandlerAdapter {
    private StorageControl storageClient;
    private FileUtils fileUtils;
    ChannelHandlerContext context;
    private Controller controller;

    public CommandHandlerClient(StorageControl storageClient) {
        this.storageClient = storageClient;
        fileUtils = storageClient.getFileUtils();
        controller = storageClient.getController();
    }

    @Override
    public void channelActive(ChannelHandlerContext context){
        this.context = context;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msgObject) {
        try {
            CommandMessage commandMessage = (CommandMessage) msgObject;
            recognizeAndArrangeMessageObject(commandMessage);
        }
        finally {
            ReferenceCountUtil.release(msgObject);
        }
    }

    public void recognizeAndArrangeMessageObject(CommandMessage commandMessage) {
        switch (commandMessage.getCommand()) {
            case Commands.SERVER_NOTIFICATION_CLIENT_CONNECTED:
                onServerConnectedResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_OK:
            case Commands.SERVER_RESPONSE_ITEMS_LIST_OK:
            case Commands.SERVER_RESPONSE_UPLOAD_ITEM_OK:
            case Commands.SERVER_RESPONSE_UPLOAD_FILE_FRAGS_OK:
            case Commands.SERVER_RESPONSE_RENAME_ITEM_OK:
            case Commands.SERVER_RESPONSE_DELETE_ITEM_OK:
                updateStorageItemListInGUI(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_ERROR:
                onAuthErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_UPLOAD_ITEM_ERROR:
                onUploadItemErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_ITEM_OK:
                onDownloadItemOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_ITEM_ERROR:
                onDownloadFileErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_OK:
                onDownloadFileFragOkServerResponse(commandMessage);
                break;
        }
    }

    private void onServerConnectedResponse(CommandMessage commandMessage) {
        storageClient.startAuthorization(context);
    }

    private void onAuthErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onAuthErrorServerResponse() - Invalid login or password");
    }

    private void onUploadItemErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandMessageManager.onUploadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadItemOkServerResponse(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        if(storageClient.downloadItem(fileMessage.getClientDirectoryItem(), fileMessage.getItem(),
                fileMessage.getData(), fileMessage.getFileSize())){
            controller.updateClientItemListInGUI(fileMessage.getClientDirectoryItem());
        } else {
            printMsg("[client]" + fileUtils.getMsg());
        }
    }

    private void onDownloadFileErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandMessageManager.onDownloadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileFragOkServerResponse(CommandMessage commandMessage) {
        FileFragmentMessage fileFragMsg = (FileFragmentMessage) commandMessage.getMessageObject();
        int command;
        if(storageClient.downloadItemFragment(fileFragMsg)){
            command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_OK;
        } else {
            printMsg("[client]" + fileUtils.getMsg());
            command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR;
        }
        if(fileFragMsg.isFinalFileFragment()){
            if(storageClient.compileItemFragments(fileFragMsg)){
                controller.updateClientItemListInGUI(
                        fileFragMsg.getToDirectoryItem());
            } else {
                printMsg("[client]" + fileUtils.getMsg());
            }
        }
    }
    private void updateStorageItemListInGUI(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        controller.updateStorageItemListInGUI(directoryMessage.getDirectoryItem(),
                directoryMessage.getItemsList());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }

    public void printMsg(String msg){
        storageClient.printMsg(msg);
    }
}