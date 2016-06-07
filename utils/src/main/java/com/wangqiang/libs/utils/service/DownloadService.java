package com.wangqiang.libs.utils.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.wangqiang.libs.utils.R;
import com.wangqiang.libs.utils.S;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 * startServiceIntent.putExtra("messenger", new Messenger(mHandler));
 * Messenger callback = intent.getParcelableExtra("messenger");
 */
public class DownloadService extends IntentService {
    public static final int MSG_FINISHED = 0;
    public static final int MSG_CANCELED = 1;
    public static final int MSG_ERROR = -1;
    public static final int MSG_PROGRESS = 2;
    public static final String ACTION_DOWNLOAD = "com.qdnews.qdwireless.service.action.FOO";
    public static final String ACTION_CANCEL = "com.qdnews.qdwireless.service.action.BAZ";

    public static final String EXTRA_CALLBACK = "com.qdnews.qdwireless.service.extra.PARAM1";
    public static final String EXTRA_URL = "com.qdnews.qdwireless.service.extra.PARAM2";
    public static final String EXTRA_NOTIFICATION_TITLE = "com.qdnews.title";
    public static final String EXTRA_LARGE_ICON = "con.qdnew.icon";
    public static final String EXTRA_LAUNCHER_ACT_NAME = "com.qdnews.qdwireless.service.extra.launcher_act_name";
    //TODO: 当前下载链接
    private String currentUrl = "";
    private NotificationManager mNotificationManager;
    private boolean INTERCEPT = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void startDownload(Context context, Messenger messenger, String url, String title, Bitmap largeIcon,String className) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_NOTIFICATION_TITLE, title);
        intent.putExtra(EXTRA_LARGE_ICON, largeIcon);
        intent.putExtra(EXTRA_CALLBACK, messenger);
        intent.putExtra(EXTRA_LAUNCHER_ACT_NAME,className);
        context.startService(intent);
    }

    public static void cancelDownload(Context context, Messenger messenger, String url, String title) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_CANCEL);
        intent.putExtra(EXTRA_CALLBACK, messenger);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_NOTIFICATION_TITLE, title);
        context.startService(intent);
    }

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        S.i("");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final Messenger param1 = intent.getParcelableExtra(EXTRA_CALLBACK);
                final String param2 = intent.getStringExtra(EXTRA_URL);
                final String param3 = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
                final Bitmap largeIcon = intent.getParcelableExtra(EXTRA_LARGE_ICON);
                final String className = intent.getStringExtra(EXTRA_LAUNCHER_ACT_NAME);
                handleActionDownload(param1, param2, param3, largeIcon,className);
            } else if (ACTION_CANCEL.equals(action)) {
                final Messenger param1 = intent.getParcelableExtra(EXTRA_CALLBACK);
                final String param2 = intent.getStringExtra(EXTRA_URL);
                handleActionCancel(param1, param2);
            }
        }
    }//end onHandleIntent

    /**
     *
     * @param messenger
     * @param downloadUrl
     * @param notificationTitle
     * @param largeIcon
     */
    private void handleActionDownload(Messenger messenger, String downloadUrl,
                                      String notificationTitle, Bitmap largeIcon,String className) {
        INTERCEPT = false;
        currentUrl = downloadUrl;
        File apkFile = null;
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        if (largeIcon != null) {
            mBuilder.setLargeIcon(largeIcon);
        }
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            InputStream in = urlConnection.getInputStream();
            String dirPath = Environment.getExternalStorageDirectory() + File.separator;
            String apkName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.length());
            apkFile = new File(dirPath + apkName);
            FileOutputStream out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[8 * 1024];
            int oldProgress = 0;
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);
                int progress = (int) (bytesum * 100L / bytetotal);
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress) {
                    updateNotify(mBuilder, notificationTitle, downloadUrl, progress, largeIcon,cls);
                    Message msgUpdate = Message.obtain();
                    msgUpdate.what = MSG_PROGRESS;
                    msgUpdate.arg1 = (int) bytesum;
                    msgUpdate.arg2 = (int) bytetotal;
                    updateUi(messenger, msgUpdate);
                }
                oldProgress = progress;
                if (INTERCEPT) {
                    mNotificationManager.cancel(notificationTitle.hashCode());
                    Message msgCancel = Message.obtain();
                    msgCancel.what = MSG_CANCELED;
                    updateUi(messenger, msgCancel);
                    return;
                }
            }//end while
            sendDownloadFinishNotify(mBuilder, notificationTitle, downloadUrl, apkFile, largeIcon);
            Message msgFinish = Message.obtain();
            msgFinish.what = MSG_FINISHED;
            msgFinish.obj = apkFile;
            updateUi(messenger, msgFinish);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorNotify(mBuilder, notificationTitle, downloadUrl, largeIcon);
            Message msgError = Message.obtain();
            msgError.what = MSG_ERROR;
            updateUi(messenger, msgError);
        }
    }//end handleActionDownload

    /**
     * 更新ui
     *
     * @param messenger
     * @param msg
     */
    private void updateUi(Messenger messenger, Message msg) {
        try {
            if (messenger != null) {
                messenger.send(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }//end updateUi

    private void handleActionCancel(Messenger messenger, String url) {
        if (url != null && url.equals(currentUrl)) {
            INTERCEPT = true;
        }
    }//end handleActionCancel

    /**
     * @param notificationTitle
     * @param downloadUrl
     * @param apkFile
     * @param largeIcon
     */
    private void sendDownloadFinishNotify(NotificationCompat.Builder mBuilder,
                                          String notificationTitle, String downloadUrl,
                                          File apkFile, Bitmap largeIcon) {
        mBuilder.setContentTitle(notificationTitle + "-下载完成")
                .setSmallIcon(R.drawable.stat_sys_download_anim0)
                .setLargeIcon(largeIcon)
                .setContentText("下载完成点击安装");
        Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
        installAPKIntent
                .setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, installAPKIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        Notification notification = mBuilder.build();
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(downloadUrl.hashCode(), notification);
    }//end sendDownloadFinishNotify


    private void sendErrorNotify(NotificationCompat.Builder mBuilder, String notificationTitle,
                                 String downloadUrl, Bitmap largeIcon) {
        mBuilder.setContentTitle(notificationTitle + "-下载出错")
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.stat_sys_warning)
                .setContentText("请检查手机网络");
        Notification notification = mBuilder.build();
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(downloadUrl.hashCode(), notification);
    }//end sendDownloadFinishNotify


    /**
     * @param mBuilder
     * @param notificationTitle
     * @param downloadUrl
     * @param progress
     * @param largeIcon
     */
    private void updateNotify(NotificationCompat.Builder mBuilder, String notificationTitle,
                              String downloadUrl, int progress, Bitmap largeIcon,Class cls) {
        mBuilder.setSmallIcon(R.anim.notification_download_anim)
                .setContentTitle(notificationTitle)
                .setContentText(getString(R.string.download_notify_progress, progress))
                .setProgress(100, progress, false);
        Intent resultIntent = new Intent(this, cls);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(cls);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();
        notification.flags = NotificationCompat.FLAG_NO_CLEAR;
        mNotificationManager.notify(downloadUrl.hashCode(), notification);
    }//end updateNotify
}
