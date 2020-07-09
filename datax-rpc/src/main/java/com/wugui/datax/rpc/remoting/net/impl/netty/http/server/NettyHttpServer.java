package com.wugui.datax.rpc.remoting.net.impl.netty.http.server;

import com.wugui.datax.rpc.remoting.net.common.NettyConstant;
import com.wugui.datax.rpc.remoting.net.impl.AbstractBaseServer;
import com.wugui.datax.rpc.remoting.net.params.Beat;
import com.wugui.datax.rpc.remoting.provider.XxlRpcProviderFactory;
import com.wugui.datax.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty_http
 *
 * @author xuxueli 2015-11-24 22:25:15
 */
public class NettyHttpServer extends AbstractBaseServer {

    @Override
    protected void startServer(XxlRpcProviderFactory xxlRpcProviderFactory) {
        // param
        final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                NettyHttpServer.class.getSimpleName(),
                xxlRpcProviderFactory.getCorePoolSize(),
                xxlRpcProviderFactory.getMaxPoolSize());
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(NettyConstant.MAX_LENGTH))  // merge request & reponse to FULL
                                    .addLast(new NettyHttpServerHandler(xxlRpcProviderFactory, serverHandlerPool));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // bind
            ChannelFuture future = bootstrap.bind(xxlRpcProviderFactory.getPort()).sync();

            logger.info(">>>>>>>>>>> xxl-rpc remoting server start success, nettype = {}, port = {}", NettyHttpServer.class.getName(), xxlRpcProviderFactory.getPort());
            onStarted();

            // wait util stop
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                logger.info(">>>>>>>>>>> xxl-rpc remoting server stop.");
            } else {
                logger.error(">>>>>>>>>>> xxl-rpc remoting server error.", e);
            }
        } finally {

            // stop
            try {
                serverHandlerPool.shutdown();    // shutdownNow
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

}
