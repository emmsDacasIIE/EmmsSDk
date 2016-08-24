package cn.mcm.key;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import cn.mcm.cryption.AESHelper;

public class GetInfo extends Application {
    private Context context;
    private AESHelper cry;

    public GetInfo(Context context) {
        cry = new AESHelper();
        this.context = context;
    }

    public String getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService(this.context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public final String getRandom(int length) {
        char[] numbersAndLetters = null;
        java.util.Random randGen = null;
        if (length < 1) {
            return null;
        }
        if (randGen == null) {
            if (randGen == null) {
                randGen = new java.util.Random();
                numbersAndLetters = ("0123456789").toCharArray();
            }
        }
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(9)];
        }
        return new String(randBuffer);
    }
}
