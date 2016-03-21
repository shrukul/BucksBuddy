package in.bucksbuddy.bucksbuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by shrukul on 27/1/16.
 */
public class GcmIntentService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
//        String message = "Yay";

        String message = data.getString("message");
        String typ = "Transaction Successful";
        String type = data.getString("type");
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        if(type.equals("1")){
            message = "Thank You for Signing Up. Your Pin is : " + message;
            typ = "Pin Code";
        } else if(type.equals("2")) {
            session.setBalance(""+(Integer.parseInt(session.getBalance())+Integer.parseInt(message)));
            message = "Dear Customer, Your Account has been credited ₹ " + message;
        } else if(type.equals("3")) {
            message = "Dear Customer, Your Account has been debited ₹ " + message;
//            session.setBalance(""+(Integer.parseInt(session.getBalance())-Integer.parseInt(message)));
        }

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("bucksbuddy")
                .setContentText(typ)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_app))
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(message));;
        notificationManager.notify(1, mBuilder.build());

/*        Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        Context context = getBaseContext();
        System.out.println("Received Notification");
        android.support.v4.app.NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notify)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentTitle("bucksbuddy")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_app))
                .setContentText(type)
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(message));
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification foregroundNote = mBuilder.build();
        mNotificationManager.notify(0, foregroundNote);*/
    }
}
