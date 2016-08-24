package cn.mcm.key;

import android.content.Context;

public class GenKey {
	private Context context;
	
	public GenKey(){
		
	}

	public GenKey(Context c){
		context=c;
	}
	

	public String genKey(String imei,String ran,String pin){

		
		int temp=0;
		temp = Math.max(imei.length(), ran.length());
		temp = Math.max(temp, pin.length());
		String imeiTmp = imei;
		String ranTmp = ran;
		String pinTmp = pin;
		while(imeiTmp.length() < temp) {
			imeiTmp += "d";
		}
		while(ranTmp.length() < temp) {
			ranTmp += "1";
		}
		while(pinTmp.length() < temp) {
			pinTmp += ".";
		}
		String key = "";
		for(int i=0; i<temp; i++) {
			key += imeiTmp.charAt(i) ^ ranTmp.charAt(i) ^ pinTmp.charAt(i);
		}
		//key.hashCode();
		return key;
	}
}
