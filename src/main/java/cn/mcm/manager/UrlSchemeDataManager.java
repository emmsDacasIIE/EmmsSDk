package cn.mcm.manager;

import android.content.Intent;
import android.net.Uri;

import java.util.List;

import cn.mcm.cryption.AESHelper;

/**
 * Created by Sun Rx on 2017-2-21.
 * get date from intent in url scheme form
 */

public class UrlSchemeDataManager {
    static private final String keyBytes = "abcdefgabcdefg12";
    private String username = null;
    private String password = null;
    private State mState = State.ErrorIntent;
    public enum State{
        ErrorIntent,
        IllegalIntent,
        NormalIntent,
    }
    public UrlSchemeDataManager(Intent intent){
        getUrlSchemeData(intent);
    }

    private void getUrlSchemeData(Intent intent){
        Uri uri = intent.getData();
        if (uri != null) {
            // 完整的url信息
            String url = uri.toString();
            // scheme部分
            String scheme = uri.getScheme();
            // host部分
            String host = uri.getHost();
            //port部分
            int port = uri.getPort();
            // 访问路劲
            String path = uri.getPath();
            List<String> pathSegments = uri.getPathSegments();
            // Query部分
            String query = uri.getQuery();
            if(uri.getQueryParameter("appID")==null||uri.getQueryParameter("appID").isEmpty()
                    ||uri.getQueryParameter("appSecret")==null||uri.getQueryParameter("appSecret").isEmpty()) {
                mState = State.IllegalIntent;
                return;
            }
            //获取指定参数值
            if(uri.getQueryParameter("username")==null||uri.getQueryParameter("username").isEmpty()
                    ||uri.getQueryParameter("password")==null||uri.getQueryParameter("password").isEmpty()) {
                mState = State.IllegalIntent;
                return;
            }
            username = AESHelper.decryptString(uri.getQueryParameter("username"),keyBytes);
            password = AESHelper.decryptString(uri.getQueryParameter("password"),keyBytes);
            mState = State.NormalIntent;
        }
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public State getState(){
        return mState;
    }
}
