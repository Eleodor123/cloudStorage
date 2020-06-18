package netty;

import control.StorageControl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import javafx.Controller;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import utils.CommandMessage;
import utils.Commands;
import utils.FileUtils;

public class CommandHandlerClient extends ChannelInboundHandlerAdapter {
    private StorageControl storageControl;
    private FileUtils fileUtils;
    private ChannelHandlerContext context;
    private Controller controller;

    public CommandHandlerClient(StorageControl storageControl) {
        this.storageControl = storageControl;
        fileUtils = storageControl.getFileUtils();
        controller = storageControl.getController();
    }

    @Override
    public void channelActive(ChannelHandlerContext context){
        this.context = context;

        storageControl.setCtx(context);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msgObject) {
        try {
            CommandMessage commandMessage = (CommandMessage) msgObject;
            printMsg("[client]CommandHandlerClient.channelRead() - command: "
                    + commandMessage.getCommand());
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
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_OK:
            case Commands.SERVER_RESPONSE_FILE_FRAGS_UPLOAD_OK:
            case Commands.SERVER_RESPONSE_RENAME_ITEM_OK:
            case Commands.SERVER_RESPONSE_DELETE_ITEM_OK:
                updateStorageItemListInGUI(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_ERROR:
                onAuthErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR:
                onUploadItemErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_FILE_OK:
                onDownloadItemOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_FILE_ERROR:
                onDownloadFileErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_DOWNLOAD_FILE_FRAG_OK:
                onDownloadFileFragOkServerResponse(commandMessage);
                break;
        }
    }

    private void onServerConnectedResponse(CommandMessage commandMessage) {
        showTextInGUI("Server has connected, insert login and password.");
        controller.openAuthWindowInGUI();
    }

    private void onAuthErrorServerResponse(CommandMessage commandMessage) {
        showTextInGUI("Something wrong with your login or password! Insert them again.");
        controller.openAuthWindowInGUI();
    }

    private void onUploadItemErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onUploadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadItemOkServerResponse(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        if(storageControl.downloadItem(fileMessage.getClientDirectoryItem(), fileMessage.getItem(),
                fileMessage.getData(), fileMessage.getFileSize())){
            controller.updateClientItemListInGUI(fileMessage.getClientDirectoryItem());
        } else {
            printMsg("[client]" + fileUtils.getMsg());
            showTextInGUI(fileUtils.getMsg());
        }
    }

    private void onDownloadFileErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onDownloadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileFragOkServerResponse(CommandMessage commandMessage) {
        FileFragmentMessage fileFragMsg = (FileFragmentMessage) commandMessage.getMessageObject();
        int command;
        if(storageControl.downloadItemFragment(fileFragMsg)){
            command = Commands.CLIENT_RESPONSE_DOWNLOAD_FILE_FRAG_OK;
        } else {
            printMsg("[client]" + fileUtils.getMsg());
            showTextInGUI(fileUtils.getMsg());
            command = Commands.CLIENT_RESPONSE_DOWNLOAD_FILE_FRAG_ERROR;
        }
        if(fileFragMsg.isFinalFileFragment()){
            if(storageControl.compileItemFragments(fileFragMsg)){
                controller.updateClientItemListInGUI(
                        fileFragMsg.getToDirectoryItem());
            } else {
                printMsg("[client]" + fileUtils.getMsg());
                showTextInGUI(fileUtils.getMsg());
            }
        }
    }

    private void updateStorageItemListInGUI(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        showTextInGUI("");
        controller.updateStorageItemListInGUI(directoryMessage.getDirectoryItem(),
                directoryMessage.getItemsList());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void printMsg(String msg){
        storageControl.printMsg(msg);
    }

    public void showTextInGUI(String text){
        controller.showTextInGUI(text);
    }
}