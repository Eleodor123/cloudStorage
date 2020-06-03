import java.io.*;
import java.util.Arrays;

public class ClientByte implements TCPConnectionListenerByte {

    public static void main(String[] args) throws IOException {
        new ClientByte().send();
    }

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 8190;
    private final PrintStream log = System.out;
    private TCPConnectionByte connection;
    private File file;
    private File fileGraph;
    FileInputStream fis;
    BufferedInputStream bis;

    public ClientByte() throws FileNotFoundException {
//        file = new File("G:\\Study\\CloudStorage\\geekbrains-cloud-storage\\client\\src\\main\\resources\\files\\HW1.TXT");
        fileGraph = new File("G:\\Study\\CloudStorage\\geekbrains-cloud-storage\\client\\src\\main\\resources\\files\\spacemarine.png");
        fis = new FileInputStream(fileGraph);
        bis = new BufferedInputStream(fis);

        try {
            connection = new TCPConnectionByte(this, IP_ADDRESS, PORT);
        } catch (IOException e) {
            printMsg("Connection exception: " + e);
        }
    }

    public void send () throws IOException {
        readAndSendFile(fileGraph);
        printMsg("client sending bytes");
    }

    private void readAndSendFile(File file) {
        try (InputStream in = new BufferedInputStream(new
                FileInputStream(file))) {
            int x;
            while ((x = in.read()) != - 1 ) {
                connection.sendByte((byte)x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void printMsg(String msg){
        log.append(msg).append("\n");
    }

    @Override
    public void onConnectionReady(TCPConnectionByte tcpConnectionByte) {
        printMsg("Connection ready...");
    }

    @Override
    public void onDisconnect(TCPConnectionByte tcpConnectionByte) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnectionByte tcpConnectionByte, Exception e) {
        printMsg("Connection exception: " + e);
    }

    @Override
    public void onReceiveBytes(TCPConnectionByte tcpConnectionByte, byte... bytes) {
        System.out.println("Client input bytes array: " + Arrays.toString(bytes));
    }

    @Override
    public void onReceiveByte(TCPConnectionByte tcpConnectionByte, byte b) {
    }
}