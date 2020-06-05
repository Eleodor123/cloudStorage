import tcp.TCPClient;

import java.io.*;

public class Client {

    public static void main(String[] args) throws IOException {
        new TCPClient().send();
    }
}