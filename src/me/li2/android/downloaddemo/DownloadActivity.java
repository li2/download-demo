package me.li2.android.downloaddemo;

import com.yyxu.download.utils.MyIntents;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import me.li2.android.downloaddemo.service.DownloadService;
import me.li2.android.downloaddemo.service.DownloadService.DownloadBinder;

public class DownloadActivity extends FragmentActivity {
    private static final String TAG = "Download";
    private static final String URL1 ="http://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    private static final String URL2 = "http://img.yingyonghui.com/apk/16457/com.rovio.angrybirdsspace.ads.1332528395706.apk";
    
    private DownloadService mService;
    boolean mBound = false;
    
    private ProgressBar mLoadingProgressBar;
    private TextView mLoadingProgressTv;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_download);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loadignProgressBar);
        mLoadingProgressTv = (TextView) findViewById(R.id.loadingProgressTv);
        
        final String url = URL2;
        Button downloadBtn = (Button) findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    mService.startDownloadTask(url);
                }
            }
        });
        
        int progress = 22;
        mLoadingProgressBar.setProgress(progress);
        mLoadingProgressTv.setText("" + progress + "%");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to DownloadService
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        //
        IntentFilter filter = new IntentFilter(DownloadService.ACTION_DOWNLOADING_STATUS);
        registerReceiver(mDownloadingStatusReceiver, filter);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        //
        unregisterReceiver(mDownloadingStatusReceiver);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadBinder binder = (DownloadBinder) service; 
            mService = binder.getService();
            mBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
    
    private BroadcastReceiver mDownloadingStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(DownloadService.ACTION_DOWNLOADING_STATUS)) {
                int type = intent.getIntExtra(MyIntents.TYPE, -1);
                
                switch (type) {
                    case MyIntents.Types.PROCESS:
                        int progress = intent.getIntExtra(MyIntents.PROCESS_PROGRESS, 0);
                        mLoadingProgressBar.setProgress(progress);
//                        Log.d(TAG, "progress " + progress);
                        mLoadingProgressTv.setText("" + progress + "%");
                        break;
                    default:
                        break;
                }
            }
        }
    };
    
}
