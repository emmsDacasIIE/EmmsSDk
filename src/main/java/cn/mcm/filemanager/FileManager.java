package cn.mcm.filemanager;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;
import cn.mcm.cryption.Cryption;
import cn.mcm.listener.FileOpener;
import cn.mcm.listener.SDCardFileObserver;

public class FileManager {
	private Context ctx;
	String sdpath=null;
	String curDirPath=null;
	
	public FileManager(Context ctx) {
		this.ctx=ctx;
		sdpath = Environment.getExternalStorageDirectory().getPath();		
        curDirPath = sdpath+ "/mcm/"+ctx.getPackageName();
	}
	
	public boolean readAndBurn(String path) {
		if (saveFile(path,true))
		{
			String fileName=path.substring(path.lastIndexOf("/")+1);
			if (openFile(fileName) && deleteFile(fileName) )
			{
				return true;
			}
			return false;
		}
		else return false;
	}
	
	public boolean saveFile(String path,boolean deleteAfterSave) {
		File f=new File(path);
		if (!f.exists()) return false;
		Cryption cryption=new Cryption(ctx);
		boolean result=cryption.enCrypt(path);	
		if (deleteAfterSave) {
			f.delete();
		}
		return result;
	}
	
	
	public boolean openFile(String fileName) {
		PackageManager manager = ctx.getPackageManager();
		List<PackageInfo> pkgList = manager.getInstalledPackages(0);
		boolean wpsInstalled=false;
		for (int i = 0; i < pkgList.size(); i++) {
			PackageInfo pI = pkgList.get(i);
			if (pI.packageName.toLowerCase().equals("cn.wps.moffice_eng")) {
				wpsInstalled=true;
				break;
			}
		}
		if (!wpsInstalled) {
			Toast.makeText(ctx, "EMMS-WPS not installed", Toast.LENGTH_SHORT).show();
			return false;
		}
		 String curPath = curDirPath + "/"+fileName;
		 File targetFile = new File(curPath);
		 if(!targetFile.exists()){
			Log.d("FileManager", "file not exist");
			return false;
		 }
		 Cryption cryption=new Cryption(ctx);
		 String tmpPath=cryption.deCrypt(curPath);
		 String dirPath = curDirPath+"/tmp";
		 SDCardFileObserver mFileObserver = new SDCardFileObserver(dirPath,fileName,FileObserver.OPEN|FileObserver.CLOSE_NOWRITE|FileObserver.CLOSE_WRITE);  
         mFileObserver.startWatching();
         FileOpener fo=new FileOpener(ctx);
         fo.openFile(new File(tmpPath));
		 return true;
	}
	
	public boolean deleteFile(String fileName) {
		 String curPath = curDirPath + "/"+fileName;
		 File targetFile = new File(curPath);
		 if(!targetFile.exists()){
			Log.d("FileManager", "file not exist");
			return false;
		 }
		 return targetFile.delete();
	}
}
