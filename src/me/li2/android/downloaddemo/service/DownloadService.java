package me.li2.android.downloaddemo.service;

import java.net.MalformedURLException;

import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.StorageUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

// http://developer.android.com/guide/components/bound-services.html

public class DownloadService extends Service {
    
    public static final String ACTION_DOWNLOADING_STATUS = "me.li2.android.downloaddemo.downloading_status";


    private final IBinder mBinder = new DownloadBinder();
    
    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /** method for clients */
    public void startDownloadTask(String url) {
        try {
            DownloadTask task = newDownloadTask(url);
            task.execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    private DownloadTask newDownloadTask(String url) throws MalformedURLException {

        DownloadTaskListener taskListener = new DownloadTaskListener() {

            @Override
            public void updateProcess(DownloadTask task) {

                Intent updateIntent = new Intent(ACTION_DOWNLOADING_STATUS);
                updateIntent.putExtra(MyIntents.TYPE, MyIntents.Types.PROCESS);
                updateIntent.putExtra(MyIntents.PROCESS_SPEED, task.getDownloadSpeed() + "kbps | "
                        + task.getDownloadSize() + " / " + task.getTotalSize());
                updateIntent.putExtra(MyIntents.PROCESS_PROGRESS, task.getDownloadPercent() + "");
                updateIntent.putExtra(MyIntents.URL, task.getUrl());
                sendBroadcast(updateIntent);
            }

            @Override
            public void preDownload(DownloadTask task) {
            }

            @Override
            public void finishDownload(DownloadTask task) {
            }

            @Override
            public void errorDownload(DownloadTask task, Throwable error) {
            }
        };
        
        return new DownloadTask(this, url, StorageUtils.FILE_ROOT, taskListener);
    }
}
