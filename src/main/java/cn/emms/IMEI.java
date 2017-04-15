package cn.emms;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by Sun on 2017-4-13.
 */

public class IMEI {
    static private String imei;
    static private TelephonyManager telephonyManager = null;
    static public String getIMEI(Context context) {
        Log.d("IMEI", getMacAddress());
        if (imei == null) {
            if (telephonyManager == null) {
                telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
            }
            imei = telephonyManager.getDeviceId();
            if (imei == null) {
                imei = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            if(imei == null){
                //WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                //wm.getConnectionInfo().getMacAddress()
                String m_szWLANMAC = getMacAddress();
                imei = getMessageDigest(m_szWLANMAC);
            }
        }
        return imei;
    }

    static private String getMacAddress() {
        String macAddress =  new String();
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
            macAddress = macSerial;
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macAddress;

    }

    static private String getMessageDigest(String m_szLongID){
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        m.update(m_szLongID.getBytes(),0,m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }   // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase(Locale.ENGLISH);
        return m_szUniqueID;
    }
}
