package io.jooby.internal.netty;

import io.jooby.netty.Netty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public interface NettyConfigurer {
  EventLoopGroup group(int threads);

  Class<? extends ServerSocketChannel> channel();

  default void configure(ServerBootstrap bootstrap) {
    // NOOP
  }

  static NettyConfigurer get() {
    /** Epoll: */
    if (Epoll.isAvailable()) {
      return new NettyConfigurer() {
        @Override public EventLoopGroup group(int threads) {
          return new EpollEventLoopGroup(threads);
        }

        @Override public Class<? extends ServerSocketChannel> channel() {
          return EpollServerSocketChannel.class;
        }

        @Override public void configure(ServerBootstrap bootstrap) {
          bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
        }
      };
    }
    if (KQueue.isAvailable()) {
      return new NettyConfigurer() {
        @Override public EventLoopGroup group(int threads) {
          return new KQueueEventLoopGroup(threads);
        }

        @Override public Class<? extends ServerSocketChannel> channel() {
          return KQueueServerSocketChannel.class;
        }
      };
    }
    return new NettyConfigurer() {
      @Override public EventLoopGroup group(int threads) {
        return new NioEventLoopGroup(threads);
      }

      @Override public Class<? extends ServerSocketChannel> channel() {
        return NioServerSocketChannel.class;
      }
    };
  }
}
