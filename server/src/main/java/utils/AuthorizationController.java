package utils;

import io.netty.channel.ChannelHandlerContext;
import jdbc.DBTemp;
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

    private boolean isUserAuthorized(ChannelHandlerContext context, String login) {
        return storageServer.getAuthorizedUsers().containsKey(context) ||
                storageServer.getAuthorizedUsers().containsValue(login);
    }

    private boolean checkLoginAndPassword(String login, String password) {
        for (int i = 0; i < DBTemp.users.length; i++) {
            if(login.equals(DBTemp.users[i][0]) && password.equals(DBTemp.users[i][1])){
                return true;
            }
        }
        return false;
    }

    public void printMsg(String msg){
        storageServer.printMsg(msg);
    }
}