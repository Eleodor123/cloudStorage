package utils.handlers;

import messages.AuthMessage;
import messages.Commands;
import messages.FileMessage;
import tcp.TCPClient;
import utils.CommandMessage;

public class ObjectHandler {
    TCPClient client;
    FileMessage fileMessage;
    FileCommandHandler fileCommandHandler;
    String clientDir;
    String storageDir;
    AuthMessage authMessage;
    ServiceCommandHandler serviceCommandHandler;

    public ObjectHandler(TCPClient client) {
        this.client = client;
    }

    public void recognizeAndArrangeMessageObject(CommandMessage messageObject) {
        switch (messageObject.getCommand()) {
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_OK:
                respondOnUploadFileOK(messageObject);
                break;
            case Commands.SERVER_RESPONSE_FILE_UPLOAD_ERROR:
                respondOnUploadFileError(messageObject);
                break;
            case Commands.SERVER_RESPONSE_FILE_DOWNLOAD_OK:
                respondOnDownloadFileOK(messageObject);
                break;
            case Commands.SERVER_RESPONSE_FILE_DOWNLOAD_ERROR:
                respondOnDownloadFileError(messageObject);
                break;
        }
    }

    private void respondOnUploadFileOK(CommandMessage messageObject) {
        client.printMsg("Client.respondOnUploadFileOK command: " + messageObject.getCommand());
    }

    private void respondOnUploadFileError(CommandMessage messageObject) {
        client.printMsg("Client.respondOnUploadFileError command: " + messageObject.getCommand());
    }

    private void respondOnDownloadFileOK(CommandMessage messageObject) {
        fileMessage = (FileMessage) messageObject.getMessageObject();
        fileCommandHandler = new FileCommandHandler(fileMessage);
        storageDir = fileMessage.getFromDir();
        clientDir = fileMessage.getToDir();
        int command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_ERROR;
        if(fileCommandHandler.saveDownloadedFile(client, clientDir, fileMessage)){
            if(true){
                command = Commands.CLIENT_RESPONSE_FILE_DOWNLOAD_OK;
            }
        }
        fileMessage = new FileMessage(storageDir, clientDir, fileMessage.getFilename());
        client.getConnection().sendMessageObject(new CommandMessage(command, fileMessage));
    }

    private void respondOnDownloadFileError(CommandMessage messageObject) {
        client.printMsg("Client.respondOnDownloadFileError command: " + messageObject.getCommand());
    }
}