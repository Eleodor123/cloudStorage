package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import messages.DirectoryMessage;
import messages.FileFragmentMessage;
import messages.FileMessage;
import utils.StorageServer;
import utils.CommandMessage;
import utils.Commands;
import utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandHandlerServer extends ChannelInboundHandlerAdapter {
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
        CommandMessage commandMessage = (CommandMessage) msg;
        switch (commandMessage.getCommand()) {
            case Commands.REQUEST_SERVER_FILE_OBJECTS_LIST:
                onFileObjectsListClientRequest(context, commandMessage);
                break;
            case Commands.REQUEST_SERVER_FILE_UPLOAD:
                onUploadFileClientRequest(context, commandMessage);
                break;
            case Commands.CLIENT_RESPONSE_FILE_UPLOAD_OK:
                onUploadFileOkClientResponse(context, commandMessage);
                break;
            case Commands.CLIENT_RESPONSE_FILE_UPLOAD_ERROR:
                onUploadFileErrorClientResponse(context, commandMessage);
                break;
            case Commands.REQUEST_SERVER_FILE_DOWNLOAD:
                onDownloadFileClientRequest(context, commandMessage);
                break;
            case Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_OK:
                onDownloadFileOkClientResponse(context, commandMessage);
                break;
            case Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR:
                onDownloadFileErrorClientResponse(context, commandMessage);
                break;
            case Commands.REQUEST_SERVER_FILE_FRAG_UPLOAD:
                onUploadFileFragClientRequest(context, commandMessage);
                break;
            case Commands.SERVER_RESPONSE_AUTH_OK:
                onAuthClientRequest(context, commandMessage);
                break;
        }
    }

    private void onFileObjectsListClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) {
        DirectoryMessage directoryMessage = (DirectoryMessage) commandMessage.getMessageObject();
        Path storageDir = Paths.get(userStorageRoot.toString());
        storageDir = storageDir.resolve(Paths.get(directoryMessage.getDirectory()));
        directoryMessage.takeFileObjectsList(storageDir.toString());
        command = Commands.SERVER_RESPONSE_FILE_OBJECTS_LIST_OK;
        context.writeAndFlush(new CommandMessage(command, directoryMessage));
    }

    private void onUploadFileClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        String storageDir = fileMessage.getToDir();
        String toDir = userStorageRoot.toString();
        toDir = toDir.concat("/").concat(storageDir);

        if(fileUtils.saveFile(toDir, fileMessage)){
            command = Commands.SERVER_RESPONSE_FILE_UPLOAD_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR;
        }
        DirectoryMessage directoryMessage = new DirectoryMessage(storageDir);
        directoryMessage.takeFileObjectsList(toDir);
        context.writeAndFlush(new CommandMessage(command, directoryMessage));
    }

    private void onUploadFileOkClientResponse(ChannelHandlerContext context, CommandMessage commandMessage) {
        printMsg("[server]CommandHandlerServer.onUploadFileOkClientResponse() command: " + commandMessage.getCommand());
    }

    private void onUploadFileErrorClientResponse(ChannelHandlerContext context, CommandMessage commandMessage) {
        printMsg("[server]CommandHandlerServer.onUploadFileErrorClientResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) throws IOException {
        FileMessage fileMessage = (FileMessage) commandMessage.getMessageObject();
        String storageDir = fileMessage.getFromDir();
        String clientDir = fileMessage.getToDir();
        Path fromDirPath = userStorageRoot;
        fromDirPath = fromDirPath.resolve(storageDir);
        String fromDir = fromDirPath.toString();
        long fileSize = Files.size(Paths.get(fromDir, fileMessage.getFilename()));
        if(fileSize > FileFragmentMessage.CONST_FRAG_SIZE){
            downloadFileByFrags(context, fromDir, clientDir,
                    fileMessage.getFilename(), fileSize);
        } else {
            downloadEntireFile(context, fromDir, clientDir, fileMessage.getFilename());
        }
    }

    private void onDownloadFileOkClientResponse(ChannelHandlerContext context, CommandMessage commandMessage) {
        printMsg("[server]CommandHandlerServer.onDownloadFileOkClientResponse() command: " + commandMessage.getCommand());
    }

    private void onDownloadFileErrorClientResponse(ChannelHandlerContext context, CommandMessage commandMessage) {
        printMsg("[server]CommandHandlerServer.onDownloadFileErrorClientResponse() command: " + commandMessage.getCommand());
    }

    private void onUploadFileFragClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) {
        FileFragmentMessage fileFragmentMessage = (FileFragmentMessage) commandMessage.getMessageObject();
        String toTempDir = userStorageRoot.toString();
        toTempDir = toTempDir.concat("/").concat(fileFragmentMessage.getToTempDir());
        String toDir = Paths.get(toTempDir).getParent().toString();
        String directory = toTempDir;
        if(fileUtils.saveFileFragment(toTempDir, fileFragmentMessage)){
            command = Commands.SERVER_RESPONSE_FILE_FRAG_UPLOAD_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_FILE_FRAG_UPLOAD_ERROR;
        }
        if(fileFragmentMessage.isFinalFileFragment()){
            if(fileUtils.compileFileFragments(toTempDir, toDir, fileFragmentMessage)){
                command = Commands.SERVER_RESPONSE_FILE_FRAGS_UPLOAD_OK;
                directory = toDir;
            } else {
                printMsg("[server]" + fileUtils.getMsg());
                command = Commands.SERVER_RESPONSE_FILE_FRAGS_UPLOAD_ERROR;
            }
        }
        DirectoryMessage directoryMessage = new DirectoryMessage(directory);
        directoryMessage.takeFileObjectsList(directory);
        context.writeAndFlush(new CommandMessage(command, directoryMessage));
    }

    private void downloadFileByFrags(
            ChannelHandlerContext context,
            String fromDir,
            String toDir,
            String filename,
            long fullFileSize) throws IOException {
        long start = System.currentTimeMillis();
        int totalEntireFragsNumber = (int) fullFileSize / FileFragmentMessage.CONST_FRAG_SIZE;
        int finalFileFragmentSize = (int) fullFileSize - FileFragmentMessage.CONST_FRAG_SIZE * totalEntireFragsNumber;
        int totalFragsNumber = (finalFileFragmentSize == 0) ? totalEntireFragsNumber : totalEntireFragsNumber + 1;

        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - fullFileSize: " + fullFileSize);
        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - totalFragsNumber: " + totalFragsNumber);
        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - totalEntireFragsNumber: " + totalEntireFragsNumber);

        long startByte = 0;
        byte[] data = new byte[FileFragmentMessage.CONST_FRAG_SIZE];
        String[] fragsNames = new String[totalFragsNumber];
        for (int i = 1; i <= totalEntireFragsNumber; i++) {
            FileFragmentMessage fileFragmentMessage =
                    new FileFragmentMessage(fromDir, toDir, filename, fullFileSize,
                            i, totalFragsNumber, FileFragmentMessage.CONST_FRAG_SIZE, fragsNames, data);
            fileFragmentMessage.readFileDataToFragment(fromDir, filename, startByte);
            startByte += FileFragmentMessage.CONST_FRAG_SIZE;
            context.writeAndFlush(new CommandMessage(Commands.SERVER_RESPONSE_FILE_FRAGS_DOWNLOAD_OK,
                    fileFragmentMessage));
        }

        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - currentFragNumber: " + totalFragsNumber);
        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - finalFileFragmentSize: " + finalFileFragmentSize);

        if(totalFragsNumber > totalEntireFragsNumber){
            byte[] dataFinal = new byte[finalFileFragmentSize];
            FileFragmentMessage fileFragmentMessage =
                    new FileFragmentMessage(fromDir, toDir, filename, fullFileSize,
                            totalFragsNumber, totalFragsNumber, finalFileFragmentSize, fragsNames, dataFinal);
            fileFragmentMessage.readFileDataToFragment(fromDir, filename, startByte);
            context.writeAndFlush(new CommandMessage(Commands.SERVER_RESPONSE_FILE_FRAGS_DOWNLOAD_OK,
                    fileFragmentMessage));
        }
        long finish = System.currentTimeMillis() - start;
        printMsg("[server]CommandHandlerServer.downloadFileByFrags() - duration(mc): " + finish);
    }

    private void downloadEntireFile(ChannelHandlerContext context, String fromDir, String clientDir, String filename){
        FileMessage fileMessage = new FileMessage(fromDir, clientDir, filename);
        if(fileUtils.readFile(fromDir, fileMessage)){
            command = Commands.SERVER_RESPONSE_FILE_DOWNLOAD_OK;
        } else {
            printMsg("[server]" + fileUtils.getMsg());
            command = Commands.SERVER_RESPONSE_FILE_DOWNLOAD_ERROR;
        }
        context.writeAndFlush(new CommandMessage(command, fileMessage));
    }

    private void onAuthClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) {
        command = commandMessage.getCommand();
        userStorageRoot = Paths.get(commandMessage.getDirectory());
        DirectoryMessage directoryMessage = new DirectoryMessage("");
        directoryMessage.takeFileObjectsList(userStorageRoot.toString());
        commandMessage = new CommandMessage(command, directoryMessage);
        context.writeAndFlush(commandMessage);
        printMsg("[server]CommandHandlerServer.onAuthClientRequest() - " +
                "removed pipeline: " + context.channel().pipeline().remove(AuthGateway.class));
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