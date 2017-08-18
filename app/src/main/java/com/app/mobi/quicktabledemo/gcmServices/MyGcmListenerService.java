package com.app.mobi.quicktabledemo.gcmServices;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.activities.BookYourTableActivity;
import com.app.mobi.quicktabledemo.activities.ListOfRestaurantActivity;
import com.app.mobi.quicktabledemo.activities.OrderConfirmationActivity;
import com.app.mobi.quicktabledemo.activities.PageScreenActivity;
import com.app.mobi.quicktabledemo.activities.RegistrationActivity;
import com.app.mobi.quicktabledemo.fragments.MenuItemFragment;
import com.app.mobi.quicktabledemo.fragments.WaitConformationFragment;
import com.app.mobi.quicktabledemo.modelClasses.MenuItemModel;
import com.app.mobi.quicktabledemo.modelClasses.QTOffers;
import com.app.mobi.quicktabledemo.utils.Config;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mobigesture-hp on 28/7/15.
 */
public class MyGcmListenerService extends GcmListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String msg = data.getString("message");
        String RestaurantName = data.getString("name");
        String RestaurantAddress = data.getString("address_line");
        String RestaurantPhone = data.getString("phone_number");
        String RestaurantCity = data.getString("city");
        String RestaurantState = data.getString("state");
        String RestaurantZip = data.getString("zip");
        String restaurantImage = data.getString("url");
        String offerImage = data.getString("client_url");
        String type = data.getString("type");

        Log.i("GCMListener", from);
        Log.i("GCMListener", msg);

//        sendNotification(msg);
        if (type.equals("page")) {
            setLayout(msg, RestaurantName, RestaurantAddress, RestaurantPhone, RestaurantCity,
                    RestaurantState, RestaurantZip, restaurantImage);
        } else if (type.equals("push")) {
            new generatePictureStyleNotification(this, RestaurantName, msg, Config.QUICK_CHAT_IMAGE + restaurantImage).execute();
            savePushData(data);
        }

//        new generatePictureStyleNotification(this,"Title", "Message", "http://api.androidhive.info/images/sample.jpg").execute();

    }

    private void setLayout(String msg, String RestaurantName, String RestaurantAddress, String RestaurantPhone,
                           String RestaurantCity, String RestaurantState, String RestaurantZip, String RestaurantImage) {

        Intent intent = new Intent(MyGcmListenerService.this, PageScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("message", msg);
        intent.putExtra("rest_name", RestaurantName);
        intent.putExtra("rest_address", RestaurantAddress);
        intent.putExtra("rest_phone", RestaurantPhone);
        intent.putExtra("rest_city", RestaurantCity);
        intent.putExtra("rest_state", RestaurantState);
        intent.putExtra("rest_zip", RestaurantZip);
        intent.putExtra("rest_image", RestaurantImage);
        startActivity(intent);

    }

    private void sendNotification(String msg) {
        Intent intent = null;
        SharedPreferences preferences = getSharedPreferences("Dine_In", Context.MODE_PRIVATE);
        boolean dineIn = preferences.getBoolean("isDineInOnly", false);
        if (dineIn) {
            intent = new Intent(this, BookYourTableActivity.class);
            intent.putExtra("isDineIn", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(this, OrderConfirmationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Quick Table")
                .setSmallIcon(R.mipmap.qt_icon)
                .setContentText(msg)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSound(soundURI)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String title, message, imageUrl;

        public generatePictureStyleNotification(Context context, String title, String message, String imageUrl) {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            Intent intent = new Intent(mContext, RegistrationActivity.class);
            intent.putExtra("key", "value");
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 100, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notif = new Notification.Builder(mContext)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.mipmap.qt_icon)
                    .setSound(soundURI)
                    .setLargeIcon(result)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setStyle(new Notification.BigPictureStyle().bigPicture(result).setSummaryText(message))
                    .build();
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, notif);
        }
    }

    private void savePushData(Bundle data){

        SharedPreferences preferences = getSharedPreferences("qt_offers", MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
        Date date = Calendar.getInstance().getTime();
        String time = dateFormat.format(date);

        if (!preferences.contains("offers")){
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("message", data.getString("message"));
                jsonObject.put("rest_name", data.getString("name"));
                jsonObject.put("rest_address", data.getString("address_line"));
                jsonObject.put("rest_phone", data.getString("phone_number"));
                jsonObject.put("rest_city", data.getString("city"));
                jsonObject.put("rest_state", data.getString("state"));
                jsonObject.put("rest_zip", data.getString("zip"));
                jsonObject.put("offer_image", data.getString("url"));
                jsonObject.put("rest_image", data.getString("client_url"));
                jsonObject.put("time", time);
                jsonArray.put(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("offers", jsonArray.toString());
            editor.commit();
        }else {

            String offers = preferences.getString("offers",null);

            try {
                JSONArray offersArray = new JSONArray(offers);
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", data.getString("message"));
                jsonObject.put("rest_name", data.getString("name"));
                jsonObject.put("rest_address", data.getString("address_line"));
                jsonObject.put("rest_phone", data.getString("phone_number"));
                jsonObject.put("rest_city", data.getString("city"));
                jsonObject.put("rest_state", data.getString("state"));
                jsonObject.put("rest_zip", data.getString("zip"));
                jsonObject.put("offer_image", data.getString("url"));
                jsonObject.put("rest_image", data.getString("client_url"));
                jsonObject.put("time", time);
                jsonArray.put(0, jsonObject);

                for (int i=0; i<offersArray.length(); i++){
                    JSONObject object = offersArray.getJSONObject(i);
                    jsonArray.put(i+1, object);
                    if (i >= 49){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            jsonArray.remove(i);
                        }
                    }
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("offers", jsonArray.toString());
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }
}

