package me.li2.android.downloaddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/*
为了解决问题：Android后台下载问题
http://segmentfault.com/q/1010000004001588
*/

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main);
        Button search = (Button) findViewById(R.id.downloadBtn);
        search.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            }
        });
    }
}
