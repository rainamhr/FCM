package com.example.queen.fcm.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.queen.fcm.R;
import com.example.queen.fcm.activity.MainActivity;
import com.example.queen.fcm.app.Config;
import com.example.queen.fcm.utils.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.R.id.message;
import static android.content.ContentValues.TAG;
import static com.example.queen.fcm.R.attr.title;

/**
 * Created by queen on 7/12/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(JSONObject data) {
        Log.e("handle message", "successasa");
        notification(data);

        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
        try {
            pushNotification.putExtra("message", data.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

        // play notification sound
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.playNotificationSound();
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");
            String videoUrl = (String) payload.get("url");



            Log.e(TAG, "title: " + title);  
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "payload: " + payload.get("type"));
            Log.e(TAG, "imageUrl: " + imageUrl);
            Log.e(TAG, "timestamp: " + timestamp);
            Log.e(TAG, "url" + videoUrl);

            SharedPreferences.Editor saveData = getSharedPreferences("datapayload", MODE_PRIVATE).edit();
            saveData.clear();
            saveData.putString("type", imageUrl);
            saveData.putString("videoURL", videoUrl);
            saveData.commit();
            handleNotification(data);

           /* if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }*/
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    public void notification(JSONObject data) {

        try {
            Log.d("notification", data.getString("title"));
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // intent.putExtra("type",data.getString("type"));
            // intent.putExtra("type",data.getString("videoURL"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notficationBuilder = new NotificationCompat.Builder(this);
            notficationBuilder.setContentTitle(data.getString("title"));
            notficationBuilder.setContentText(data.getString("message"));
            notficationBuilder.setAutoCancel(true);
            notficationBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
            notficationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notficationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
