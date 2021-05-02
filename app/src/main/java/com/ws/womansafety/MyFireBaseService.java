package com.ws.womansafety;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ws.womansafety.model.Guardian;
import com.ws.womansafety.splash.SplashActivity;
import com.ws.womansafety.utils.SharedPrefConst;
import com.ws.womansafety.utils.SharedPrefUtils;

public class MyFireBaseService extends FirebaseMessagingService {
    String title, message;
    private NotificationManagerCompat notificationManager;
    public final String CHANNEL_1_ID = "channel1";
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String userType = SharedPrefUtils.getStringData(SharedPrefConst.USER_TYPE);
        if (userType.equalsIgnoreCase(getString(R.string.str_guardian))) {
            String name = SharedPrefUtils.getStringData(SharedPrefConst.USER_NAME);
            String mob = SharedPrefUtils.getStringData(SharedPrefConst.USER_NO);
            Guardian guardian = new Guardian(s, name, mob);
            FirebaseDatabase.getInstance().getReference(getString(R.string.str_guardian)).child(mob).setValue(guardian);
        }
        SharedPrefUtils.saveStringData(SharedPrefConst.AUTH_KEY, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        title = remoteMessage.getData().get("Title");
        message = remoteMessage.getData().get("Message");
        Log.e("Info ", " Notification Received " + title + "    " + message);

        notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }


        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);



        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_app)
                .setContentTitle(title)
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1, notification);






    }
}