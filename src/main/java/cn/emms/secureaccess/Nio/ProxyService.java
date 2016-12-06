package cn.emms.secureaccess.Nio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Sun RX on 2016-10-7.
 * A service to control EMMSProxySet
 */
public class ProxyService extends Service {

    String TAG = "SecureAccess";
    EMMSProxySet emmsProxySet;
    HashMap<Integer,String> map ;

    class ProxyIBinder extends Binder {
        EMMSProxySet emmsProxySet;

        public ProxyIBinder(EMMSProxySet emmsProxySet){
            this.emmsProxySet = emmsProxySet;
        }

        public EMMSProxySet getIForward() {
            return this.emmsProxySet;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Proxy Service onBind");
        map = (HashMap<Integer, String>) intent.getSerializableExtra("IpMap");

        if(map.size()<0)
            return null;

        emmsProxySet.forwardAddr = intent.getStringExtra("ForwardAddr");
        emmsProxySet.addMapping(map);
        return new ProxyIBinder(emmsProxySet);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        emmsProxySet = new EMMSProxySet();
        Log.d(TAG, "Proxy Service onCreate");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Proxy Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        emmsProxySet.stopMapping();
        super.onDestroy();
        Log.d(TAG, "Proxy Server onDestroy");
    }
}
