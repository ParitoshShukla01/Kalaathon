package kalaathon.com;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import kalaathon.com.start.OpenLink;

import static android.graphics.Color.rgb;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        Map<String, String> extraData = remoteMessage.getData();

        String id = extraData.get("media_id");
        String type = extraData.get("type");
        String user_id=extraData.get("user_id");

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "kalaathon.com")
                        .setContentTitle(title)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setColor(rgb(4,42,55))
                        .setSmallIcon(R.drawable.ic_notification_);

        Intent intent;
        if (!(type ==null)) {
            intent = new Intent(this, OpenLink.class);
            intent.putExtra("id", id);
            intent.putExtra("type", type);
            intent.putExtra("user_id",user_id);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("kalaathon.com","default", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify((int)System.currentTimeMillis(),notificationBuilder.build());

    }
}
