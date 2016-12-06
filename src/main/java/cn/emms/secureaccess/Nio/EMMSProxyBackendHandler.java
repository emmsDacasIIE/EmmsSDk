package cn.emms.secureaccess.Nio;
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import android.util.Log;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EMMSProxyBackendHandler extends ChannelInboundHandlerAdapter {
    private final Channel inboundChannel;
    //public Boolean waitOk = false;
	private static final String TAG = "SecureAccess";
	private static String WEB_SERVER;

    public EMMSProxyBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
		WEB_SERVER = EMMSProxy.WEB_SERVER;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx){
		Log.d(TAG, "BackEnd channel Active");
		/* ByteBuf addrInfoBuf = ctx.alloc().buffer(4*WEB_SERVER.length());
    	addrInfoBuf.writeBytes(WEB_SERVER.getBytes());
		Log.d(TAG, "BeckEnd send WEB IP to SA");
		ctx.channel().writeAndFlush(addrInfoBuf)
    		.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future){
				if(future.isSuccess()){
					EMMSProxy.waitOK = true;
					ctx.channel().read();

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else{
					//EMMSProxy.waitOK = false;
					future.channel().close();
					inboundChannel.close();
				}				
			}
		});*/
    	//waitOk = true;
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
    	/*if(EMMSProxy.waitOK){
    		// OK replay has arrived, so it needn't to wait ok;
			EMMSProxy.waitOK = false;
    		ByteBuf byteBuf = (ByteBuf)msg;
    		byte[] result = new byte[byteBuf.readableBytes()];
		
    		byteBuf.readBytes(result);
    		String replyOkString = new String(result);
    		// if the reply doesn't start with ok, the proxy fails.
    		if (replyOkString.startsWith("OK")) {
				Log.d(TAG, "SA says OK!");
				EMMSProxy.waitOK.notifyAll();
				ctx.channel().read();
    		}
			else {
				ctx.channel().close();
				EMMSProxyFrontendHandler.closeOnFlush(inboundChannel);
			}
    	}*/
    	
    	//else{
			Log.d(TAG, "BackEnd get Response: "+((ByteBuf)msg).readableBytes());
			inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
						Log.e(TAG, "Failed! BackEnd write to FrontEnd");
                        future.channel().close();
                    }
                }
            });	
    	//}
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        EMMSProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Log.e(TAG, "Back: "+cause.toString() );
		cause.printStackTrace();
        EMMSProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}
