package cn.mcm.manager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import static cn.emms.Tools.getIMEI;

public class Statistics {
	private static boolean newUser = true;
	private static int count = 0;
	private static long lastUpdateTime = 0;
	private static String imei = null;
	private static String accessToken = null;
	private static String downloadUrl=null;

	public static void updateUsageInfo(Context ctx) {
		updateUsageInfo(ctx, "emms.csrcqsf.com", 45478);
		//updateUsageInfo(ctx, "192.168.151.137", 8080);
		//updateUsageInfo(ctx, "159.226.94.159", 8080);
		//updateUsageInfo(ctx, "172.16.1.238", 8080);
	}
	

	public static void updateUsageInfo(final Context ctx, final String ip, final int port) {
		count++;
		if (!isNetworkAvailable(ctx))
			return;
		final long time = System.currentTimeMillis();
		if (time - lastUpdateTime < 2 * 1000)
			return;
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (null==accessToken) {
						//accessToken=new DcSecurity(ctx,"https://"+ip+":"+port).getAccessToken();
						accessToken = getAccessToken(ip,port);
						if (null==accessToken)
							return;
					}
					updateNewUser(ctx,ip,port);
					HttpsTrustManager.allowAllSSL();
					HttpClient httpClient = new DefaultHttpClient();
					String uri = "https://" + ip + ":" + Integer.toString(port)
							+"/api/v1/client/devices/"
							+getIMEI(ctx)
							+"/apps/"
							+ctx.getPackageName()
							+"/stats/usage?platform=android&access_token="
							+accessToken;
					HttpPost post = new HttpPost(uri);
					
					post.addHeader("Content-Type", "application/json");  
				    JSONObject obj = new JSONObject();  

					obj.put("type", "total_start_times");
					obj.put("count",Integer.toString(count));

					StringEntity s = new StringEntity(obj.toString()); 
					s.setContentType("application/json");
					post.setEntity(s);
					 
					Log.d("Statistics","Usage post="+post.getURI().toString());
					HttpResponse httpResponse=httpClient.execute(post);
					if(httpResponse.getStatusLine().getStatusCode() != 204) {
						Log.e("Statistics", "Usage httpResponse: " + httpResponse.getStatusLine().getStatusCode());
						return;
					}
					Log.d("Statistics","Usage result="+httpResponse.getStatusLine().getStatusCode());
					count = 0;
					lastUpdateTime = time;
					Log.d("Statistics","update Usage success");
				} catch (Exception e1) {
					Log.e("Statistics", e1.toString());
					e1.printStackTrace();
				}
			}
		});
		thread.start();
		return;
	}

	public static void updateNewUser(final Context ctx, final String ip, final int port){
		if (!isNetworkAvailable(ctx))
			return;
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		newUser = prefs.getBoolean("newUser", true);
		// if it isn't the case of adding new user, do nothing;
		if(!newUser)
			return;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (null==accessToken) {
						accessToken = getAccessToken(ip,port);
						if (null==accessToken)
							return;
					}
					HttpsTrustManager.allowAllSSL();
					HttpClient httpClient = new DefaultHttpClient();
					String uri = "https://" + ip + ":" + Integer.toString(port)
							+"/api/v1/client/devices/"
							+getIMEI(ctx)
							+"/apps/"
							+ctx.getPackageName()
							+"/stats/user?platform=android&access_token="
							+accessToken;
					HttpPost post = new HttpPost(uri);

					post.addHeader("Content-Type", "application/json");

					Log.d("Statistics","User post="+post.getURI().toString());
					HttpResponse httpResponse=httpClient.execute(post);
					if(httpResponse.getStatusLine().getStatusCode() != 204) {
						Log.e("Statistics", "User httpResponse: " + httpResponse.getStatusLine().getStatusCode());
						return;
					}
					Log.d("Statistics","User result="+httpResponse.getStatusLine().getStatusCode());
					if (newUser) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("newUser", false);
						editor.commit();
					}
					Log.d("Statistics","update new user success");
				} catch (Exception e1) {
					Log.e("Statistics", e1.toString());
					e1.printStackTrace();
				}
			}
		});
		thread.start();
		return;
	}
	public static String getUpdateUrl(final Context ctx) {
		//return getUpdateUrl(ctx,"159.226.94.159", 8080);
		return getUpdateUrl(ctx,"emms.csrcqsf.com", 45478);
	}
	
	public static String getUpdateUrl(final Context ctx,final String ip,final int port) {
		if(Version.getServerVersionCode(ip+":"+port)<1)
			return oldGetUpdateUrl(ctx,ip,port);
		else
			return newGetUpdateUrl(ctx,ip,port);
	}
	public static String newGetUpdateUrl(final Context ctx,final String ip,final int port) {
		downloadUrl = null;
		final int nativeVersionCode = getVersionCode(ctx);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (null==accessToken) {
						accessToken=getAccessToken(ip,port);
						if (null==accessToken) return;
					}
					HttpsTrustManager.allowAllSSL();
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet get = new HttpGet("https://" + ip + ":" + Integer.toString(port)
							+"/api/v1/client/devices/"
							+getIMEI(ctx)
							+"/apps/"
							+ctx.getPackageName()
							+"?platform=android&access_token="
							+accessToken);
					HttpParams params = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(params, 50000);
					HttpConnectionParams.setSoTimeout(params, 50000);
					get.setParams(params);
					HttpResponse httpResponse=httpClient.execute(get);
					String result = EntityUtils.toString(httpResponse.getEntity());
					Log.e("Statistics", "VersionInfo:"+result);
					JSONObject obj = new JSONObject(result);
					int serviceVersionCode = Integer.parseInt(obj
							.getString("version_code"));
					if (serviceVersionCode <= nativeVersionCode)
						return;
					downloadUrl =obj.getString("url");
				} catch (Exception e1) {
					e1.printStackTrace();
					Log.e("Statistics", e1.toString());
				}
			}
		});
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return downloadUrl;
	}
	public static String oldGetUpdateUrl(final Context ctx,final String ip,final int port) {
		downloadUrl=null;
		final int nativeVersionCode=getVersionCode(ctx);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (null==accessToken) {
						accessToken=getAccessToken(ip,port);
						if (null==accessToken) return;
					}
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet get = new HttpGet("http://" + ip + ":" + Integer.toString(port)
							+ "/EMMS-WS/api/v1/apps/"+ctx.getPackageName()+"?access_token="
							+ accessToken);
					HttpParams params = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(params, 2000);
					HttpConnectionParams.setSoTimeout(params, 2000);
					get.setParams(params);
					HttpResponse httpResponse=httpClient.execute(get);
					String result = EntityUtils.toString(httpResponse.getEntity());
					JSONObject obj = new JSONObject(result);
					int serviceVersionCode=Integer.parseInt(obj
							.getString("version_code"));
					String id=obj.getString("id");
					if (serviceVersionCode<=nativeVersionCode) return;
					downloadUrl = "http://" + ip + ":" + Integer.toString(port)
							+ "/EMMS-WS/api/v1/user/apps/download/"+id
							+"?uuid="+ getIMEI(ctx)+"&access_token="+ accessToken;
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
			}
		});
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return downloadUrl;
	}

	
	private static String getAccessToken(String ip,int port) 
			throws URISyntaxException, ClientProtocolException, IOException, JSONException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 10000);
		HttpConnectionParams.setSoTimeout(params, 10000);
		post.setParams(params);
		String url = "";
		if(Version.getServerVersionCode(ip+":"+port)<1)
			url = "/EMMS-WS/oauth/token";
		else
			url = "/api/v1/oauth/token";
		post.setURI(new URI("https://" + ip + ":" + port
				+ url));//  /EMMS-WS/api/v1/oauth/token
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();

		BasicNameValuePair grant_typeParam = new BasicNameValuePair(
				"grant_type", "client_credentials");
		paramList.add(grant_typeParam);
		BasicNameValuePair client_idParam = new BasicNameValuePair(
				"client_id", "6b5a38705d7b3562655925406a652e32");
		paramList.add(client_idParam);
		BasicNameValuePair client_secretParam = new BasicNameValuePair(
				"client_secret", "355f523128212d6e70634446224c2a48");
		paramList.add(client_secretParam);
		post.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
		Log.d("Statistics","post="+post.getURI().toString());

		HttpsTrustManager.allowAllSSL();
		HttpResponse httpResponse = httpClient.execute(post);
		String result = EntityUtils.toString(httpResponse.getEntity());
		Log.d("Statistics","result"+ result);
		if (null == result)
			return null;
		JSONObject obj = new JSONObject(result);
		if (null == obj.getString("access_token"))
			return null;
		String token = obj.getString("access_token");
		Log.d("Statistics","receive token="+ token);
		return token;
	}

	private static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			return cm.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}
	
	private static int getVersionCode(Context context)
	{
		int versionCode = 0;
		try
		{
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return versionCode;
	}
}
