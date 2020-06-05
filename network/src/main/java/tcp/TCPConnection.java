package tcp;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;

    ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(final TCPConnectionListener eventListener, final Socket socket) {
        this.eventListener = eventListener;
        this.socket = socket;

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!rxThread.isInterrupted()){
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                        eventListener.onReceiveObject(TCPConnection.this, objectInputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendMessageObject(Object messageObject){
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(messageObject);

            System.out.println("TCPConnection.sendMessageObject() - " + socket + " has sent the object: " +
                    messageObject.getClass().getSimpleName() +
                    ": " + Arrays.toString(messageObject.getClass().getFields()));

        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {

            objectInputStream.close();
            objectOutputStream.close();

            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPConnectionByte: " + socket.getInetAddress() + ": " + socket.getPort();
    }

    public Socket getSocket() {
        return socket;
    }
}