package cn.mcm.cryption;

import java.io.File;

import javax.crypto.Cipher;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;

import cn.mcm.cryption.AESHelper;
import cn.mcm.key.GenKey;
import cn.mcm.key.GetInfo;
import cn.mcm.listener.FileOpener;
import cn.mcm.listener.SDCardFileObserver;

public class Cryption extends Application {
	private String imei;
    private String ran;
	private AESHelper cry;
	private Context context;
	private SharedPreferences sp;
	private String pin="12345678";
	//private EncryptionOrDecryptionTask mTask = null; 
	
	public static int block = 1024 * 1024;
    public Cryption(){
		
	}
	public Cryption(Context context) {
		this.context = context;
		cry=new AESHelper();
		sp = this.context.getSharedPreferences("random", this.context.MODE_PRIVATE);
	}
	public boolean enCrypt(String filePath){
		final GetInfo gi=new GetInfo(context);
		final GenKey gk=new GenKey();
	    imei = gi.getIMEI();
	    ran = gi.getRandom(16);
	    String key=gk.genKey(imei, ran, pin);
	    String sdpath = Environment.getExternalStorageDirectory().getPath();		
        String curDirPath = sdpath+ "/mcm/"+context.getPackageName();
		String fileName=filePath.substring(filePath.lastIndexOf("/")+1);
		String curPath = curDirPath + "/"+fileName;
		File file = new File(curDirPath);
		File curFile = new File(filePath);
		if (!file.exists())
		{
			file.mkdirs();
		}
        if(curFile.length() < block){
        	try {
    			cry.AESCipher(Cipher.ENCRYPT_MODE, filePath,  
    			        curPath, key);
    			 putRan(curPath, ran);
    			 return true;
    		} catch (Exception e) {
    			e.printStackTrace();
    		}	  
        }else{
        	AESHelper.blockEncCode(filePath, curPath, key);
        	return true;
        }
	    //curPath = curPath + filePath.substring(curPath.lastIndexOf("/"));
	    //this.encryptRan(pin, curPath, ran);
	    return false;
	}

  	public String deCrypt(String filePath) {
  		final GetInfo gi=new GetInfo(context);
		final GenKey gk=new GenKey();
  		ran = this.getRan(filePath);
  		imei = gi.getIMEI();
  		String key=gk.genKey(imei, ran, pin);
 		String tmpDirPath = filePath.subSequence(0, filePath.lastIndexOf("/"))+"/tmp";
 		String tmpPath=tmpDirPath+"/"+filePath.substring(filePath.lastIndexOf("/")+1);
 		File file = new File(tmpDirPath);
 		File curFile = new File(filePath);

		if (!file.exists())
		{
			file.mkdirs();
		}
  		if(curFile.length() < block){
  			try {
  	  			cry.AESCipher(Cipher.DECRYPT_MODE, filePath, tmpPath, key);
  	  			return tmpPath;
  	  		} catch (Exception e) {
  	  			e.printStackTrace();
  	  		}
  		}else{
        	AESHelper.blockDecCode(filePath, tmpPath, key);
        	return tmpPath;
        }
  		return null;
  	}

  	private void putRan(String filePath , String random){
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putString(filePath , random);
	    editor.commit();
  	}

  	private String getRan(String filePath){

        String random = sp.getString(filePath,null); 
        return random;
  	}


  	public void encryptRan(String pin , String filePath , String random){
  		String temp = null;

	    SharedPreferences.Editor editor = sp.edit();

	    temp = cry.encrypt(pin, random);
	    editor.putString(filePath , temp);
	    editor.commit();
  	}
  	//ȡ�������
  	public String decryptRan(String pin , String filePath){
  	    //ȡ���ļ���Ӧ�������  
        String random = sp.getString(filePath, ""); 
        random = cry.decrypt(pin, random);
        return random;
  	}
  	 
	/***********************************************************AESHelper*************************************
	// #######################*/
    public class EncryptionOrDecryptionTask extends  
            AsyncTask<Void, Void, Boolean> {  
    	
    	private String mSourceFile = "";  
        private String mNewFilePath = "";  
        private String mNewFileName = "";  
        private String mSeed = "";  
        private boolean mIsEncrypt = false; 
        
  
        public EncryptionOrDecryptionTask(boolean isEncrypt, String sourceFile,  
                String newFilePath, String newFileName, String seed) {  
        	
            this.mSourceFile = sourceFile;  
            this.mNewFilePath = newFilePath;  
            this.mNewFileName = newFileName;  
            this.mSeed = seed;  
            this.mIsEncrypt = isEncrypt;  
        }  
  
        @Override  
        protected Boolean doInBackground(Void... params) {  
  
            boolean result = false;  
			try {
				if (mIsEncrypt) {  
				    result = cry.AESCipher(Cipher.ENCRYPT_MODE, mSourceFile,  
				            mNewFilePath + mNewFileName, mSeed);  
				} else {  
				    result = cry.AESCipher(Cipher.DECRYPT_MODE, mSourceFile,  
				            mNewFilePath + mNewFileName, mSeed);  
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
            return result;  
        }      

        @Override  
        protected void onPostExecute(Boolean result) {  
            super.onPostExecute(result);   
            if (mIsEncrypt) {  
            	if(result){
            		 
            	}else{
            	}
            } else {
            	if(result){
            		 
            	}else{
            	}
            }   
        }
    } 
}

