package cn.mcm.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.FileObserver;
import android.util.Log;

public class SDCardFileObserver extends FileObserver {
	private Timer mTimer;
	private Map<String,DeleteFileTimerTask> tasks;
	String targetDirName,targetFileName;
	

    public SDCardFileObserver(String path, String fileName,int mask) {  
        super(path, mask);  
        mTimer=new Timer();
        tasks=new HashMap<String,DeleteFileTimerTask>();
        targetDirName=path;
        targetFileName=fileName;
    }  
    
    @Override  
    public void onEvent(int event, String path) {  
        final int action = event & FileObserver.ALL_EVENTS;  
        switch (action) {  
        case FileObserver.OPEN:
        	Log.d("FileObserver","file open; path="+path); 
        	break;
        case FileObserver.CLOSE_NOWRITE:
        case FileObserver.CLOSE_WRITE:
        	Log.d("FileObserver","file close; path=" + path); 
        	if (!path.endsWith(targetFileName)) return;
        	if (tasks.containsKey(path)) {
        		DeleteFileTimerTask task=tasks.get(path);
        		if (task.cancel()) {
		        	task=new DeleteFileTimerTask(path);
		        	mTimer.schedule(task, 30000);
        		}
        	}
        	else {
        		DeleteFileTimerTask task=new DeleteFileTimerTask(path);
        		tasks.put(path, task);
	        	mTimer.schedule(task, 30000);
        	}
            break;               
       default:
            break;  
        }  
    }  
    
    class DeleteFileTimerTask extends TimerTask{
    	
    	private String path;
    	
		public DeleteFileTimerTask(String path) {
			this.path=path;
		}

		@Override
		public void run() {
	 		String tmpFile= targetDirName +"/"+targetFileName;
			File f = new File(tmpFile);
			if (f.exists() && f.isFile()) {
				Log.d("FileObserver", "delete tmp file " + path);
				f.delete();
				SDCardFileObserver.this.stopWatching();
			}
			tasks.remove(path);
		}
    }
	
}
