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
import android.widget.Toast;
import me.li2.android.downloaddemo.service.DownloadService;
import me.li2.android.downloaddemo.service.DownloadService.DownloadBinder;

public class DownloadActivity extends FragmentActivity {
    private static final String TAG = "DownloadActivity";
    private static final String URL ="http://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    
    private DownloadService mService;
    boolean mBound = false;
    
    private ProgressBar mLoadingProgressBar;
    private TextView mLoadingProgressTv;
    private Button mDownloadBtn;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Log.d(TAG, "onCreate() called");
        Toast.makeText(this, "onCreate() called", Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_download);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loadignProgressBar);
        mLoadingProgressTv = (TextView) findViewById(R.id.loadingProgressTv);
        
        mDownloadBtn = (Button) findViewById(R.id.downloadBtn);
        mDownloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    mDownloadBtn.setEnabled(false);
                    mService.startDownloadTask(URL);
                }
            }
        });
        
        int progress = 0;
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "onDestroy() called", Toast.LENGTH_SHORT).show();        
        Log.d(TAG, "onDestroy() called");
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called");
            Toast.makeText(DownloadActivity.this, "onServiceConnected() called", Toast.LENGTH_SHORT).show();        
            DownloadBinder binder = (DownloadBinder) service; 
            mService = binder.getService();
            mBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called");
            Toast.makeText(DownloadActivity.this, "onServiceDisconnected() called", Toast.LENGTH_SHORT).show();        
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
                        long progress = intent.getLongExtra(MyIntents.PROCESS_PROGRESS, 3);
                        mLoadingProgressBar.setProgress((int)progress);
                        mLoadingProgressTv.setText("" + progress + "%");
                        break;
                    default:
                        break;
                }
            }
        }
    };
    
}
