package cn.emms.secureaccess;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by SRX on 2016/6/27.
 * 安全接入管理类
 */
public class IForward {
    //public static final int FORWARD_PORT = 3546;
    private String TAG="SecureAccess";
    String forwardAdrr;
    private HashMap<Integer, String> localPort2Addr = null;
    private List<ATrans> transactions = new ArrayList<>();

    @SuppressLint("UseSparseArrays")
    public IForward(){
        localPort2Addr = new HashMap<>();
    }

    public void addMapping(HashMap<Integer, String> map){
        Set<Integer> keys = map.keySet();

        if(keys.size() <= 0){
            return;
        }

        for(Integer key:keys){
            if(!localPort2Addr.containsKey(key)){
                //start listening
                String[] server = map.get(key).split(":");
                if(server.length == 2){
                    localPort2Addr.put(key, map.get(key));
                    ATrans trans = new ATrans(key, server[0], Integer.parseInt(server[1]));
                    transactions.add(trans);
                    trans.execute(getForwardAdrr());
                }
            }
        }
    }

    public void stopMapping(){
        for(ATrans trans:transactions){
            try{
                trans.stopExecution();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        transactions.clear();
        localPort2Addr.clear();
    }

    public void clearSockets(){
        if(transactions.size()==0||transactions==null)
            return;
        for(ATrans trans:transactions) {
            Set<Socket> appClients = trans.getSocketMapping().keySet();
            for (Iterator<Socket> it = appClients.iterator(); it.hasNext(); ) {
                Socket client = it.next();
                Socket server = trans.getSocketMapping().get(client);
                try {
                    //client.shutdownInput();
                    client.close();
                    Log.d(TAG, "client.shutdownInput");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    server.close();
                    //server.shutdownOutput();
                    Log.d(TAG, "server.shutdownOutput");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //trans.getSocketMapping().remove(client);
            }
            trans.getSocketMapping().clear();
        }
    }

    public String getForwardAdrr() {
        return forwardAdrr;
    }

    public void setForwardAdrr(String addr){
        this.forwardAdrr=addr;
    }
}