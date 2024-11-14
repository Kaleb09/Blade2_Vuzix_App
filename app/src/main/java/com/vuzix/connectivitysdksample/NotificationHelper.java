package com.vuzix.connectivitysdksample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "my_notification_channel";
    private static final int NOTIFICATION_ID = 1;
    Context context;
public  NotificationHelper(Context context){
    this.context=context;
}
    public  void sendNotification(String title,String Sku/*Context context*/) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Notification Channel";
            String description = "Channel for my notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivitytwo.class);
       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Sku)
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set a notification category for Android 8.0 and later
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      /*  if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"accept notification permission ",Toast.LENGTH_SHORT).show();

            return;
        }*/
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}