package cn.emms.secureaccess.Nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016-10-7.
 */
public class EMMSProxySet {
    String forwardAddr;
    private HashMap<Integer, String> localPort2Addr = null;
    private List<EMMSProxy> proxies = new ArrayList<>();

    public EMMSProxySet(){
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
                String server = map.get(key);
                if(!server.equals("")){
                    localPort2Addr.put(key, map.get(key));
                    EMMSProxy emmsProxy = new EMMSProxy(key, server);
                    proxies.add(emmsProxy);
                    emmsProxy.startWork();
                }
            }
        }
    }

    public void stopMapping(){
        for(EMMSProxy emmsProxy:proxies){
            emmsProxy.clearProxy();
        }
        proxies.clear();
        localPort2Addr.clear();
    }
}
