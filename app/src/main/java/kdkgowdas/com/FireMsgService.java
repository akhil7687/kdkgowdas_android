package kdkgowdas.com;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireMsgService extends FirebaseMessagingService {
 
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
 
        Log.d("Msg", "Message received ["+remoteMessage+"]");

        if (remoteMessage.getData().size() > 0) {

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String url = remoteMessage.getData().get("url");
            String notificaiton_id = remoteMessage.getData().get("notification_id");

            // Create Notification
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("url",url);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, notificaiton_id.hashCode(),
                    intent, PendingIntent.FLAG_ONE_SHOT);


            NotificationCompat.Builder notificationBuilder = new
                    NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(title.hashCode(), notificationBuilder.build());
        }
    }
}