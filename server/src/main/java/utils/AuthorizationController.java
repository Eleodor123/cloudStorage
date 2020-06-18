package utils;

import io.netty.channel.ChannelHandlerContext;
import jdbc.DBConf;
import messages.AuthMessage;

public class AuthorizationController {
    private StorageServer storageServer;

    public AuthorizationController(StorageServer storageServer) {
        this.storageServer = storageServer;
    }

    public boolean authorizeUser(ChannelHandlerContext context, AuthMessage authMessage){
        if(isUserAuthorized(context, authMessage.getLogin())){
            printMsg("[server]AuthorizationController.authorizeUser - This user has been authorised already!");
            return false;
        }

        if(checkLoginAndPassword(authMessage.getLogin(), authMessage.getPassword())){
            storageServer.getAuthorizedUsers().put(context, authMessage.getLogin());

            printMsg("[server]AuthorizationController.authorizeUser - authorizedUsers: " +
                    storageServer.getAuthorizedUsers().toString());

            return true;
        }
        return false;
    }

    private boolean isUserAuthorized(ChannelHandlerContext ctx, String login) {
        return storageServer.getAuthorizedUsers().containsKey(ctx) ||
                storageServer.getAuthorizedUsers().containsValue(login);
    }

    private boolean checkLoginAndPassword(String login, String password) {
        for (int i = 0; i < DBConf.users.length; i++) {
            if(login.equals(DBConf.users[i][0]) && password.equals(DBConf.users[i][1])){
                return true;
            }
        }
        return false;
    }

    public void printMsg(String msg){
        storageServer.printMsg(msg);
    }
}