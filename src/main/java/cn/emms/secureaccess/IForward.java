package cn.emms.secureaccess;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by SRX on 2016/6/27.
 * 安全接入管理类
 */
public class IForward {
    //public static final int FORWARD_PORT = 3546;
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
        for(ATrans trans:transactions){
            try{
                Set<Socket> appClients = trans.getSocketMapping().keySet();
                for (Socket client : appClients) {
                    Socket server = trans.getSocketMapping().get(client);
                    //直接如此删除会报错。
                    //trans.getSocketMapping().remove(client);
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                trans.getSocketMapping().clear();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getForwardAdrr() {
        return forwardAdrr;
    }

    public void setForwardAdrr(String addr){
        this.forwardAdrr=addr;
    }
}
