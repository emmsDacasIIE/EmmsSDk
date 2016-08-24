package cn.emms.secureaccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Set;

import android.util.Log;
/**
 * Created by SRX on 2016/6/27.
 */
public class ATrans {
    private static final int BUFF_SIZE = 512*4;

    /** This Ip&Port is just for debug, not for running in real condition*/
    String ForwardIpPort = "";//"192.168.151.123:3546";//"emms.csrcqsf.com:43546";//Forward IP: 123:3546
    private int localPort = -1;
    private String serverAddr = null;
    private int serverPort = -1;

    private ServerSocket s = null;

    private boolean isValidTrans = true;
    private String TAG="SecureAccess";

    /** 中转代理的转发对应关系：《连接app的client socket，连接服务器的client socket》*/
    HashMap<Socket, Socket> socketMapping = null;

	/**
	 * ATrans初始化
	 * @param localPort 本地端口
	 * @param serverAddr 服务器地址
	 * @param serverPort 服务器端口
	 */
    public ATrans(int localPort, String serverAddr, int serverPort) {
        this.localPort = localPort;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.socketMapping = new HashMap<Socket, Socket>();
    }

	/**
	 * 运行
	 * 1. 如果发现本地端口、服务器地址、服务器端口三者有一没有设置，则返回空，不执行。
	 * 2. isValidTrans为真时，循环监听端口，如果发现有APP发起连接请求，则建立到Server端的对应关系Socket转发关系
	 * 3. 建立完对应关系后，执行转发进程。
	 */
    public void execute(String IFAddr) {
    	//如果发现本地端口、服务器地址、服务器端口三者有一没有设置，则返回空，不执行
    	if (this.localPort <= -1 || this.serverAddr == null
                || this.serverPort <= -1) {
            return;
        }
        isValidTrans = true;

        ForwardIpPort = IFAddr;
        if (ForwardIpPort == null || ForwardIpPort == "")
        {
            return;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    s = new ServerSocket(localPort);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    try {
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                while (isValidTrans) {
                    try {
                    	/*为实验而修改*/
                        //String ipAndPortRaw = NetworkDef.getAvailableForwardIp();
                        String ipAndPortRaw = ForwardIpPort;
                        if (ipAndPortRaw == null)
                            continue;
                        String ipAndport[] = ipAndPortRaw.split(":");
                        String ip = ipAndport[0];
                        int port = Integer.valueOf(ipAndport[1]);
                        // 接收'到'来自app的socket
                        Socket appClient = s.accept();
                        Log.d(TAG, appClient.getRemoteSocketAddress()+" || "+appClient.getLocalAddress()+":"+appClient.getLocalPort());

                        // 新建'到'Server的client
                        Socket toForward = new Socket(ip, port);
                        toForward.setSoTimeout(1000*IForwardManager.getTimeOut());//!!
                        Log.d(TAG, toForward.getRemoteSocketAddress()+" || "+toForward.getLocalAddress()+":"+toForward.getLocalPort());
                        socketMapping.put(appClient, toForward);
                        Log.d(TAG,"Size of Mapping:"+socketMapping.size());

                        Thread forwardThread = new ForwardingThread(appClient,
                                toForward);
                        forwardThread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void stopExecution() {
        this.isValidTrans = false;

        try {
            s.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Set<Socket> appClients = socketMapping.keySet();

        for (Socket client : appClients) {
            Socket server = socketMapping.get(client);
            socketMapping.remove(client);
            try {
                client.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ForwardingThread extends Thread {
        private Socket appClient = null;
        private InputStream appClientIn = null;
        private OutputStream appClientOut = null;

        private Socket toForward = null;
        private InputStream toForwardIn = null;
        private OutputStream toForwardOut = null;

        public ForwardingThread(Socket appClient, Socket toForward) {
            this.appClient = appClient;
            this.toForward = toForward;

            try {
                this.appClientIn = this.appClient.getInputStream();
                this.appClientOut = this.appClient.getOutputStream();

                this.toForwardIn = this.toForward.getInputStream();
                this.toForwardOut = this.toForward.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                clearSocket();
            }
        }

        public void run() {
            if (this.appClient.isClosed() || this.toForward.isClosed()) {
                return;
            }

            try {
                this.toForwardOut.write((serverAddr + ":" + String.valueOf(serverPort)).getBytes());
                Log.d(TAG, "Forward sends serverAddr");
            } catch (IOException e) {
                e.printStackTrace();
                clearSocket();
                return;
            }

            byte[] reply = new byte[8];
            int readSize = 0;

            try {
                readSize = this.toForwardIn.read(reply);
            } catch (IOException e) {
                e.printStackTrace();
                clearSocket();
                return;
            }

            if (readSize <= 0) {
                clearSocket();
                return;
            }

            try {
                String replyStr = new String(reply, 0, readSize, "US-ASCII");
                if (!replyStr.startsWith("OK")) {
                    clearSocket();
                    return;
                }
                Log.d(TAG, "Forward gets Server's reply:"+replyStr);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // 监听toForward发送过来的消息，然后通过appClient转发出去
            new Thread(new Runnable() {

                @Override
                public void run() {
                    byte[] buf = null;

                    while (isValidTrans) {
                        buf = new byte[BUFF_SIZE];
                        int readSize = 0;
                        try {
                            readSize = toForwardIn.read(buf);
                            if(readSize!=-1)
                                Log.d(TAG,toForward.getLocalSocketAddress()+" Forward: Receive Len="+readSize);
                        }catch(SocketTimeoutException e){
                        	Log.d(TAG, "TimeOut!");
                        	clearSocket();
                            return;
                        }catch (IOException e) {
                            e.printStackTrace();
                            clearSocket();
                            return;
                        }

                        if (readSize > 0) {
                            try {
                                appClientOut.write(buf,0,readSize);
                                appClientOut.flush();
                                Log.d(TAG,appClient.getLocalSocketAddress()+" App: Get Len="+readSize);
                            } catch (IOException e) {
                                clearSocket();
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            // 监听appClient发送过来的消息,然后通过toForward转发出去
            byte[] buf = null;
            while (isValidTrans) {
                buf = new byte[BUFF_SIZE];
                int read = 0;
                try {
                    read = this.appClientIn.read(buf);
                    if(read>0)
                        Log.d(TAG,appClient.getLocalSocketAddress()+" App: Write Len="+read);
                } catch (IOException e) {
                    e.printStackTrace();
                    clearSocket();
                    return;
                }

                if (read > 0) {
                    try {
                        this.toForwardOut.write(buf, 0, read);//设置转发长度
                        //this.toForwardOut.write(buf);
                        toForwardOut.flush();
                        Log.d(TAG,toForward.getLocalSocketAddress()+" Forward: Get Len="+read);
                    } catch (IOException e) {
                        e.printStackTrace();
                        clearSocket();
                        return;
                    }
                }
            }
        }

        private void clearSocket() {
            socketMapping.remove(this.appClient);
            try {
                this.appClientIn.close();
                this.appClientOut.close();
                this.toForwardIn.close();
                this.toForwardOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                this.appClient.close();
                this.toForward.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
