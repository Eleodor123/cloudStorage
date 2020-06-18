package netty;

import control.StorageControl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClient {
    private StorageControl StorageControl;
    private final String IP_ADDR;
    private final int PORT;

    public NettyClient(StorageControl StorageControl, String IP_ADDR, int PORT) {
        this.StorageControl = StorageControl;
        this.IP_ADDR = IP_ADDR;
        this.PORT = PORT;
    }

    public void run() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class);
                    b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(
                    new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                    new ObjectEncoder(),
                    new CommandHandlerClient(StorageControl)
                    );
                }
            });
            ChannelFuture future = b.connect(IP_ADDR, PORT).sync();

            onConnectionReady(future);

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void onConnectionReady(ChannelFuture future) {
        printMsg("Waiting for the server answer...");
    }

    public void printMsg(String msg){
        StorageControl.printMsg(msg);
    }
}
