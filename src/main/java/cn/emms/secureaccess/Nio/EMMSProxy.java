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

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EMMSProxy {

    private final int LOCAL_PORT ;
    private String REMOTE_HOST = System.getProperty("remoteHost", "emms.csrcqsf.com");
    private int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "43546"));
    static  String WEB_SERVER;
    static final String TAG = "SecureAccess";
    private static boolean https = true;
    static public Boolean waitOK = true;

    private  EventLoopGroup bossGroup,workerGroup;

    public void setRemoteHost(String ip, int port){
        this.REMOTE_HOST = ip;
        this.REMOTE_PORT = port;
    }

    public static void setHttps(boolean flag){
        https = flag;
    }

    public static boolean isHttps(){
        return https;
    }

    public EMMSProxy(int localPort, String webAddr){
        LOCAL_PORT = localPort;
        WEB_SERVER = webAddr;
    }

    public void startWork(){
        Log.e(TAG, "Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT + " ...");

        // Configure the bootstrap.
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new EMMSProxyInitializer(REMOTE_HOST, REMOTE_PORT))
                            .childOption(ChannelOption.AUTO_READ, false)
                            .bind(LOCAL_PORT).sync().channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    Log.e(TAG, "run: ", e);
                } catch (SSLException e) {
                    Log.e(TAG, "run: ", e);
                } finally {
                    clearProxy();
                }
            }
        }).start();
    }

    public void clearProxy(){
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
    }
}
