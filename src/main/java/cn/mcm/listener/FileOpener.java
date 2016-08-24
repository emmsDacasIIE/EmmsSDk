package cn.mcm.listener;

import java.io.File;

import cn.mcm_sdk.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

public class FileOpener {
	private static final String TAG = FileOpener.class.getSimpleName();
	private Context context;
	
	private String[] fileEndingWebText=new String[] {
			  ".htm","html",".php",".jsp"
	};
	private String[] fileEndingText=new String[] {
			".txt",".java",".c",".cpp",".py",".xml",".json",".log"
	};
	private String[] fileEndingWord=new String[] {
			".doc",".docx"
	};
	private String[] fileEndingExcel=new String[] {
			".xls",".xlsx"
	};
	private String[] fileEndingPPT=new String[] {
			".ppt",".pptx"
	};
	private String[] fileEndingPdf=new String[] {
			".pdf"
	};
	
	public FileOpener(Context context){
		this.context = context;
	}
	

    public static Intent getHtmlFileIntent(File file)
    {
        Uri uri = Uri.parse(file.toString()).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(file.toString()).build();
        Intent intent = new Intent();
        intent.setDataAndType(uri, "text/html");
        intent.setAction("android.intent.action.DCSVIEW"); 
        return intent;
    }

    public static Intent getImageFileIntent(File file)
    {
        Intent intent = new Intent();
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.intent.action.DCSVIEW"); 
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    public static Intent getPdfFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addCategory("android.intent.category.DEFAULT");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("android.intent.action.DCSVIEW"); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "application/pdf");
      return intent;
    }

  public static Intent getTextFileIntent(File file)
  {    
    Intent intent = new Intent();
    intent.addCategory("android.intent.category.DEFAULT");
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setAction("android.intent.action.DCSVIEW"); 
    Uri uri = Uri.fromFile(file);
    intent.setDataAndType(uri, "text/plain");
    return intent;
  }
 

    public static Intent getAudioFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("oneshot", 0);
      intent.putExtra("configchange", 0);
      intent.setAction(android.content.Intent.ACTION_VIEW); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "audio/*");
      return intent;
    }

    public static Intent getVideoFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra("oneshot", 0);
      intent.putExtra("configchange", 0);
      intent.setAction(android.content.Intent.ACTION_VIEW); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "video/*");
      return intent;
    }
 
 

    public static Intent getChmFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addCategory("android.intent.category.DEFAULT");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("android.intent.action.DCSVIEW"); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "application/x-chm");
      return intent;
    }
 
 

    public static Intent getWordFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addCategory("android.intent.category.DEFAULT");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("android.intent.action.DCSVIEW"); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "application/msword");
      return intent;
    }

    public static Intent getExcelFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addCategory("android.intent.category.DEFAULT");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("android.intent.action.DCSVIEW"); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "application/vnd.ms-excel");
      return intent;
    }

    public static Intent getPPTFileIntent(File file)
    {
      Intent intent = new Intent();
      intent.addCategory("android.intent.category.DEFAULT");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("android.intent.action.DCSVIEW"); 
      Uri uri = Uri.fromFile(file);
      intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
      return intent;
    }

    public static Intent getApkFileIntent(File file)
    {
        Intent intent = new Intent();  
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        intent.setAction(android.content.Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.fromFile(file),  "application/vnd.android.package-archive");  
        return intent;
    }
	
	public boolean checkEndsWithInStringArray(String checkItsEnd, 
            String[] fileEndings){
		for(String aEnd : fileEndings){
			if(checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}
	
	public void openFile(File currentPath) {
		if(currentPath!=null&&currentPath.isFile())
        {
            String fileName = currentPath.toString();
            Intent intent;
            Resources r = context.getResources(); 
            //Log.d(TAG, "R.array"+context.getResources().getStringArray(R.array.fileEndingImage));
            if(checkEndsWithInStringArray(fileName, fileEndingWebText)){
                intent = getHtmlFileIntent(currentPath);
                context.startActivity(intent);
            }else if(checkEndsWithInStringArray(fileName,fileEndingText)){
                intent = getTextFileIntent(currentPath);
                context.startActivity(intent);
            }else if(checkEndsWithInStringArray(fileName, fileEndingPdf)){
                intent = getPdfFileIntent(currentPath);
                context.startActivity(intent);
            }else if(checkEndsWithInStringArray(fileName, fileEndingWord)){
                intent = getWordFileIntent(currentPath);
                context.startActivity(intent);
            }else if(checkEndsWithInStringArray(fileName, fileEndingExcel)){
                intent = getExcelFileIntent(currentPath);
                context.startActivity(intent);
            }else if(checkEndsWithInStringArray(fileName, fileEndingPPT)){
                intent = getPPTFileIntent(currentPath);
                context.startActivity(intent);
            }else{
            	Toast.makeText(context.getApplicationContext(), "Path:"+currentPath.getName(), Toast.LENGTH_SHORT).show();
            }
        }else{
        	Toast.makeText(context.getApplicationContext(), "?", Toast.LENGTH_SHORT).show();
        }
	}
}
