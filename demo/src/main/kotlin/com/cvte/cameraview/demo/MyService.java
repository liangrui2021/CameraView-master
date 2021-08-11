package com.cvte.cameraview.demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    String notificationChannelId = "notification_channel_id_01";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //2.初始化一个notification的对象
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0 通知栏专属
            NotificationChannel notificationChannel = new
                    NotificationChannel(notificationChannelId,"我的应用名称",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(notificationChannelId);
        }
        // 自定义通知栏样式用到RemoteViews ，第一个参数固定写法，第二个布局id
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.activity_camera);

//        // 设置点击事件 1
//        Intent cleatInt = new Intent(this, AAAAActivity.class);
//        cleatInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent clearIntent = PendingIntent.getActivity(this,0,cleatInt,PendingIntent.FLAG_UPDATE_CURRENT);
//        remoteViews.setOnClickPendingIntent(R.id.imgToClear,clearIntent);
//        // 设置点击事件 2
//        Intent speedInt = new Intent(this, BBBBActivity.class);
//        speedInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent speedIntent = PendingIntent.getActivity(this,12,speedInt,0);
//        remoteViews.setOnClickPendingIntent(R.id.imgToSpeed,speedIntent);
        //这里把布局 设置到Content中
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.mipmap.logo);
        Notification notification = builder.build();
        notificationManager.notify(1,notification);
        startForeground(1,notification);

        return super.onStartCommand(intent, flags, startId);

    }
}