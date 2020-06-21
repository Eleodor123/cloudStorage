package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import messages.AuthMessage;
import utils.AuthorizationController;
import utils.StorageServer;
import utils.CommandMessage;
import utils.Commands;

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
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new CommandMessage(Commands.SERVER_NOTIFICATION_CLIENT_CONNECTED));

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        printMsg("[server]AuthGateway.channelInactive() - removed client(login): " +
                storageServer.getAuthorizedUsers().remove(ctx));

        printMsg("[server]AuthorizationController.authorizeUser - authorizedUsers: " +
                storageServer.getAuthorizedUsers().toString());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObject) {
        try {
            CommandMessage commandMessage = (CommandMessage) msgObject;
            if(commandMessage.getCommand() != Commands.SERVER_REQUEST_AUTH){
                return;
            }
            onAuthClientRequest(ctx, commandMessage);
        } finally {
            ReferenceCountUtil.release(msgObject);
        }
    }

    private void onAuthClientRequest(ChannelHandlerContext ctx, CommandMessage commandMessage) {
        AuthMessage authMessage = (AuthMessage) commandMessage.getMessageObject();

        if(authorizationController.authorizeUser(ctx, authMessage)){
            command = Commands.SERVER_RESPONSE_AUTH_OK;

            Path userStorageRoot = storageServer.getSTORAGE_ROOT_PATH();
            userStorageRoot = userStorageRoot.resolve(authMessage.getLogin());
            ctx.fireChannelRead(new CommandMessage(command, userStorageRoot.toString()));
        } else {
            command = Commands.SERVER_RESPONSE_AUTH_ERROR;
            commandMessage = new CommandMessage(command, authMessage);
            ctx.writeAndFlush(commandMessage);

            printMsg("[server]AuthGateway.onAuthClientRequest() - ctx: " + ctx +
                    ", command: " + commandMessage.getCommand());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void printMsg(String msg){
        storageServer.printMsg(msg);
    }
}