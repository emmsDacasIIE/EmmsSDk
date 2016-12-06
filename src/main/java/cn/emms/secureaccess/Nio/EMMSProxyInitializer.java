package cn.emms.secureaccess.Nio;/*
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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class EMMSProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;
    private SslContext sslContext;
    //private SSLContext sslContext;

    public EMMSProxyInitializer(String remoteHost, int remotePort) throws CertificateException, SSLException {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        if(EMMSProxy.isHttps()) {
            //sslContext = getSSLContext();
            try {
                sslContext =SslContextBuilder.forServer(getKMF()).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //SelfSignedCertificate ssc = new SelfSignedCertificate();
            //this.sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        }
    }

    @Override
    public void initChannel(SocketChannel ch) {
        if(EMMSProxy.isHttps()){
            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));

            //SSLEngine sslEngine = sslContext.createSSLEngine();
            //sslEngine.setUseClientMode(false);
            //ch.pipeline().addLast("ssl",new SslHandler(sslEngine));
        }
        ch.pipeline().addLast("aggegator", new HttpObjectAggregator(512 * 1024));
        ch.pipeline().addLast(
                new LoggingHandler(LogLevel.INFO),
                new EMMSProxyFrontendHandler(remoteHost, remotePort));
    }

    private SSLContext getSSLContext() {
        //KeyStore ks;
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");//建立证书实体 SSLv3
            Log.d(EMMSProxy.TAG, "getProtocol: " + sslContext.getProtocol());
            sslContext.init(getKMF().getKeyManagers(),tm, null);

        } catch(Exception e){
            Log.e(EMMSProxy.TAG, "getSSLContext: ", e);
        }
        return sslContext;
    }

    public KeyManagerFactory getKMF() throws KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
        KeyStore ks;
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        //ks.load(new FileInputStream("E:\\serverkeys" ), "123456".toCharArray());
        ks.load(ProxyManager.context.getAssets().open("bksserver.keystore"), "123456".toCharArray());

        //Log.d(EMMSProxy.TAG, "KeyManagerFactory.getDefaultAlgorithm(): "+KeyManagerFactory.getDefaultAlgorithm());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");//建立一个密钥管理工厂
        kmf.init(ks, ("123456").toCharArray());

        return kmf;
    }

    TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};
}
