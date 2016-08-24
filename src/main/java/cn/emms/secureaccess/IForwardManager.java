package cn.emms.secureaccess;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import cn.emms.dcsecurity.DcSecurity;

/**
 * Created by SRX on 2016-7-13.
 */
public class IForwardManager {
    static final int SHOW_RESPONSE =0;
    static Integer localPort;
    static String WebServerAddr;
    static String IForwardServerAddr;
    static Context context;
    static Class aClass;
    static String serviceName;
    static int timeOut = 10;

    /**标志位 来判断是否初始化成功*/
    static boolean initFlag = false;
    /**标志位 用来判断设备是否被认证，-1：操作失败；0：未认证；1：认证成功*/
    static int authFlag = -1;
    /**标志位 表示是否收到认证结果*/
    static boolean getAuthDeviceFlag = false;

    static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("SecureAccess", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //context.startService(getStartIntent(context));
            Log.d("SecureAccess", "onServiceDisconnected");
            bindService();
        }
    };

    static Handler handler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what){
                case SHOW_RESPONSE:
                    getAuthDeviceFlag = true;
            }
        }
    };

    public static boolean init(Context context,Class cls,String packageName,Integer localPort, String webServerAddr){
        if(packageName.equals("")||localPort<-1||webServerAddr.equals("")){
            initFlag = false;
            return false;
        }
        setLocalPort(localPort);
        setWebServerAddr(webServerAddr);
        setContext(context);
        setIForwardServerAddr("emms.csrcqsf.com:43546");//192.168.151.123:3456
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

    static void setIForwardServerAddr(String iForwardServerAddr1){
        IForwardServerAddr = iForwardServerAddr1;
    }

    static Intent getStartIntent(){
        HashMap<Integer,String> map = new HashMap<>();
        map.put(localPort,WebServerAddr);
        Bundle bundle = new Bundle();
        bundle.putSerializable("IpMap",map);

        Intent intent = new Intent(context,aClass);
        intent.putExtras(bundle);
        intent.putExtra("ForwardAddr",IForwardServerAddr);
        return intent;
    }

    static Intent getStopIntent(){
        Intent intent = new Intent(context, aClass);
        return intent;
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
            if (nm.equals(serviceName) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void bindService(){
        if (initFlag==true)
            context.bindService(getStartIntent(),serviceConnection,Context.BIND_AUTO_CREATE);
    }

    public static void unbindSerice(){
        context.unbindService(serviceConnection);
    }

    public static void start(){
        if(initFlag==true && isServiceRunning()==false )
            context.startService(getStartIntent());
    }
    public static void stop(){
        if (isServiceRunning() == true)
            context.stopService(getStopIntent());
    }

    static void authDevice(final Context context1) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Message message = new Message();
                    DcSecurity dcSecurity = new DcSecurity(context1);
                    authFlag = dcSecurity.authDevice();

                    //发出通知，已经收到设备认证的结果
                    message.what = SHOW_RESPONSE;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                }
            }
        }).start();
    }

    public static int getAuthFlag(Context context){
        authDevice(context);
        while(!getAuthDeviceFlag){}
        return authFlag;
    }


    static void setTimeOut(int time){
        timeOut = time;
    }

    static  int getTimeOut(){
        return timeOut;
    }
}
