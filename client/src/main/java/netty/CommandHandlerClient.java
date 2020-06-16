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

    private int command;

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
                onAuthOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_ERROR:
                onAuthErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_OBJECTS_LIST_OK:
                onFileObjectsListOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_OK:
                onUploadFileOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR:
                onUploadFileErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_DOWNLOAD_OK:
                onDownloadFileOkServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_DOWNLOAD_ERROR:
                onDownloadFileErrorServerResponse(commandMessage);
                break;
            case Commands.SERVER_RESPONSE_FILE_FRAGS_DOWNLOAD_OK:
                onDownloadFileFragOkServerResponse(commandMessage);
                break;
        }
    }

    private void onServerConnectedResponse(CommandMessage commandMessage) {
        storageClient.startAuthorization(context);
    }

    private void onAuthOkServerResponse(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        controller.updateStorageItemListInGUI(directoryMessage.getDirectory(),
                directoryMessage.getFileObjectsList());
    }

    private void onAuthErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onAuthErrorServerResponse() - Invalid login or password");
    }

    private void onFileObjectsListOkServerResponse(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        controller.updateStorageItemListInGUI(directoryMessage.getDirectory(),
                directoryMessage.getFileObjectsList());
    }

    private void onUploadFileOkServerResponse(CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        controller.updateStorageItemListInGUI(directoryMessage.getDirectory(),
                directoryMessage.getFileObjectsList());

        printMsg("[client]CommandHandlerClient.onUploadFileOkServerResponse() - " +
                "command: " + commandMessage.getCommand() +
                ". directoryMessage.getDirectory(): " + directoryMessage.getDirectory() +
                ". directoryMessage.getFileObjectsList(): " +
                Arrays.toString(directoryMessage.getFileObjectsList()));

    }

    private void onUploadFileErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onUploadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileOkServerResponse(CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        String storageDir = fileMessage.getFromDir();
        String clientDir = fileMessage.getToDir();

        String toDir = storageClient.getDefaultDirClient();
        toDir = toDir.concat("/").concat(clientDir);
        if(fileUtils.saveFile(toDir, fileMessage)){
            command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_OK;
        } else {
            printMsg("[client]" + fileUtils.getMsg());
            command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR;
        }
        fileMessage = new FileMessage(storageDir, clientDir, fileMessage.getFilename());

        context.writeAndFlush(new CommandMessage(command, fileMessage));
    }

    private void onDownloadFileErrorServerResponse(CommandMessage commandMessage) {
        printMsg("[client]CommandHandlerClient.onDownloadFileErrorServerResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileFragOkServerResponse(CommandMessage commandMessage) {
        FileFragmentMessage fileFragmentMessage = (FileFragmentMessage) commandMessage.getMessageObject();

        String toTempDir = storageClient.getDefaultDirClient();
        toTempDir = toTempDir.concat("/").concat(fileFragmentMessage.getToTempDir());
        String toDir = Paths.get(toTempDir).getParent().toString();

        if(fileUtils.saveFileFragment(toTempDir, fileFragmentMessage)){

        } else {
            printMsg("[client]" + fileUtils.getMsg());

        }
        if(fileFragmentMessage.isFinalFileFragment()){
            if(fileUtils.compileFileFragments(toTempDir, toDir, fileFragmentMessage)){
            } else {
                printMsg("[client]" + fileUtils.getMsg());
            }
        }
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