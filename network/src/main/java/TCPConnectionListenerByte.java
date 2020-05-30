import java.io.BufferedInputStream;

public interface TCPConnectionListenerByte {
    void onConnectionReady(TCPConnectionByte tcpConnectionByte);
    void onDisconnect(TCPConnectionByte tcpConnectionByte);
    void onException(TCPConnectionByte tcpConnectionByte, Exception e);

    void onReceiveBytes(TCPConnectionByte tcpConnectionByte, byte... bytes);
    void onReceiveByte(TCPConnectionByte tcpConnectionByte, byte b);

}