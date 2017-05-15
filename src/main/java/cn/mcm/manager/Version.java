package cn.mcm.manager;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import cn.emms.dcsecurity.HttpsClient;

/**
 * Created by Sun RX on 2016-12-23.
 * To get Server Version Code
 */

public class Version {
    static private int serverVersionCode = 0;
    static public final int sdkVersionCode = 2;
    static public int getServerVersionCode(String ip){
        if( serverVersionCode == 0) {
            HttpsTrustManager.allowAllSSL();
            setServerVersionCode(ip);
        }
        return serverVersionCode;
    }
    static public void clearVersionCode (){
        serverVersionCode = 0;
    }
    private Version(){}
    static private void setServerVersionCode(String ip){
        try {
            HttpClient httpClient = HttpsClient.newHttpsClient();
            HttpGet get = new HttpGet("https://" + ip + "/api/v1/server");

            HttpResponse httpResponse = null;
            httpResponse = httpClient.execute(get);
            String result = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(result);
            serverVersionCode = jsonObject.getInt("version_code");
        }catch (Exception e) {
            Log.e("Statistics", "setServerVersionCode: ",e );
            serverVersionCode = -1;
        }
    }
}
