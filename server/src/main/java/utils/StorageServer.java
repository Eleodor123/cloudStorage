package utils;

import io.netty.channel.ChannelHandlerContext;
import netty.NettyServer;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StorageServer {
    private final int PORT = 8190;
    private final PrintStream log = System.out;
    private final Path storageRoot = Paths.get("storage","server_storage");
    private Map<ChannelHandlerContext, String> authorizedUsers;
    private AuthorizationController authorizationController;
    private FileUtils fileUtils;

    public void run() throws Exception {
        authorizedUsers = new HashMap<>();
        authorizationController = new AuthorizationController(this);
        fileUtils = new FileUtils();
        new NettyServer(this, PORT).run();
    }

    public Path getStorageRoot() {
        return storageRoot;
    }

    public Map<ChannelHandlerContext, String> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public AuthorizationController getAuthorizationController() {
        return authorizationController;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public void printMsg(String msg){
        log.append(msg).append("\n");
    }
}