package io.jooby.netty;

import io.jooby.WebServer;
import io.jooby.Router;
import io.jooby.internal.netty.NettyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.jooby.funzy.Throwing;

public class Netty implements WebServer {

  public static class Pipeline extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final NettyHandler handler;

    public Pipeline(SslContext sslCtx, DefaultEventExecutorGroup executor, Router router) {
      this.sslCtx = sslCtx;
      this.handler = new NettyHandler(executor, router);
    }

    @Override
    public void initChannel(SocketChannel ch) {
      ChannelPipeline p = ch.pipeline();
      if (sslCtx != null) {
        p.addLast(sslCtx.newHandler(ch.alloc()));
      }
      p.addLast(new HttpServerCodec());
      p.addLast(new HttpServerExpectContinueHandler());
      p.addLast(handler);
    }
  }

  private static boolean SSL = false;
  private NioEventLoopGroup bossGroup;
  private NioEventLoopGroup workerGroup;

  public void start(int port, Router router, boolean join) {
    // Configure SSL.
    final SslContext sslCtx;
    // Configure the server.
    this.bossGroup = new NioEventLoopGroup(1);
    this.workerGroup = new NioEventLoopGroup();
    try {
      if (SSL) {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
      } else {
        sslCtx = null;
      }
      DefaultEventExecutorGroup executor = new DefaultEventExecutorGroup(32);
      ServerBootstrap b = new ServerBootstrap();
      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .handler(new LoggingHandler(LogLevel.DEBUG))
          .childHandler(new Pipeline(sslCtx, executor, router));

      Channel ch = b.bind(port).sync().channel();

      ChannelFuture future = ch.closeFuture();
      if (join) {
        future.sync();
      }
    } catch (Throwable x) {
      throw Throwing.sneakyThrow(x);
    }
  }

  @Override public void stop() {
    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
  }
}
