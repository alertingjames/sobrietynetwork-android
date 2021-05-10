package com.sobriety.sobriety.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.main.LoginActivity;
import com.sobriety.sobriety.main.NotificationActivity;

// Created by batman on 3/4/18.


public class FcmMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        Log.d("MESSAGE+++", message);

//        Intent intent = new Intent(this, LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
//        notificationBuilder.setContentTitle(title);
//        notificationBuilder.setContentText(message);
//        notificationBuilder.setSmallIcon(R.mipmap.appicon);
//        notificationBuilder.setAutoCancel(true);
//        notificationBuilder.setContentIntent(pendingIntent);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notificationBuilder.build());
    }
}
