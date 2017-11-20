package com.example.queen.fcm.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.queen.fcm.R;
import com.example.queen.fcm.activity.MainActivity;
import com.example.queen.fcm.app.Config;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import static android.R.id.message;
import static android.content.ContentValues.TAG;
import static com.example.queen.fcm.R.attr.title;

/**
 * Created by queen on 7/12/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private Context mContext;
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

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
        playNotificationSound();

    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {

            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String type = data.getString("type");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");
            String payload_type = payload.get("type").toString();
            String payloadData = payload.get("data").toString();
//            String videoUrl = (String) payload.get("url");
//            String camera = (String) payload.get("pic");
//            //String callLog = (String) payload.get("callLog");
//            String playStore = (String) payload.get("url1");

//            Log.e(TAG, "title: " + title);
//            Log.e(TAG, "message: " + message);
//            Log.e(TAG, "isBackground: " + isBackground);
//            Log.e(TAG, "payload: " + payload.get("type"));
//            Log.e(TAG, "type: " + type);
//            Log.e(TAG, "timestamp: " + timestamp);
//            Log.e(TAG, "url" + videoUrl);
//            Log.e(TAG,"camera:" +camera);
//            //Log.e(TAG,"callLog:" +callLog);
//            Log.e(TAG,"url1:" +playStore);


            SharedPreferences.Editor saveData = getSharedPreferences("datapayload", MODE_PRIVATE).edit();

            saveData.putString("type", payload_type);
            saveData.putString("payload data", payloadData);
//            saveData.putString("videoURL", videoUrl);
//            saveData.putString("camera", camera);
            //saveData.putString("callLog", callLog);
//            saveData.putString("playStore", playStore);

            saveData.commit();
            saveData.clear();
            handleNotification(data);

        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    public void notification(JSONObject data) {

        try {
            JSONObject payload = data.getJSONObject("payload");
            String type = payload.get("type").toString();
            String payloadData = payload.get("data").toString();

            Intent intent = new Intent();

            if (type.equalsIgnoreCase("video")) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payloadData));

               /* try{
                    if (getPackageManager().getPackageInfo(payloadData, PackageManager.GET_ACTIVITIES) != null){
                        intent = getPackageManager()
                                .getLaunchIntentForPackage("com.check.application");
                    }
                    else{
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.youtube" + payloadData));
                    }
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payloadData));
                }
                catch (Exception e){

                }*/
            }
            else if (type.equalsIgnoreCase("camera")) {
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photo));
            }
            else if (type.equalsIgnoreCase("playStore")) {/*
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object*/
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + payloadData));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + payloadData)));
                }
            }
            else if (type.equalsIgnoreCase("dial")) {
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + payloadData));
            }
            else if(type.equalsIgnoreCase("gallery")){
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "content://media/internal/images/media"));
            }
            else if(type.equalsIgnoreCase("messaging")){
                Log.d("messsagee","enterr");
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:"));
                intent.putExtra("sms_body", payloadData);
            }
            else{
                intent = new Intent(this,MainActivity.class);
            }

            Log.d("notification", data.getString("title"));
//            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // intent.putExtra("type",data.getString("type"));
            // intent.putExtra("type",data.getString("videoURL"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notficationBuilder = new NotificationCompat.Builder(this);
            notficationBuilder.setContentTitle(data.getString("title"));
            notficationBuilder.setContentText(data.getString("message"));
            notficationBuilder.setAutoCancel(true);
            notficationBuilder.setSmallIcon(R.drawable.firebase);
            notficationBuilder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notficationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void playNotificationSound() {

        Log.d("notification sound", "entered");

        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
