package cn.emms.secureaccess;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by SUN RX on 2016-9-29.
 * to manage toForward Socket, generate Socket and regenerate it,
 * so that AppClient Socket needn't to care about changes and reconnection about toForwards.
 */
public class TransSocket {
    private static final String TAG = "SecureAccess" ;
    static int num =0;
    private Socket toForward;
    private String ip;
    private int timeoutCount =10;
    private int port;
    private String serverIp;
    private int serverPort;
    private boolean working =false;
    private boolean error = false;
    volatile UrgentDataThread urgentDataThread;


    public TransSocket(String ip, int port, String ip2, int port2,int timeout) throws IOException {
        this.ip = ip;
        this.port = port;
        this.timeoutCount = timeout;
        serverIp = ip2;
        serverPort = port2;
        generateNewSocket();
    }

    public synchronized Socket getSocket(){
        return  toForward;
    }

    public synchronized InputStream getInputStream() throws IOException {
        return  toForward.getInputStream();
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        return toForward.getOutputStream();
    }

    public synchronized void generateNewSocket() throws IOException {
        toForward = new Socket();
        toForward.setKeepAlive(true);
        toForward.setTcpNoDelay(true);
        toForward.setSoTimeout(1000*IForwardManager.getForwardTimeOut());//!!
        toForward.connect(new InetSocketAddress(ip, port), timeoutCount);
        working = true;
        registerToServer();
        urgentDataThread = new UrgentDataThread();
        urgentDataThread.start();
        num++;
    }

    public synchronized void close() throws IOException {
        working = false;
        urgentDataThread.cancel();
        toForward.close();
    }

    public synchronized boolean isWorking() throws InterruptedException {
        return working;
    }

    public synchronized void reconnect() throws IOException {
        close();
        generateNewSocket();
    }

    public synchronized boolean isClosed(){
        return toForward.isClosed();
    }

    public synchronized boolean isError(){
        return error;
    }

    public void hasError(){
        error= true;
    }

    public UrgentDataThread getUrgentDateThread(){
        return urgentDataThread;
    }

    public void registerToServer() throws IOException {
        this.toForward.getOutputStream().write((serverIp + ":" + String.valueOf(serverPort)).getBytes());
        Log.d(TAG, "Forward sends serverAddr");

        byte[] reply = new byte[8];
        int readSize = 0;


        readSize = this.toForward.getInputStream().read(reply);
        if (readSize <= 0) {
            throw new IOException();
        }

        try {
            String replyStr = new String(reply, 0, readSize, "US-ASCII");
            if (!replyStr.startsWith("OK")) {
                throw new IOException();
            }
            Log.d(TAG, "Forward gets Server's reply:" + replyStr);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }

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
                    getSocket().sendUrgentData(0xFF);
                    Log.d(TAG, "Urgent Data " + count);
                    count++;
                    Thread.sleep(1000 * 10);
                } catch (IOException e) {
                    //UrgentData;
                    Log.e(TAG, "UrgentData:" + count + "," + e.toString());
                    try {
                        this.cancel();
                        reconnect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
