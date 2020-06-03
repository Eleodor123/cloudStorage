import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPConnectionByte {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListenerByte eventListenerByte;

    private final BufferedOutputStream outCom;

    public TCPConnectionByte(TCPConnectionListenerByte eventListenerByte, String ipAddress, int port) throws IOException {
        this(eventListenerByte, new Socket(ipAddress, port));
    }

    public TCPConnectionByte(final TCPConnectionListenerByte eventListenerByte, Socket socket) throws IOException {
        this.eventListenerByte = eventListenerByte;
        this.socket = socket;

        final DataInputStream dis = new DataInputStream(socket.getInputStream());
        final Byte[] inComByte = new Byte[1];
        outCom = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListenerByte.onConnectionReady(TCPConnectionByte.this);
                    while(!rxThread.isInterrupted()){

                        eventListenerByte.onReceiveByte(TCPConnectionByte.this, dis.readByte());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    eventListenerByte.onDisconnect(TCPConnectionByte.this);
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendMessageObject(byte [] arr){
        try {
            outCom.write(arr);
            outCom.flush();
        } catch (IOException e) {
            eventListenerByte.onException(TCPConnectionByte.this, e);
            disconnect();
        }
    }

    public synchronized void sendByte(byte b){
        try {
            outCom.write(b);
            outCom.flush();
        } catch (IOException e) {
            eventListenerByte.onException(TCPConnectionByte.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListenerByte.onException(TCPConnectionByte.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPConnectionByte: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}