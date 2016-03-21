package in.bucksbuddy.bucksbuddy;

/**
 * Created by shrukul on 24/1/16.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PushNotificationService extends GcmListenerService {

    public static final int MESSAGE_NOTIFICATION_ID = 435345;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String typ = "Transaction Successful";
        System.out.println(message);
        String type = data.getString("type");
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        if(type.equals("1")){
            message = "Thank You for Signing Up. Your Pin is : " + message;
            typ = "Pin Code";
        } else if(type.equals("2")) {
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            db.addContact(new Person("Name" ,formattedDate, R.drawable.profile, message, 1));
            db.close();
            message = "Dear Customer, Your Account has been credited ₹ " + message;
            session.setBalance(""+(Integer.parseInt(session.getBalance())+Integer.parseInt(message)));
        } else if(type.equals("3")) {
            message = "Dear Customer, Your Account has been debited ₹ " + message;
//            session.setBalance(""+(Integer.parseInt(session.getBalance())-Integer.parseInt(message)));
        }
        createNotification("bucksbuddy", message, typ);
    }

    private void createNotification(String title, String body, String type) {
        Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        Context context = getBaseContext();
        System.out.println("Received Notification");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notify)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_app))
                .setContentText(type)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification foregroundNote = mBuilder.build();
        mNotificationManager.notify(0, foregroundNote);
    }
}
