package cn.emms.secureaccess.Nio;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sun RX on 2016-10-7.
 * a static Class to manage Proxy Service
 */
public class ProxyManager {
    static Integer localPort;
    static String WebServerAddr;
    static String IForwardServerAddr;
    static Context context;
    static Class aClass;
    static String serviceName;
    static HashMap<Integer,String> map;
    /**标志位 来判断是否初始化成功*/
    static boolean initFlag = false;

    /** 用于接收IForward*/
    static EMMSProxySet emmsProxySet;

    static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            emmsProxySet = ((ProxyService.ProxyIBinder) service).getIForward();
            Log.d("SecureAccess", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("SecureAccess", "onServiceDisconnected");
        }
    };

    public static boolean init(Context context,Class cls,String packageName,Integer localPort, String webServerAddr){
        if(packageName.equals("")||localPort<-1||webServerAddr.equals("")){
            initFlag = false;
            return false;
        }
        map = new HashMap<>();
        map.put(localPort,webServerAddr);

        setContext(context);
        setIForwardServerAddr("emms.csrcqsf.com:43546");//192.168.151.123:3456  emms.csrcqsf.com 122.4.80.26
        aClass = cls;
        serviceName = packageName;
        initFlag = true;
        return initFlag;
    }

    public static boolean init(Context context,Class cls,String packageName,HashMap<Integer,String> addrMap){
        if(packageName.equals("")||addrMap.size()<1){
            initFlag = false;
            return false;
        }
        map = new HashMap<>(addrMap);
        setContext(context);
        setIForwardServerAddr("emms.csrcqsf.com:43546");//192.168.151.123:3456  emms.csrcqsf.com 122.4.80.26
        aClass = cls;
        serviceName = packageName;
        initFlag = true;
        return initFlag;
    }

    static void setContext(Context context1){
        context = context1;
    }

    static void setLocalPort(Integer port){
        localPort = port;
    }

    static void setWebServerAddr(String webServerAddr1){
        WebServerAddr = webServerAddr1;
    }

    public static void setIForwardServerAddr(String iForwardServerAddr1){
        IForwardServerAddr = iForwardServerAddr1;
    }

    static Intent getStartIntent(){
        Bundle bundle = new Bundle();
        bundle.putSerializable("IpMap",map);

        Intent intent = new Intent(context,aClass);
        intent.putExtras(bundle);
        intent.putExtra("ForwardAddr",IForwardServerAddr);
        return intent;
    }

    static Intent getStopIntent(){
        return new Intent(context, aClass);
    }

    public static boolean isServiceRunning() {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(100);

        if (!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            String nm = serviceList.get(i).service.getClassName();
            if (nm.equals(serviceName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void bindService(){
        if (initFlag)
            context.bindService(getStartIntent(),serviceConnection,Context.BIND_AUTO_CREATE);
    }

    public static void unbindSerice(){
        context.unbindService(serviceConnection);
    }

    public static void start(){
        if(initFlag&&!isServiceRunning() )
            context.startService(getStartIntent());
    }
    public static void stop(){
        if (isServiceRunning())
            context.stopService(getStopIntent());
    }
}
