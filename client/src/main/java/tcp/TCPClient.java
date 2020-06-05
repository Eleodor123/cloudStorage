package tcp;

import messages.Commands;
import messages.FileMessage;
import utils.CommandMessage;
import utils.handlers.ObjectHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;

public class TCPClient implements TCPConnectionListener {
    private static final String IP_ADDR = "127.0.0.1";
    private static final int PORT = 8190;
    private final PrintStream log = System.out;
    private TCPConnection connection;
    private final String storageDir = "storage/server_storage";
    private final String clientDir = "storage/client_storage";
    private CommandMessage messageObject;
    private ObjectHandler objectHandler;

    public TCPClient() {
        objectHandler = new ObjectHandler(this);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    public void send () {
        uploadFile(clientDir, storageDir, "file1.txt");
        downloadFile(storageDir, clientDir, "img.png");
    }

    public void uploadFile(String fromDir, String toDir, String filename){
        FileMessage fileMessage = new FileMessage(fromDir, toDir, filename);
        try {
            fileMessage.readFileData();
        } catch (IOException e) {
            printMsg("There is no file in the directory!");
            e.printStackTrace();
        }
        connection.sendMessageObject(new CommandMessage(Commands.REQUEST_SERVER_FILE_UPLOAD,
                fileMessage));
    }

    public void downloadFile(String fromDir, String toDir, String filename){
        FileMessage fileMessage = new FileMessage(fromDir, toDir, filename);
        connection.sendMessageObject(new CommandMessage(Commands.REQUEST_SERVER_FILE_DOWNLOAD,
                fileMessage));
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    @Override
    public void onReceiveObject(TCPConnection tcpConnection, ObjectInputStream ois) {
        try {
            messageObject = (CommandMessage) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        objectHandler.recognizeAndArrangeMessageObject(messageObject);
    }

    public TCPConnection getConnection() {
        return connection;
    }

    public synchronized void printMsg(String msg){
        log.append(msg).append("\n");
    }
}
