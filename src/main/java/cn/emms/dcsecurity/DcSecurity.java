package cn.emms.dcsecurity;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import cn.emms.IMEI;
import cn.mcm.manager.Version;

import static cn.emms.IMEI.getIMEI;

public class DcSecurity {
	private String ip;
	private String accessToken;
	private Context ctx;

	public DcSecurity(Context ctx) {
		this.ctx=ctx;
//		this.ip = "https://159.226.94.159:8443";
		this.ip ="emms.csrcqsf.com:47836";
	}

	public DcSecurity(Context ctx, String url) {
		this.ctx=ctx;
//		this.ip = "https://159.226.94.159:8443";
		this.ip= url;
	}

	private String getAccessToken() throws JSONException, IOException {
		String url;
		if(Version.getServerVersionCode(ip)<1)
			url = "/EMMS-WS/oauth/token?grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48";
		else
			url = "/api/v1/oauth/token?grant_type=client_credentials&client_id=2b5a38705d7b3562655925406a652e65&client_secret=234f523128212d6e70634446224c2a48";

		HttpClient httpClient = HttpsClient.newHttpsClient();
		HttpPost post = new HttpPost("https://" + ip + url);

		HttpResponse httpResponse = null;
		httpResponse = httpClient.execute(post);
		String result = null;
		result = EntityUtils.toString(httpResponse.getEntity());
		JSONObject jsonObject = new JSONObject(result);
		accessToken = jsonObject.getString("access_token");
		return accessToken;
	}

	public boolean disableDevice() {
		boolean status = false;
		if (accessToken == null || accessToken.equals("")) {
			try {
				getAccessToken();
			} catch (JSONException e) {
				e.printStackTrace();
				return status;
			} catch (IOException e) {
				e.printStackTrace();
				return status;
			}
		}
		String IMEI = cn.emms.IMEI.getIMEI(ctx);
		// 锟斤拷锟斤拷锟斤拷锟斤拷
		// HttpClient httpClient = new DefaultHttpClient();
		HttpClient httpClient = HttpsClient.newHttpsClient();
		HttpPut get = new HttpPut(ip + "/api/v1/devices/" + IMEI + "/suspended?access_token=" + accessToken);

		// 锟斤拷锟斤拷HttpPost锟斤拷锟襟，诧拷锟斤拷锟斤拷HttpResponse锟斤拷锟斤拷
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			return status;
		}

		try {
			StatusLine statusLine = httpResponse.getStatusLine();
			status = statusLine.getStatusCode() == 204;
		} catch (ParseException e) {
			e.printStackTrace();
			return status;
		}
		return status;
	}

	public int authDevice() {
		int status = -1;
		if (accessToken == null || accessToken.equals("")) {
			try {
				getAccessToken();
			} catch (JSONException e) {
				e.printStackTrace();
				return status;
			} catch (IOException e) {
				e.printStackTrace();
				return status;
			}
		}
		String IMEI = getIMEI(ctx);
		// 锟斤拷锟斤拷锟斤拷锟斤拷
		// HttpClient httpClient = new DefaultHttpClient();
		String url ="";
		if(Version.getServerVersionCode(ip)<1)
			url = "/EMMS-WS/api/v1/devices/";
		else
			url = "/api/v1/client/devices/";
		HttpClient httpClient = HttpsClient.newHttpsClient();
		HttpGet get = new HttpGet("https://"+ ip + url + IMEI + "?access_token=" + accessToken);
		Log.d("SDK", "authDevice request:"+get.getURI().toString());
		// 锟斤拷锟斤拷HttpPost锟斤拷锟襟，诧拷锟斤拷锟斤拷HttpResponse锟斤拷锟斤拷
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			return status;
		}
		String result = null;

		try {
			result = EntityUtils.toString(httpResponse.getEntity());
		} catch (ParseException e) {
			e.printStackTrace();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			return status;
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(result);
			Log.d("SDK", "authDevice: "+jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return status;
		}

		try {
			status = (!jsonObject.getBoolean("status"))  ? 0 : 1;
		} catch (JSONException e) {
			e.printStackTrace();
			status = 0;
			return status;
		}
		return status;
	}

	public void cleanDevice() {
		DataCleanManager.cleanInternalCache(ctx);
		DataCleanManager.cleanDatabases(ctx);
		DataCleanManager.cleanSharedPreference(ctx);
		DataCleanManager.cleanFiles(ctx); 
	}
}
