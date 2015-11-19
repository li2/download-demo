# Q

想实现一个后台下载的功能，类似百度地图离线包下载，当下载任务在进行时可以返回其他界面进行其他的操作，下载列表所在的界面为activity1，其他界面为activity2，当在activity1中点击几个任务进行下载时，返回了activity2进行其他操作，过一段时间后再进入activity1怎么显示当前的下载进度？

http://segmentfault.com/q/1010000004001588/a-1020000004005912

# A

下载任务显然需要在主线程之外处理。
而从当前执行下载任务的activity按了返回键，然后再次进入后，该activity已经被销毁并且重建了，并且一般情况下，在activity被销毁后，我们应该清理新建的线程，以避免无法预料的后果。所以activity重建后显示当前下载信息的各种UI组件也就无法显示正确的状态了。
而Service与UI无关，可以在后台长时间运行；Bound service 更是允许 app 组件和 service 沟通：发送请求（比如下载请求）、获取状态（比如下载进度）。

所以，一种可行的解决办法是：

1. AsyncTask 负责另起一个线程下载，并且定义一个接口，实现该接口就可以获得下载进度及其它下载信息。
2. Bound Service 负责在后台启动 AsyncTask，并且管理该下载任务；实现 AsyncTask 定义的接口，把获取的下载信息以广播的形式发送出去；
3. Activity 在绑定到 service 后，就可以通过 service 启动一个下载任务；然后实现一个 broadcast receiver，接收下载信息并更新UI.

如果对service不了解，可以点这里[链接1](http://developer.android.com/guide/components/services.html)；对绑定也不了解，可以点这里[链接2](http://developer.android.com/guide/components/bound-services.html)。


## AsyncTask负责另起一个线程下载

```java
public class DownloadTask extends AsyncTask<Void, Integer, Long> {
    // 维护下载大小、下载进度、下载速率等信息。
    private long downloadPercent;

    // 构造器，指定下载的url和存储路径。
    public DownloadTask(Context context, String url, String path);
    
    @Override
    protected Long doInBackground(Void... params) {
        // 根据获取的url，执行下载任务。
    }
    
    @Override
    protected void onProgressUpdate(Integer... progress) {
        // 定义一个callback，实现该callback的即可获得进度的更新。
        if (listener != null)
            listener.updateProcess(this);
    }
```

## Bound Service负责在后台启动AsyncTask，并管理下载任务

```java
public class DownloadService extends Service {    
    // Activity绑定该service后，就可以调用这个方法启动一个下载任务。
    public void startDownloadTask(String url) {
        DownloadTask task = newDownloadTask(url);
        task.execute();    
    }
    
    private DownloadTask newDownloadTask(String url) throws MalformedURLException {
        // 实现该接口就可以获得下载进度及其它下载信息，然后以广播的形式把获取的信息发送出去。
        DownloadTaskListener taskListener = new DownloadTaskListener() {
            @Override
            public void updateProcess(DownloadTask task) {
                Intent updateIntent = new Intent(ACTION_DOWNLOADING_STATUS);
                updateIntent.putExtra(MyIntents.PROCESS_PROGRESS, task.getDownloadPercent());
                sendBroadcast(updateIntent);
            }
        };
        return new DownloadTask(this, url, /path/to/store/, taskListener);
    }
}
```
    
## Activity负责绑定service，实现一个 broadcast receiver，接收下载信息并更新UI


```java
public class DownloadActivity extends FragmentActivity {
    // 在onStart中绑定service，注册receiver
    protected void onStart();

    // 在onStop中取消绑定，取消注册receiver
    protected void onStop();
    
    // 实现一个ServiceConnection，在onServiceConnected()回调方法中获取service
    private ServiceConnection mConnection = new ServiceConnection() {}
    
    // 实现一个BroadcastReceiver，在onReceive()回调方法中获取intent，从中解析下载信息然后更新UI.
    private BroadcastReceiver mDownloadingStatusReceiver = new BroadcastReceiver() {}
    
    // 获取service后，就可以启动一个下载任务了。
    mService.startDownloadTask(URL);
}
```

------

完整的代码可以从这里获取 https://github.com/li2/DownloadDemo
这仅仅是一个demo，用于演示bound service和async task是如何实现后台下载任务的。
该Demo从这个项目里[yingyixu/android-download-manager](https://github.com/yingyixu/android-download-manager) (1) 借用了核心代码；(2) 使用 Binder class实现service，代替原工程中使用的AIDL方式（因为该方式实现复杂，而Binder已经可以满足你的需求，而且容易理解）；(3) 删除了管理多个下载任务的代码。

建议了解实现方法后，直接使用第三方框架。


## Update 如何更新各自的ProgressBar

每个进度条都对应惟一一个URL，那么就可以把URL作为tag：View.setTag(url)。因此，只要知道URL，就能找到对应的View。当接收到进度更新的数据时（数据中必然需要包含URL），就可以更新相应的View. 