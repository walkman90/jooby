package io.jooby.internal.netty;

import io.jooby.Context;
import io.jooby.Route;
import io.jooby.Router;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executor;

@ChannelHandler.Sharable
public class NettyHandler extends ChannelInboundHandlerAdapter {
  private static final AttributeKey<String> PATH = AttributeKey.valueOf("path");
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final DefaultEventExecutorGroup executor;
  private final Router router;

  public NettyHandler(DefaultEventExecutorGroup executor, Router router) {
    this.executor = executor;
    this.router = router;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
    if (msg instanceof HttpRequest) {
      HttpRequest req = (HttpRequest) msg;
      String path = Route.normalize(req.uri());
      Iterator<Route> iterator = router.iterator(req.method().name(), path);
      NettyContext context = new NettyContext(ctx, req, isKeepAlive(req), path, iterator);
      Route.Chain chain = context.chain();
      ctx.channel().attr(PATH).set(path);
      try {
        log.info("{}", path);
        chain.next(context);
      } catch (Context.Dispatched dispatched) {
        dispatch(dispatched.executor, context, chain, iterator);
      } catch (Throwable x) {
        exceptionCaught(ctx, x);
      }
    }
  }

  private void dispatch(Executor executor, Context context, Route.Chain chain, Iterator<Route> iterator) {
    Executor exec = executor == null ? this.executor : executor;
    exec.execute(() -> {
      try {
        // ctx.dispatch comes from handler so we are safe to keep existing handler and move from there
        iterator.next().handler().handle(context, chain);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    String path = ctx.channel().attr(PATH).get();
    log.error("execution of {} resulted in exception", path, cause);
    ctx.close();
  }
}

