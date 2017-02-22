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
 * 安全接入管理类，用户直接操作该类控制安全接入服务
 */
public class IForwardManager {
    static final int SHOW_RESPONSE =0;
    static Integer localPort;
    static String WebServerAddr;
    static String IForwardServerAddr;
    static Context context;
    static Class aClass;
    static String serviceName;

    static int minTimeOut = 10;
    static int maxTimeOut = 120;
    static int withOutTS = 0;
    static int ForwardTimeOut = withOutTS;
    static int appTimeOut = maxTimeOut;

    static boolean isNeedUrgentData = true;

    /**标志位 来判断是否初始化成功*/
    static boolean initFlag = false;

    /** 用于接收IForward*/
    static IForward iForward;

    static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iForward = ((IForwardService.IForwardIBinder) service).getIForward();
            Log.d("SecureAccess", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //context.startService(getStartIntent(context));
            Log.d("SecureAccess", "onServiceDisconnected");
            //bindService();
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
        setIForwardServerAddr("emms.csrcqsf.com:43546");//192.168.151.175:3456  emms.csrcqsf.com:43546
        aClass = cls;
        serviceName = packageName;
        initFlag = true;
        setForwardTimeOut(withOutTS);
        setAppTimeOut(withOutTS);
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

    static public void setIForwardServerAddr(String iForwardServerAddr1){
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

    public static int getAppTimeOut() {
        return appTimeOut;
    }

    public static void setAppTimeOut(int appTimeOut) {
        IForwardManager.appTimeOut = appTimeOut;
    }

    static void setForwardTimeOut(int time){
        ForwardTimeOut = time;
    }

    static int getForwardTimeOut(){
        return ForwardTimeOut;
    }

    public static void refresh(){
        if (iForward==null)
            return;
        iForward.clearSockets();
    }

    private static void turnOnGetTSAndRefresh(){
        setThresholdAndRefresh(withOutTS,minTimeOut);
    }

    private static void turnOnPostTSAndRefresh(){
        setThresholdAndRefresh(minTimeOut,withOutTS);
    }

    private static void turnOffThresholdAndRefresh(){
        setThresholdAndRefresh(withOutTS,withOutTS);
    }

    private static void turnBothThresholdAndRefresh(){
        setThresholdAndRefresh(minTimeOut,minTimeOut);
    }

    /**
     * @param appTs app threshold
     * @param forwardTs forward threshold
     */
    private static void setThresholdAndRefresh(int appTs, int forwardTs){
        int oldForwardTS = getForwardTimeOut();
        int oldAppTS = getAppTimeOut();
        //如果原有超时时间和要设置的值相同，则返回。
        if(oldForwardTS == forwardTs && oldAppTS == appTs) return;

        if(forwardTs>=minTimeOut && forwardTs<=maxTimeOut){
            setForwardTimeOut(forwardTs);
        } else if (forwardTs==withOutTS){
            setForwardTimeOut(withOutTS);
        }
        else {
            setForwardTimeOut(minTimeOut);
        }

        if(appTs>=minTimeOut && appTs<=maxTimeOut){
            setAppTimeOut(appTs);
        } else if (appTs==withOutTS){
            setAppTimeOut(withOutTS);
        }
        else {
            setAppTimeOut(minTimeOut);
        }
        refresh();
    }

    static public void setOneSyncTask(){
        isNeedUrgentData = false;
        refresh();
    }

    static public void setMulAsynTask(){
        isNeedUrgentData = true;
        refresh();
    }
}
