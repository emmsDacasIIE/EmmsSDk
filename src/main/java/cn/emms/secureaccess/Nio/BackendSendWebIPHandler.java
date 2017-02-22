package cn.emms.secureaccess.Nio;

import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.ssl.SslContext;

/**
 * Created by Sun RX on 2016-10-18.
 */
@Deprecated
public class BackendSendWebIPHandler extends ChannelInboundHandlerAdapter{
    private final Channel inboundChannel;
    private static String WEB_SERVER;
    private final String remoteHost;
    private final int remotePort;
    private SslContext sslContext;

    public BackendSendWebIPHandler(Channel inboundChannel,SslContext sslContext,String remoteHost, int remotePort) {
        this.inboundChannel = inboundChannel;
        WEB_SERVER = EMMSProxy.WEB_SERVER;
        this.sslContext = sslContext;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ByteBuf addrInfoBuf = ctx.alloc().buffer(4*WEB_SERVER.length());
        addrInfoBuf.writeBytes(WEB_SERVER.getBytes());
        Log.d(EMMSProxy.TAG, "BeckEnd send WEB IP to SA");
        ctx.channel().writeAndFlush(addrInfoBuf)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future){
                        if(future.isSuccess()){
                            EMMSProxy.waitOK = true;
                            ctx.channel().read();
                        }
                        else{
                            Log.e(EMMSProxy.TAG, "Send WEN IP Failed!");
                            future.channel().close();
                            inboundChannel.close();
                        }
                    }
                });
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // OK replay has arrived, so it needn't to wait ok;
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] result = new byte[byteBuf.readableBytes()];

        byteBuf.readBytes(result);
        String replyOkString = new String(result);
        Log.d(EMMSProxy.TAG, "SA says "+replyOkString);
        // if the reply doesn't start with ok, the proxy fails.
        if (replyOkString.startsWith("OK")) {

            Log.d(EMMSProxy.TAG, "remove WaitOk Handler & Add SSL");
            ctx.pipeline().remove("waitOK");
            if (EMMSProxy.isHttps()) {
                Channel ch = ctx.channel();
                ch.pipeline().addFirst("ssl", sslContext.newHandler(ch.alloc(), remoteHost, remotePort));
            }
            EMMSProxy.waitOK = false;
            ctx.channel().read();
        } else {
            ctx.channel().close();
            EMMSProxyFrontendHandler.closeOnFlush(inboundChannel);
        }
    }
}
