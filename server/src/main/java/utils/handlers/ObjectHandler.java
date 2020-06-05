package utils.handlers;

import messages.AuthMessage;
import messages.Commands;
import messages.FileMessage;
import tcp.TCPServer;
import utils.CommandMessage;

public class ObjectHandler {
    TCPServer server;
    FileMessage fileMessage;
    FileCommandHandler fileCommandHandler;
    String clientDir;
    String storageDir;
    AuthMessage authMessage;
    ServiceCommandHandler serviceCommandHandler;

    public ObjectHandler(TCPServer server) {
        this.server = server;
    }

    public void recognizeAndArrangeMessageObject(CommandMessage messageObject) {
        switch (messageObject.getCommand()) {
            case Commands.REQUEST_SERVER_FILE_UPLOAD:
                uploadFile(messageObject);
                break;
            case Commands.CLIENT_RESPONSE_FILE_UPLOAD_OK:
                respondOnUploadFileOK(messageObject);
                break;
            case Commands.CLIENT_RESPONSE_FILE_UPLOAD_ERROR:
                respondOnUploadFileError(messageObject);
                break;
            case Commands.REQUEST_SERVER_FILE_DOWNLOAD:
                downloadFile(messageObject);
                break;
            case Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_OK:
                respondOnDownloadFileOK(messageObject);
                break;
            case Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR:
                respondOnDownloadFileError(messageObject);
                break;
        }

    }

    private void uploadFile(CommandMessage messageObject) {
        fileMessage = (FileMessage) messageObject.getMessageObject();
        fileCommandHandler = new FileCommandHandler(fileMessage);
        clientDir = fileMessage.getFromDir();
        storageDir = fileMessage.getToDir();
        int command = Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR;
        if(fileCommandHandler.saveUploadedFile(server, storageDir, fileMessage)){
            if(true){
                command = Commands.SERVER_RESPONSE_FILE_UPLOAD_OK;
            }
        }
        fileMessage = new FileMessage(storageDir, clientDir, fileMessage.getFilename());
        server.sendToClient("login1", new CommandMessage(command, fileMessage));
    }

    private void respondOnUploadFileOK(CommandMessage messageObject) {
        server.printMsg("Server.respondOnUploadFileOK command: " + messageObject.getCommand());
    }

    private void respondOnUploadFileError(CommandMessage messageObject) {
        server.printMsg("Server.respondOnUploadFileError command: " + messageObject.getCommand());
    }

    private void downloadFile(CommandMessage messageObject) {
        fileMessage = (FileMessage) messageObject.getMessageObject();
        fileCommandHandler = new FileCommandHandler(fileMessage);
        storageDir = fileMessage.getFromDir();
        clientDir = fileMessage.getToDir();
        int command = Commands.SERVER_RESPONSE_FILE_DOWNLOAD_ERROR;
        fileMessage = new FileMessage(storageDir, clientDir, fileMessage.getFilename());
        if(fileCommandHandler.downloadFile(server, fileMessage)){
            if(true){
                command = Commands.SERVER_RESPONSE_FILE_DOWNLOAD_OK;
            }
        }
        server.sendToClient("login1", new CommandMessage(command, fileMessage));
    }

    private void respondOnDownloadFileOK(CommandMessage messageObject) {
        server.printMsg("Server.respondOnDownloadFileOK command: " + messageObject.getCommand());
    }

    private void respondOnDownloadFileError(CommandMessage messageObject) {
        server.printMsg("Server.respondOnDownloadFileError command: " + messageObject.getCommand());
    }
}