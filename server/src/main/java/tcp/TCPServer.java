package tcp;

import utils.CommandMessage;
import utils.handlers.ObjectHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;

public class TCPServer implements TCPConnectionListener {

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private final PrintStream log = System.out;
    private final String storageDir = "storage/server_storage";
    private final String clientDir = "storage/client_storage";
    private CommandMessage messageObject;
    private ObjectHandler objectHandler;

    public TCPServer() {
        printMsg("Server running...");
        objectHandler = new ObjectHandler(this);
        try(ServerSocket serverSocket = new ServerSocket(8190)){
            while(true){
                try{
                    new TCPConnection(this, serverSocket.accept());
                } catch(IOException e){
                    System.out.println("tcp.TCPConnection: " + e);
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnectionByte exception: " + e);
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

    public void sendToClient(String login, CommandMessage messageObject){

        printMsg("TCPServer.sendToClient() - login: " + login);

        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).sendMessageObject(messageObject);

        }
    }

    public synchronized void printMsg(String msg){
        log.append(msg).append("\n");
    }
}