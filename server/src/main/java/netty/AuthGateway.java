package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import messages.AuthMessage;
import utils.StorageServer;
import utils.CommandMessage;
import utils.Commands;
import utils.AuthorizationController;

import java.nio.file.Path;

public class AuthGateway extends ChannelInboundHandlerAdapter {
    private final StorageServer storageServer;
    private AuthorizationController authorizationController;
    private int command;

    public AuthGateway(StorageServer storageServer) {
        this.storageServer = storageServer;
        authorizationController = storageServer.getAuthorizationController();
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        context.writeAndFlush(new CommandMessage(Commands.SERVER_NOTIFICATION_CLIENT_CONNECTED));

        printMsg("[server]AuthGateway.channelActive() - context: " + context +
                ", command: " + Commands.SERVER_NOTIFICATION_CLIENT_CONNECTED);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        printMsg("[server]AuthGateway.channelInactive() - removed client(login): " +
                storageServer.getAuthorizedUsers().remove(context));

        printMsg("[server]AuthorizationController.authorizeUser - authorizedUsers: " +
                storageServer.getAuthorizedUsers().toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msgObject) {
        try {
            CommandMessage commandMessage = (CommandMessage) msgObject;
            if(commandMessage.getCommand() != Commands.REQUEST_SERVER_AUTH){
                return;
            }
            onAuthClientRequest(context, commandMessage);
        } finally {
            ReferenceCountUtil.release(msgObject);
        }
    }

    private void onAuthClientRequest(ChannelHandlerContext context, CommandMessage commandMessage) {
        AuthMessage authMessage = (AuthMessage) commandMessage.getMessageObject();
        printMsg("[server]AuthGateway.onAuthClientRequest() - " +
                "login: " + authMessage.getLogin() + ", password: " + authMessage.getPassword());
        if(authorizationController.authorizeUser(context, authMessage)){
            command = Commands.SERVER_RESPONSE_AUTH_OK;
            Path userStorageRoot = storageServer.getStorageRoot();
            userStorageRoot = userStorageRoot.resolve(authMessage.getLogin());
            context.fireChannelRead(new CommandMessage(command, userStorageRoot.toString()));
        } else {
            command = Commands.SERVER_RESPONSE_AUTH_ERROR;
            commandMessage = new CommandMessage(command, authMessage);
            context.writeAndFlush(commandMessage);
            printMsg("[server]AuthGateway.onAuthClientRequest() - context: " + context +
                    ", command: " + commandMessage.getCommand());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)  {
        cause.printStackTrace();
        context.close();
    }

    public void printMsg(String msg){
        storageServer.printMsg(msg);
    }
}