package cn.emms.secureaccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Set;

import android.util.Log;
/**
 * Created by SRX on 2016/6/27.
 * Socket转发类，具体实现安全接入消息转发
 */
public class ATrans {
    private static final int BUFF_SIZE = 512*2*50;

    String ForwardIpPort = "";//"192.168.151.123:3546";//"emms.csrcqsf.com:43546";
    private int localPort = -1;
    private String serverAddr = null;
    private int serverPort = -1;

    private ServerSocket s = null;

    private boolean isValidTrans = true;
    private String TAG="SecureAccess";

    /** 中转代理的转发对应关系：《连接app的client socket，连接服务器的client socket》*/
    HashMap<Socket, Socket> socketMapping = null;

    public HashMap<Socket, Socket> getSocketMapping() {
        return socketMapping;
    }
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
        this.socketMapping = new HashMap<>();
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
        if (ForwardIpPort == null || ForwardIpPort.equals("")) {
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
                        String ipAndPortRaw = ForwardIpPort;
                        if (ipAndPortRaw == null)
                            continue;
                        String ipAndport[] = ipAndPortRaw.split(":");
                        String ip = ipAndport[0];
                        int port = Integer.valueOf(ipAndport[1]);

                        // 接收'到'来自app的socket 拥塞！
                        Socket appClient = s.accept();
                        appClient.setKeepAlive(true);
                        appClient.setTcpNoDelay(true);
                        //appClient.setSoLinger(true,1);
                        appClient.setSoTimeout(1000*IForwardManager.getAppTimeOut());
                        Log.d(TAG,"APP TimeOut :"+IForwardManager.getAppTimeOut());
                        Log.d(TAG, appClient.getRemoteSocketAddress()+" || "+appClient.getLocalAddress()+":"+appClient.getLocalPort());

                        // 新建'到'Server的client
                        Socket toForward = new Socket();
                        toForward.setKeepAlive(true);
                        toForward.setTcpNoDelay(true);
                        toForward.connect(new InetSocketAddress(ip, port), 1000*5);
                        //设置超时时间
                        toForward.setSoTimeout(1000*IForwardManager.getForwardTimeOut());//!!
                        Log.d(TAG, "Forw TimOut:"+IForwardManager.getForwardTimeOut());
                        Log.d(TAG, toForward.getRemoteSocketAddress()+" || "+toForward.getLocalAddress()+":"+toForward.getLocalPort());

                        socketMapping.put(appClient, toForward);
                        Log.d(TAG,"Size of Mapping:"+socketMapping.size());

                        Thread forwardThread = new ForwardingThread(appClient,
                                toForward);
                        forwardThread.start();
                    } catch (IOException e) {
                        Log.e(TAG, "create Sockets:"+e.toString());
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

        //NEW　ADD
        Boolean appTimeOut = (IForwardManager.getAppTimeOut()==IForwardManager.withOutTS);
        Boolean ForTimeOut = (IForwardManager.getForwardTimeOut()==IForwardManager.withOutTS);

        private class UrgentDataThread extends Thread{
            boolean canceled = false;
            public synchronized void cancel(){
                canceled = true;
            }
            public boolean getCancled(){
                return canceled;
            }
            @Override
            public void run() {
                int count = 0;
                while(!canceled) {
                    try {
                        toForward.sendUrgentData(0xFF);
                        Log.d(TAG, "Urgent Data " + count);
                        count++;
                        Thread.sleep(1000 * 10);
                    } catch (IOException e) {
                        //UrgentData;
                        Log.e(TAG, "UrgentData:" + count + "," + e.toString());
                        clearSocket(0);
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        UrgentDataThread urgentDataThread;


        public ForwardingThread(Socket appClient, Socket toForward) {

            this.appClient = appClient;
            this.toForward = toForward;
            appTimeOut = (IForwardManager.getAppTimeOut()==IForwardManager.withOutTS);
            ForTimeOut = (IForwardManager.getForwardTimeOut()==IForwardManager.withOutTS);

            urgentDataThread = new UrgentDataThread();

            try {
                this.appClientIn = this.appClient.getInputStream();
                this.appClientOut = this.appClient.getOutputStream();

                this.toForwardIn = this.toForward.getInputStream();
                this.toForwardOut = this.toForward.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                clearSocket(0);
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
                clearSocket(0);
                return;
            }

            byte[] reply = new byte[8];
            int readSize = 0;

            try {
                readSize = this.toForwardIn.read(reply);
            } catch (IOException e) {
                e.printStackTrace();
                clearSocket(0);
                return;
            }

            if (readSize <= 0) {
                clearSocket(0);
                return;
            }

            try {
                String replyStr = new String(reply, 0, readSize, "US-ASCII");
                if (!replyStr.startsWith("OK")) {
                    clearSocket(0);
                    return;
                }
                Log.d(TAG, "Forward gets Server's reply:" + replyStr);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            if(IForwardManager.isNeedUrgentData)
                urgentDataThread.start();
            // 监听toForward发送过来的消息，然后通过appClient转发出去
            new Thread(new Runnable() {

                @Override
                public void run() {
                    byte[] buf = null;

                    while (isValidTrans && !appClient.isClosed() && !toForward.isClosed()) {
                        buf = new byte[BUFF_SIZE];
                        int readSize = 0;
                        try {
                            //read from SA Server
                            readSize = toForwardIn.read(buf);
                            if (readSize != -1) {   //如果TimeOut设为0则为True 不为0时超时启动，用数据为false
                                if(readSize==0)
                                    Log.d(TAG, "Read size=0 from Server");
                                ForTimeOut = (IForwardManager.getForwardTimeOut() == IForwardManager.withOutTS);
                                Log.d(TAG, toForward.getLocalSocketAddress() + " Forward: Receive Len=" + readSize);
                            }
                        } catch (SocketTimeoutException e) {
                            Log.e(TAG, "Forw TimeOut!");
                            clearSocket(2);
                        } catch (IOException e) {
                            Log.e(TAG, "READ from Server:"+e.toString());
                            clearSocket(0);
                            return;
                        }

                        if (readSize > 0) {
                            try {
                                //Write to APP
                                appClientOut.write(buf, 0, readSize);
                                appClientOut.flush();
                                Log.d(TAG, appClient.getLocalSocketAddress() + " App: Get Len=" + readSize);
                            } catch (IOException e) {
                                Log.e(TAG, "Write to APP:"+e.toString());
                                clearSocket(0);
                            }
                        }
                    }
                }
            }).start();

            // 监听appClient发送过来的消息,然后通过toForward转发出去
            byte[] buf = null;
            while (isValidTrans && !appClient.isClosed() && !toForward.isClosed()) {
                buf = new byte[BUFF_SIZE];
                //Log.d(TAG, "NEW BUFF:"+Arrays.toString(buf));
                int read = 0;
                try {
                    // read from APP
                    read = this.appClientIn.read(buf);
                    if(read==0)
                        Log.d(TAG, "Read size=0 from APP");
                    if (read > 0) {
                        //当超时启动时，有数据说明没有超时false；超时没有启动的话失效，一直为true；
                        appTimeOut = (IForwardManager.getAppTimeOut() == IForwardManager.withOutTS);
                        Log.d(TAG, appClient.getLocalSocketAddress() + " App: Write Len=" + read);
                    }
                } catch (SocketTimeoutException e) {
                    Log.e(TAG, "APP TimeOut!");
                    clearSocket(1);
                } catch (IOException e) {
                    Log.e(TAG, "Read From APP:"+e.toString());
                    clearSocket(0);
                    return;
                }


                if (read > 0) {
                    try {
                        //toForward.sendUrgentData(0xFF);
                        // Write to Server: Exception should been thrown here!
                        toForward.getOutputStream().write(buf, 0, read);
                        toForward.getOutputStream().flush();
                        Log.d(TAG, toForward.getLocalSocketAddress() + " Forward: Get Len=" + read);
                        //Thread.sleep(1000*13);
                    } catch (IOException e) {
                        Log.e(TAG, "Write to Server:"+e.toString());
                        e.printStackTrace();
                        clearSocket(0);
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, e.toString() );
                        return;
                    }
                }
            }
        }

        /**
         * clear当前的套接字
         * @param type 0:直接清除; 1：appTimeOut; 2:ForwardTimeOut
         */
        private void clearSocket(int type) {// 0:直接清除; 1：appTimeOut; 2:ForwardTimeOut
            if(type == 1)
                appTimeOut = true;
            else if (type == 2) {
                ForTimeOut = true;
            }
            Boolean bothTimeOut = appTimeOut&&ForTimeOut;

            if (type == 0 || bothTimeOut) {
                socketMapping.remove(this.appClient);
                urgentDataThread.cancel();
                try {
                    this.appClientIn.close();
                    this.appClientOut.close();
                    this.toForwardIn.close();
                    this.toForwardOut.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
                try {
                    this.appClient.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
                try {
                    this.toForward.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
                if(appClient.isClosed()&&toForward.isClosed())
                    Log.d(TAG, "clearSocket: Done");
            }
        }
    }
}