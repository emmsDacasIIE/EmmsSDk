package cn.emms.secureaccess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by SRX on 2016/7/4.
 */
public class IForwardService extends Service {

    String TAG = "SecureAccess";
    IForward forward;
    HashMap<Integer,String> map ;
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "IF Service onBind");
        map = (HashMap<Integer, String>) intent.getSerializableExtra("IpMap");
        forward.setForwardAdrr(intent.getStringExtra("ForwardAddr"));
        forward.addMapping(map);
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        forward = new IForward();
        Log.d(TAG, "IF Service onCreate");
    }

    @SuppressWarnings("unchecked")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"IF Service onStartCommand");
        map = (HashMap<Integer, String>) intent.getSerializableExtra("IpMap");
        forward.setForwardAdrr(intent.getStringExtra("ForwardAddr"));
        forward.addMapping(map);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "IF Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        forward.stopMapping();
        Log.d(TAG, "IF Server onDestroy");
    }
}
