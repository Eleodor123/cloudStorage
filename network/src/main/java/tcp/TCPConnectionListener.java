package tcp;

import java.io.ObjectInputStream;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection tcpConnection);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception e);
    void onReceiveObject(TCPConnection tcpConnection, ObjectInputStream ois);
}
