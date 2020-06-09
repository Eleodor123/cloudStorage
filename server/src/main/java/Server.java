//import old.tcp.TCPServer;
import utils.StorageServer;

public class Server {
//    public static void main(String[] args) {
//        new TCPServer();
//    }

    public static void main(String[] args) throws Exception {
        new StorageServer().run();
    }
}
