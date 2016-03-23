package in.bucksbuddy.bucksbuddy;

/**
 * Created by shrukul on 18/3/16.
 */
import com.google.android.gms.gcm.GcmListenerService;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GcmMessageHandler extends GcmListenerService {
    public static final int MESSAGE_NOTIFICATION_ID = 435345;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String balance = data.getString("balance");
        String typ = "Transaction Successful";
        String type = data.getString("type");
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        if(type.equals("0")){
            message = "Thank You for Signing Up. Your Pin is : " + message;
            typ = "Pin Code";
        } else if(type.equals("1")) {
            session.setBalance(""+Integer.parseInt(balance));
            message = "Dear Customer, Your Account has been credited ₹ " + message;
        } else if(type.equals("-1")) {
            message = "Dear Customer, Your Account has been debited ₹ " + message;
//            session.setBalance(""+(Integer.parseInt(session.getBalance())-Integer.parseInt(message)));
        }

        createNotification(typ, message);
    }

    // Creates notification based on title and body received
    private void createNotification(String typ, String body) {
        Context context = getBaseContext();
/*        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notify).setContentTitle(title)
                .setContentText(body);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());*/
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        android.support.v7.app.NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle("bucksbuddy")
                .setContentText(typ)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.app_icon))
                .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(body));;
        notificationManager.notify(1, mBuilder.build());
    }

}
