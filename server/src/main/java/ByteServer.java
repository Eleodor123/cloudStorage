import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class ByteServer implements TCPConnectionListenerByte {//создаем слушателя прямо в этом классе

    public static void main(String[] args) throws IOException {
        new ByteServer();
    }

    private final ArrayList<TCPConnectionByte> connections = new ArrayList<>();
    private File file;
    private File fileGraph;
    FileOutputStream fos;
    BufferedOutputStream bos;

    private ByteServer() throws IOException {

        fileGraph = new File("G:\\Study\\CloudStorage\\geekbrains-cloud-storage\\server\\src\\main\\resources\\files\\spacemarine.png");
        fileGraph.createNewFile();
        fos = new FileOutputStream(fileGraph);
        bos = new BufferedOutputStream(fos);

        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8190)){//это "try с ресурсом"
            while(true){
                try{
                    new TCPConnectionByte(this, serverSocket.accept());
                } catch(IOException e){
                    System.out.println("TCPConnection: " + e);
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        } finally {
            fos.close();
            bos.close();
        }
    }

    @Override
    public void onConnectionReady(TCPConnectionByte tcpConnectionByte) {
        connections.add(tcpConnectionByte);
        sendToAllConnections("ClientByte connected: " + tcpConnectionByte);
    }

    @Override
    public void onDisconnect(TCPConnectionByte tcpConnectionByte) {
        connections.remove(tcpConnectionByte);
        sendToAllConnections("ClientByte disconnected: " + tcpConnectionByte);
    }

    @Override
    public void onException(TCPConnectionByte tcpConnectionByte, Exception e) {
        System.out.println("TCPConnectionByte exception: " + e);
    }

    @Override
    public void onReceiveBytes(TCPConnectionByte tcpConnectionByte, byte... bytes) {
        System.out.println("Server input bytes array: " + Arrays.toString(bytes));

    }

    @Override
    public void onReceiveByte(TCPConnectionByte tcpConnectionByte, byte b) {
        try {
            bos.write(b);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToAllConnections(String value){
        System.out.println(value);
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
        }
    }
}