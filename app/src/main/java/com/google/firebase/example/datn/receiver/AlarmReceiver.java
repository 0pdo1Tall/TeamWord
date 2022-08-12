package com.google.firebase.example.datn.receiver;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.example.datn.GroupActivity;
import com.google.firebase.example.datn.MainActivity;
import com.google.firebase.example.datn.R;

public class AlarmReceiver extends BroadcastReceiver
{
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String title = intent.getStringExtra(AlarmReceiver.TITLE);
        Toast.makeText(context, "Alarm! " + title, Toast.LENGTH_SHORT).show();
        String description = intent.getStringExtra(AlarmReceiver.DESCRIPTION);

        Intent i = new Intent(context, GroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"channelId")
                .setSmallIcon(R.drawable.ic_word_white_24px)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123,builder.build());
    }
}