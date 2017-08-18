package com.app.mobi.quicktabledemo.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.adapters.ChatAdapter;
import com.app.mobi.quicktabledemo.adapters.DrawerListAdapter;
import com.app.mobi.quicktabledemo.modelClasses.LocationListModel;
import com.app.mobi.quicktabledemo.modelClasses.OfflineChatImagesModel;
import com.app.mobi.quicktabledemo.network.ChatMessage;
import com.app.mobi.quicktabledemo.network.ChatService;
import com.app.mobi.quicktabledemo.network.VolleySingleton;
import com.app.mobi.quicktabledemo.utils.ChatLocationSingleton;
import com.app.mobi.quicktabledemo.utils.Config;
import com.app.mobi.quicktabledemo.utils.ConnectionDetector;
import com.app.mobi.quicktabledemo.utils.GPSTracker;
import com.app.mobi.quicktabledemo.utils.Globals;
import com.app.mobi.quicktabledemo.utils.SpecificMenuSingleton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ChatActivity extends AppCompatActivity {

    Button quickChat, editOrder, getOutOfLine;
    TextView userNameTxt, userLocation, accuracyText;
    EditText imageCaption;
    DrawerLayout drawerLayout;
    ListView drawerList;
    ImageView homeBtn, loadingImage, imagePreview, imageDone, imageClose, userImage;
    RelativeLayout cartContent, chatLayout, chatScreen, imagePreviewScreen;
    public ListView listView;
    private WebView chatWebView;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadImage;
    //    public Uri[] imageUri;
    private boolean isPreLollipop;
    private ChatService chatService;
    private Random random;
    public ArrayList<ChatMessage> chatMessages;
    public ChatAdapter chatAdapter;
    private MultiUserChatManager manager;
    private MultiUserChat muc;
    private XMPPTCPConnection connection;
    String profileName;
    String userAddress;
    String userPhone;
    boolean isQtSupported;
    boolean isMeSending = false;
    String date = null;
    private static Uri imageUri;
    private double currentLatitude, currentLongitude, restLatitude, restLongitude;
    private float distance;
    private String restaurantName;
    private double accuracy;
    private EditText writeMsg;
    private int sendMsgCount = 0;
    private GPSTracker gpsTracker;
    private Handler handler;
    private boolean isFirstTime = true;
    private Runnable runnable;
    private ArrayList<OfflineChatImagesModel> offlineChatImagesModels;
    private boolean isXmppException = false;
    private int msgIndex = -1;
    private ArrayList<ChatMessage> finalTempChatMessages;
    private int totalMissingIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView = (ListView) findViewById(R.id.chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        cartContent = (RelativeLayout) findViewById(R.id.cart_content);
//        drawerList = (ListView) findViewById(R.id.drawer_list);
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
//        userNameTxt = (TextView) findViewById(R.id.user_name);
        userLocation = (TextView) findViewById(R.id.user_location);
        homeBtn = (ImageView) findViewById(R.id.menu_button);
        loadingImage = (ImageView) findViewById(R.id.loading_image);
        chatLayout = (RelativeLayout) findViewById(R.id.chat_layout);
        writeMsg = (EditText) findViewById(R.id.write_msg);
        ImageView sendMsg = (ImageView) findViewById(R.id.send_msg);
        ImageView sendImage = (ImageView) findViewById(R.id.send_image);
        chatScreen = (RelativeLayout) findViewById(R.id.chat_screen);
        imagePreviewScreen = (RelativeLayout) findViewById(R.id.image_preview_screen);
        imagePreview = (ImageView) findViewById(R.id.image_preview);
        imageDone = (ImageView) findViewById(R.id.image_done);
        imageClose = (ImageView) findViewById(R.id.image_close);
        imageCaption = (EditText) findViewById(R.id.image_caption);
        userImage = (ImageView) findViewById(R.id.user_image);
//        accuracyText = (TextView) findViewById(R.id.accuracy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
        }

//        SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
//        SharedPreferences.Editor editor = offlineImagesData.edit();
//        Gson gson = new Gson();
//        String json1 = gson.toJson(offlineChatImagesModels);
//        editor.putString("OfflineImages", json1);
//        editor.clear();
//        editor.apply();

        offlineChatImagesModels = new ArrayList<>();
        sendMsgCount = 0;

        final Intent intent = getIntent();
        final SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
        String placeId = null;
        if (SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantName() != null) {
            restaurantName = SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantName();
            placeId = SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID();
        }

        gpsTracker = new GPSTracker(this);
        gpsTracker.startLocation();
        if (gpsTracker.canGetLocation()) {
            currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();
            accuracy = gpsTracker.getAccuracy();
        }

        restLatitude = menuSingleton.getClickedRestaurant().getLatitude();
        restLongitude = menuSingleton.getClickedRestaurant().getLongitude();

        final Location currentLocation = new Location("current location");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);
        final Location restLocation = new Location("rest location");
        restLocation.setLatitude(restLatitude);
        restLocation.setLongitude(restLongitude);

        if (currentLocation.hasAccuracy()) {
            accuracy = currentLocation.getAccuracy();
        }

        distance = currentLocation.distanceTo(restLocation);

        toolbar.setTitle("QuickChat- " + restaurantName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                drawerLayout.openDrawer(Gravity.LEFT);
                Intent intent = new Intent(ChatActivity.this, RegistrationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

        cartContent.setVisibility(View.GONE);

        chatMessages = new ArrayList<ChatMessage>();
        finalTempChatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        listView.setAdapter(chatAdapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setStackFromBottom(true);

        distance = currentLocation.distanceTo(restLocation);
        System.out.println("distance = " + distance);
        if (distance > 320) {
            writeMsg.setFocusable(false);
        } else {
            writeMsg.setFocusable(true);
        }

        writeMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                GPSTracker gpsTracker = new GPSTracker(ChatActivity.this);
                if (gpsTracker.canGetLocation()) {
                    currentLatitude = gpsTracker.getLatitude();
                    currentLongitude = gpsTracker.getLongitude();
                }

                final Location currentLocation = new Location("current location");
                currentLocation.setLatitude(currentLatitude);
                currentLocation.setLongitude(currentLongitude);
                final Location restLocation = new Location("rest location");
                restLocation.setLatitude(restLatitude);
                restLocation.setLongitude(restLongitude);

                distance = currentLocation.distanceTo(restLocation);

//                if (currentLocation.hasAccuracy()){
//                    Toast.makeText(ChatActivity.this,""+currentLocation.getAccuracy(),Toast.LENGTH_SHORT);
//                }

                Log.i("Accuracy", "" + accuracy);
                if (distance > 320) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(ChatActivity.this, "Oops! You need to be at " +
                            restaurantName +
                            " to post a message or picture in QuickChat.", Toast.LENGTH_LONG).show();
                }
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gpsTracker = new GPSTracker(ChatActivity.this);
                if (gpsTracker.canGetLocation()) {
                    currentLatitude = gpsTracker.getLatitude();
                    currentLongitude = gpsTracker.getLongitude();
                }

                final Location currentLocation = new Location("current location");
                currentLocation.setLatitude(currentLatitude);
                currentLocation.setLongitude(currentLongitude);
                final Location restLocation = new Location("rest location");
                restLocation.setLatitude(restLatitude);
                restLocation.setLongitude(restLongitude);

                distance = currentLocation.distanceTo(restLocation);

                String msg = writeMsg.getText().toString();
                boolean charExist = false;
                for (int i = 0; i < msg.length(); i++) {
                    if (!(msg.charAt(i) == ' ' || msg.charAt(i) == '\n')) {
                        charExist = true;
                    }
                }

                if (distance < 320) {
                    if (!msg.equalsIgnoreCase("") && charExist) {
                        writeMsg.setText("");

                        // sending message to server
                        try {
                            Message message = new Message();
                            message.setBody(msg);
                            if (chatMessages.size() > 0){
                                msgIndex = chatMessages.get(chatMessages.size() - 1).getMsgIndex();
                            }else {
                                msgIndex = -1;
                            }
                            msgIndex++;
//                            msgIndex = 10;
                            String subject = profileName + "@" + String.valueOf(msgIndex);
                            message.setSubject(subject);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                            String time = dateFormat.format(new Date()).toString();
                            time = time.replace(".", "");
                            date = time + "@" + dateFormat1.format(new Date()).toString();
                            message.setThread(date);
                            muc.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // adding message to message list
                        ChatMessage chatMessage = new ChatMessage();
                        String[] timeStamp = date.split("@");
                        chatMessage.setMsgid(timeStamp[1]);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                        String time = dateFormat.format(new Date()).toString();
                        chatMessage.setTime(time);
                        chatMessage.setBody(msg);
                        chatMessage.setMine(true);
                        chatMessage.setUserName(profileName);
                        chatMessage.setMsgIndex(msgIndex);

                        // Offline msg
                        OfflineChatImagesModel offlineChatImagesModel = new OfflineChatImagesModel();
                        offlineChatImagesModel.setPlaceId(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
                        offlineChatImagesModel.setMsgid(timeStamp[1]);
                        offlineChatImagesModel.setTime(time);
                        offlineChatImagesModel.setMine(true);
                        offlineChatImagesModel.setUserName(profileName);
                        offlineChatImagesModel.setChatTime(date);
                        offlineChatImagesModel.setMsgIndex(msgIndex);
                        offlineChatImagesModel.setChatMsg(msg);
                        offlineChatImagesModels.add(offlineChatImagesModel);

                        SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
                        String json = offlineImagesData.getString("OfflineImages", null);
                        if (json != null && !json.equals("null")) {

                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
                            }.getType();
                            ArrayList<OfflineChatImagesModel> totalOfflineChatImagesModels = gson.fromJson(json, type);
//                                for (int i=0; i<offlineChatImagesModels.size(); i++){
                            totalOfflineChatImagesModels.add(offlineChatImagesModel);
//                                }
                            SharedPreferences.Editor editor = offlineImagesData.edit();
                            Gson gson1 = new Gson();
                            String json1 = gson1.toJson(totalOfflineChatImagesModels);
                            editor.putString("OfflineImages", json1);
                            editor.apply();

                        } else {
                            SharedPreferences.Editor editor = offlineImagesData.edit();
                            Gson gson = new Gson();
                            String json1 = gson.toJson(offlineChatImagesModels);
                            editor.putString("OfflineImages", json1);
                            editor.apply();
                        }

                        if (chatMessages.size() > 0) {
                            ChatMessage compareMessage = chatMessages.get(chatMessages.size() - 1);
                            if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                    chatMessage.setFirstMsg(false);
                                } else {
                                    chatMessage.setFirstMsg(true);
                                }
                            }
                            chatMessages.add(chatMessage);
                        } else {
                            chatMessage.setFirstMsg(true);
                            chatMessages.add(chatMessage);
                        }
                        isMeSending = true;
                        chatAdapter.notifyDataSetChanged();
                        listView.setSelection(chatAdapter.getCount() - 1);

//                        SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
//                        LocationListModel locationListModel = menuSingleton.getClickedRestaurant();
//                        locationListModel.setRestaurantChatAvailable(String.valueOf(chatAdapter.getCount()));
//                        sendMsgCount++;
                    }
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(ChatActivity.this, "Oops! You need to be at " +
                            restaurantName +
                            " to post a message or picture in QuickChat.", Toast.LENGTH_LONG).show();
                }
            }
        });

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gpsTracker = new GPSTracker(ChatActivity.this);
                if (gpsTracker.canGetLocation()) {
                    currentLatitude = gpsTracker.getLatitude();
                    currentLongitude = gpsTracker.getLongitude();
                }

                final Location currentLocation = new Location("current location");
                currentLocation.setLatitude(currentLatitude);
                currentLocation.setLongitude(currentLongitude);
                final Location restLocation = new Location("rest location");
                restLocation.setLatitude(restLatitude);
                restLocation.setLongitude(restLongitude);

                distance = currentLocation.distanceTo(restLocation);

                if (distance < 320) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File photo = null;
                    try {
//                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
//                        photo = new File(imageFilePath);
                        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "QuickTable");
                        boolean success = true;
                        if (!directory.exists()) {
                            success = directory.mkdir();
                        }
                        Log.i("QuickChat ", "File inserting!" + success);
                        if (success) {
                            photo = new File(directory.toString() + File.separator + System.currentTimeMillis() + ".jpg");
                            Log.i("QuickChat ", "File inserted! " + photo.toString());
                        } else {
                            photo = new File(directory.toString() + File.separator + System.currentTimeMillis() + ".jpg");
                            Log.i("QuickChat ", "File inserted!");
                        }

                    } catch (Exception e) {

                    }
                    imageUri = Uri.fromFile(photo);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Oops! You need to be at " +
                            restaurantName +
                            " to post a message or picture in QuickChat.", Toast.LENGTH_LONG).show();
                }
            }
        });

//        drawerList.setAdapter(new DrawerListAdapter(this));

//         FOR USER PROFILE OPTIONS
//        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 1:
//                        // User Profile
//                        startActivity(new Intent(ChatActivity.this, UserProfileActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 2:
//                        // User Favorites Orders List
//                        startActivity(new Intent(ChatActivity.this, OrderHistoryActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 3:
//                        // Order Booking Status - Show user estimated wait time
//                        startActivity(new Intent(ChatActivity.this, OrderConfirmationActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 4:
//                        shareApp();
//                        break;
//                    case 5:
//                        startActivity(new Intent(ChatActivity.this, ChatImages.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 6:
//                        startActivity(new Intent(ChatActivity.this, AboutUsActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                }
//            }
//        });

        ConnectionDetector connectionDetector = new ConnectionDetector(this);
        if (!connectionDetector.isConnectedToInternet()) {
            connectionDetector.internetError();
        } else {
            if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isQtSupported()) {
                new XMPPAsyncTask().execute();
            } else {
                createChatGroup();
            }
        }

    }

    private File createTemporaryFile(String part, String ext) throws IOException {
        File tempFile = Environment.getExternalStorageDirectory();
        tempFile = new File(tempFile.getAbsolutePath() + "/.temp/");
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return File.createTempFile(part, ext, tempFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (imageUri == null) {
                        return;
                    }
                    this.getContentResolver().notifyChange(imageUri, null);
                    ContentResolver cr = this.getContentResolver();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                    } catch (IOException e) {
                    }

                    ExifInterface exifInterface = null;
                    try {
                        exifInterface = new ExifInterface(imageUri.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    System.out.println("rotation = " + rotation);

                    SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
                    String deviceName = preferences.getString("device_name", null);

                    Camera.CameraInfo info = new Camera.CameraInfo();

                    if (deviceName.equals("samsung") || deviceName.equals("Sony") || deviceName.equalsIgnoreCase("LGE")) {

                        if (Build.VERSION.SDK_INT > 17) {
                            if (rotation == 6) {
                                // portrait == 6
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            }

                            if (rotation == 8) {
                                // portrait == 8 front camera
                                Matrix matrix = new Matrix();
                                matrix.postRotate(270);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            }
                        }
                    }

//                    imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));

                    int actualWidth = bitmap.getWidth();
                    int actualHeight = bitmap.getHeight();
                    int maxHeight = 1280;//1136.0;
                    int maxWidth = 960;//640.0;
                    float imgRatio = (float) actualWidth / actualHeight;
                    float maxRatio = (float) maxWidth / maxHeight;
                    if (actualHeight > maxHeight || actualWidth > maxWidth) {
                        if (imgRatio < maxRatio) {
                            //adjust width according to maxHeight
                            imgRatio = (float) maxHeight / actualHeight;
                            actualWidth = (int) (imgRatio * actualWidth);
                            actualHeight = maxHeight;
                        } else if (imgRatio > maxRatio) {
                            //adjust height according to maxWidth
                            imgRatio = (float) maxWidth / actualWidth;
                            actualHeight = (int) (imgRatio * actualHeight);
                            actualWidth = maxWidth;
                        } else {
                            actualHeight = maxHeight;
                            actualWidth = maxWidth;
                        }
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, actualWidth, actualHeight, true);

                    chatScreen.setVisibility(View.GONE);
                    imagePreviewScreen.setVisibility(View.VISIBLE);
                    imagePreview.setImageBitmap(bitmap);

                    final Bitmap finalBitmap = bitmap;
                    imageDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // adding message to message list
                            if (chatMessages.size() > 0){
                                msgIndex = chatMessages.get(chatMessages.size() - 1).getMsgIndex();
                            }else {
                                msgIndex = -1;
                            }
                            msgIndex++;
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                            date = dateFormat.format(new Date()).toString() + "@" + dateFormat1.format(new Date()).toString();
                            ChatMessage chatMessage = new ChatMessage();
                            String[] timeStamp = date.split("@");
                            chatMessage.setMsgid(timeStamp[1]);
                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                            String time = timeFormat.format(new Date()).toString();
                            chatMessage.setTime(time);
                            chatMessage.setBitmap(finalBitmap);
                            chatMessage.setImageUri(imageUri);
                            chatMessage.setMine(true);
                            chatMessage.setUserName(profileName);
                            chatMessage.setCaption(imageCaption.getText().toString());
                            chatMessage.setMsgIndex(msgIndex);

                            OfflineChatImagesModel offlineChatImagesModel = new OfflineChatImagesModel();
                            offlineChatImagesModel.setPlaceId(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
                            offlineChatImagesModel.setImagePath(imageUri.getPath());
                            offlineChatImagesModel.setMsgid(timeStamp[1]);
                            offlineChatImagesModel.setTime(time);
                            offlineChatImagesModel.setMine(true);
                            offlineChatImagesModel.setUserName(profileName);
                            offlineChatImagesModel.setCaption(imageCaption.getText().toString());
                            offlineChatImagesModel.setChatTime(date);
                            offlineChatImagesModel.setMsgIndex(msgIndex);
                            offlineChatImagesModels.add(offlineChatImagesModel);

                            SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
                            String json = offlineImagesData.getString("OfflineImages", null);
                            Log.i("Offline Image", "" + json);
                            if (json != null && !json.equals("null")) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
                                }.getType();
                                ArrayList<OfflineChatImagesModel> totalOfflineChatImagesModels = gson.fromJson(json, type);
//                                for (int i=0; i<offlineChatImagesModels.size(); i++){
                                totalOfflineChatImagesModels.add(offlineChatImagesModel);
//                                }
                                SharedPreferences.Editor editor = offlineImagesData.edit();
                                Gson gson1 = new Gson();
                                String json1 = gson1.toJson(totalOfflineChatImagesModels);
                                editor.putString("OfflineImages", json1);
                                editor.apply();
                            } else {
                                SharedPreferences.Editor editor = offlineImagesData.edit();
                                Gson gson = new Gson();
                                String json1 = gson.toJson(offlineChatImagesModels);
                                editor.putString("OfflineImages", json1);
                                editor.apply();
                            }


                            if (chatMessages.size() > 0) {
                                ChatMessage compareMessage = chatMessages.get(chatMessages.size() - 1);
                                if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                    if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                        chatMessage.setFirstMsg(false);
                                    } else {
                                        chatMessage.setFirstMsg(true);
                                    }
                                }
                                chatMessages.add(chatMessage);
                            } else {
                                chatMessage.setFirstMsg(true);
                                chatMessages.add(chatMessage);
                            }

                            isMeSending = true;
                            chatAdapter.notifyDataSetChanged();
                            listView.setSelection(chatAdapter.getCount() - 1);

//                            SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
//                            LocationListModel locationListModel = menuSingleton.getClickedRestaurant();
//                            locationListModel.setRestaurantChatAvailable(String.valueOf(chatAdapter.getCount()));

                            System.out.println("Bitmap byte count - " + finalBitmap.getByteCount());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//                            Bitmap tempBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
//                            System.out.println("compressed = " + compressed + "Bitmap byte count - " + tempBitmap.getByteCount());
                            byte[] bytes = baos.toByteArray();

                            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                            postImage(encoded, imageCaption.getText().toString(), imageUri.getPath(), date, chatMessage.getMsgid());
                            imageCaption.setText("");
                            chatScreen.setVisibility(View.VISIBLE);
                            imagePreviewScreen.setVisibility(View.GONE);
//                            sendMsgCount++;
                        }
                    });

                    imageClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageCaption.setText("");
                            chatScreen.setVisibility(View.VISIBLE);
                            imagePreviewScreen.setVisibility(View.GONE);
                        }
                    });

//                    showImagePreviewScreen(bitmap);

//                    Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, 720, 1080);

//                    Bitmap bmp = decodeSampledBitmapFromUri(String.valueOf(imageUri), 720, 1080);
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inDensity = 1;
//                    options.inTargetDensity = 1;
//                    options.inJustDecodeBounds = true;
//                    Bitmap bmp = BitmapFactory.decodeFile(String.valueOf(imageUri));

                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gpsTracker == null) {
            gpsTracker = new GPSTracker(this);
        }
        gpsTracker.startLocation();
        SharedPreferences sharedPreference = getSharedPreferences("user_info", MODE_PRIVATE);
        profileName = sharedPreference.getString("user_name", null);
        userAddress = sharedPreference.getString("user_address", null);
        userPhone = sharedPreference.getString("user_phone", null);
        String userImageUrl = sharedPreference.getString("user_image", null);
//        userNameTxt.setText(profileName);
//        userLocation.setText(userAddress);
//        userNameTxt.setTypeface(Globals.robotoBold);
//        userLocation.setTypeface(Globals.robotoRegular);
//        Glide.with(this).load(Config.QUICK_CHAT_IMAGE + userImageUrl)
//                .asBitmap().placeholder(R.mipmap.default_profile_pic)
//                .into(userImage);
//        Glide.with(this).load("https://s3-us-west-2.amazonaws.com/stagingquicktable/profile/" + userImageUrl)
//                .asBitmap().placeholder(R.mipmap.default_profile_pic)
//                .into(userImage);

//        userImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

//        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();
            accuracy = gpsTracker.getAccuracy();
        }

        final Location currentLocation = new Location("current location");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);
        final Location restLocation = new Location("rest location");
        restLocation.setLatitude(restLatitude);
        restLocation.setLongitude(restLongitude);
        distance = currentLocation.distanceTo(restLocation);

//        if (distance > 100) {
//            writeMsg.setFocusable(false);
//        } else {
//            writeMsg.setFocusable(true);
//            System.out.println("(On Resume) distance = " + distance);
//        }
//
//        writeMsg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                distance = currentLocation.distanceTo(restLocation);
//
////                if (currentLocation.hasAccuracy()){
////                    Toast.makeText(ChatActivity.this,""+currentLocation.getAccuracy(),Toast.LENGTH_SHORT);
////                }
//
//                Log.i("Accuracy", "" + accuracy);
//                if (distance > 100) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    Toast.makeText(ChatActivity.this, "Oops! You need to be at " +
//                            restaurantName +
//                            " to enable Quickchat.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

//        ConnectionDetector connectionDetector = new ConnectionDetector(this);
//        if (!connectionDetector.isConnectedToInternet()){
//            connectionDetector.internetError();
//        }else {
//            if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isQtSupported()) {
//                new XMPPAsyncTask().execute();
////            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
//            } else {
//                createChatGroup();
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gpsTracker != null) {
            gpsTracker.stopLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return true;
    }

    private class ChatScreen extends WebViewClient {
        ProgressDialog progressDialog;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            loadingImage.setVisibility(View.VISIBLE);
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loadingImage.setVisibility(View.GONE);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public ChatService getService() {
        return chatService;
    }

    public class XMPPAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chatLayout.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
//                XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
//                configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//                configBuilder.setHost("159.203.88.161");
//                configBuilder.setPort(5222);
//                configBuilder.setServiceName("159.203.88.161");
//
//                connection = new XMPPTCPConnection(configBuilder.build());
//                connection.setPacketReplyTimeout(30000);
//                connection.connect();
//
//                Log.i("Login", "Try to login as " + userPhone);
//                Log.i("factualId", "FactualId " + SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
//                connection.login(userPhone, userPhone);
////                connection.login("917569550492", "917569550492");
//                Log.i("XMPPClient", "Logged in as " + connection.getUser());

                SharedPreferences sharedPreference = getSharedPreferences("user_info", MODE_PRIVATE);
                final String userPhone = sharedPreference.getString("user_phone", null);

                SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
                final Gson gson = new Gson();
                final String json = offlineImagesData.getString("OfflineImages", null);
                Log.i("Offline Images", "" + json);

                if (SpecificMenuSingleton.getInstance().getConnection() != null) {
                    connection = SpecificMenuSingleton.getInstance().getConnection();
                } else {
                    new XMPPConnectTask().execute();
                    return null;
                }
//                System.out.println("connection = " + connection.toString());

                Presence presence = new Presence(Presence.Type.available);
                connection.sendPacket(presence);
                manager = MultiUserChatManager.getInstanceFor(connection);
//                manager = SpecificMenuSingleton.getInstance().getManager();
                muc = manager.getMultiUserChat(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID() +
                        "@conference." + com.app.mobi.quicktabledemo.utils.Config.QUICK_CHAT_IP_URL);
//                muc = manager.getMultiUserChat("0123456789@conference.159.203.88.161");
                Log.i("CHAT JOINING", SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID() +
                        "@conference." + com.app.mobi.quicktabledemo.utils.Config.QUICK_CHAT_IP_URL);
                muc.join(userPhone);
                Log.i("Chat joined", "chat joined");
                isXmppException = false;

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (json != null && !json.equals("null")) {
//                            Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
//                            }.getType();
//                            ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);
//                            for (int i=0; i<offlineChatImagesModels.size(); i++){
//                                if (offlineChatImagesModels.get(i).getPlaceId().equals(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID())){
//                                    if (offlineChatImagesModels.get(i).getMsgIndex() == 0) {
//                                        getOfflineMessages();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                });

                Message message = null;
                final ArrayList<ChatMessage> tempChatMessages = new ArrayList<>();
                while ((message = muc.nextMessage(1000)) != null) {

                    if (message == null)
                        break;
                    Log.i("Poll Messages", message.toString());

                    final ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setBody(message.getBody());

                    String subject = message.getSubject();
                    if (subject.contains("@")) {
                        msgIndex = Integer.valueOf(subject.split("@")[1]);
                        Log.i("MsgIndex", "Index - " + msgIndex);
                        chatMessage.setUserName(subject.split("@")[0]);
                    } else {
                        chatMessage.setUserName(message.getSubject());
                    }
                    chatMessage.setMsgIndex(msgIndex);

                    // Getting time
                    if (message.getThread() != null) {
                        String timeString = message.getThread();
                        String[] times = timeString.split("@");
                        String[] time = times[0].split(" ");
                        String[] t = time[1].split("\\:");
                        StringBuilder displayTime = new StringBuilder();
                        displayTime.append(t[0]);
                        displayTime.append(":");
                        displayTime.append(t[1]);
                        displayTime.append(" " + time[2]);
                        chatMessage.setTime(displayTime.toString());
                        chatMessage.setMsgid(times[1]);

                        if (message.getFrom().contains(userPhone)) {
                            chatMessage.setMine(true);
                        } else {
                            chatMessage.setMine(false);
                        }

                        if (tempChatMessages.size() > 0) {
//                                        if (count == chatMessages.size()) {
                            ChatMessage compareMessage = tempChatMessages.get(tempChatMessages.size() - 1);

                            if (chatMessage.getMsgid() != null && compareMessage.getMsgid() != null){
                                if (!compareMessage.getMsgid().equals(chatMessage.getMsgid())) {
                                    if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                        if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                            chatMessage.setFirstMsg(false);
                                        } else {
                                            chatMessage.setFirstMsg(true);
                                        }
                                    }
                                    tempChatMessages.add(chatMessage);
                                }
                            }

//                        if (tempChatMessages.size() > 1) {
//                            if (compareMessage.getMsgIndex() - tempChatMessages.get(tempChatMessages.size()-2).getMsgIndex() >= 1) {
//                                getOfflineMessages();
//                            }
//                        }
//                                        }
                        } else {
                            chatMessage.setFirstMsg(true);
                            tempChatMessages.add(chatMessage);
                        }

                    }

                }

                finalTempChatMessages = tempChatMessages;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (json != null) {
                            getOfflineMessages();
//                            Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
//                            }.getType();
//                            ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);
//                            if (offlineChatImagesModels != null && offlineChatImagesModels.size() > 0) {
//                                for (int i = 0; i < offlineChatImagesModels.size(); i++) {
//                                    if (offlineChatImagesModels.get(i).getPlaceId().equalsIgnoreCase(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID())) {
//
//                                        if (finalTempChatMessages.size() > 0) {
//                                            if (offlineChatImagesModels.get(i).getMsgIndex() - finalTempChatMessages.get(finalTempChatMessages.size()-1).getMsgIndex() >= 1) {
//                                                Log.i("Offline Images", "Getting data");
//                                                getOfflineMessages();
////                                                if (finalTempChatMessages.get(0).getMsgIndex() > 0){
////                                                    if (finalTempChatMessages.get(0).getMsgIndex() + finalTempChatMessages.size() >= offlineChatImagesModels.get(i).getMsgIndex())
////                                                        getOfflineMessages();
////                                                }else {
////                                                    if (finalTempChatMessages.size() >= offlineChatImagesModels.get(i).getMsgIndex())
////                                                        getOfflineMessages();
////                                                }
//                                            }
//                                        }
//
//                                    }
//                                }
//                            }
                        }
                        Log.i("Chat size", "" + finalTempChatMessages.size());
                        ChatMessage[] tempChatMsg = new ChatMessage[finalTempChatMessages.size()];
                        if (finalTempChatMessages.size()>0){
                            ChatMessage tempChatMessage = new ChatMessage();
                            for (int i = 0; i < finalTempChatMessages.size()-1; i++){
                                for (int j = 0; j < finalTempChatMessages.size()-i-1; j++) {
                                    if (finalTempChatMessages.get(j).getMsgid().compareTo(finalTempChatMessages.get(j+1).getMsgid()) > 0) {
                                        tempChatMessage = finalTempChatMessages.get(j);
                                        finalTempChatMessages.set(j, finalTempChatMessages.get(j+1));
                                        finalTempChatMessages.set(j+1, tempChatMessage);
                                    }
                                }
//                                if (i > 0) {
//                                    Log.i("Indexes - ", "" + finalTempChatMessages.get(i).getMsgIndex() + " " + finalTempChatMessages.get(i-1).getMsgIndex() +
//                                                    "Missing Index count - " + totalMissingIndex);
//                                    if ((finalTempChatMessages.get(i).getMsgIndex() - finalTempChatMessages.get(i-1).getMsgIndex()) == 1){
//                                        tempChatMsg[finalTempChatMessages.get(i).getMsgIndex() - totalMissingIndex] = finalTempChatMessages.get(i);
//                                    }else {
//                                        totalMissingIndex += (finalTempChatMessages.get(i).getMsgIndex() - finalTempChatMessages.get(i-1).getMsgIndex());
//                                        tempChatMsg[tempChatMsg.length-1] = finalTempChatMessages.get(i);
//                                    }
//                                }else {
//                                    tempChatMsg[0] = finalTempChatMessages.get(i);
//                                }
                            }
//                            chatMessages.addAll(Arrays.asList(tempChatMsg));
//                            chatMessages.removeAll(Collections.singleton(null));
                            chatMessages.addAll(finalTempChatMessages);
                            chatAdapter = new ChatAdapter(ChatActivity.this, chatMessages);
                            listView.setAdapter(chatAdapter);
                        }
                    }
                });

                muc.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Message message) {
                        Log.i("IncomingMessage", message.toString());
//                        String from = message.getFrom();
//                        Log.i("XMPPClient", "Got text [" + message.getBody() + "] from [" + from + "]");

//                        if (message.getThread() != null){
                        final ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setBody(message.getBody());

                        String subject = message.getSubject();
                        if (subject.contains("@")) {
                            msgIndex = Integer.valueOf(subject.split("@")[1]);
                            Log.i("MsgIndex", "Index - " + msgIndex);
                            chatMessage.setUserName(subject.split("@")[0]);
                        } else {
                            chatMessage.setUserName(message.getSubject());
                        }
                        chatMessage.setMsgIndex(msgIndex);

                        // Getting time
                        if (message.getThread() != null) {
                            String timeString = message.getThread();
                            String[] times = timeString.split("@");
                            String[] time = times[0].split(" ");
                            String[] t = time[1].split("\\:");
                            StringBuilder displayTime = new StringBuilder();
                            displayTime.append(t[0]);
                            displayTime.append(":");
                            displayTime.append(t[1]);
                            displayTime.append(" " + time[2]);
                            chatMessage.setTime(displayTime.toString());
                            chatMessage.setMsgid(times[1]);
                        }

                        if (message.getFrom().contains(userPhone)) {
                            chatMessage.setMine(true);
                        } else {
                            chatMessage.setMine(false);
                        }

//                        if (chatMessages.size() > 1) {
//                            if ((msgIndex - chatMessages.get(chatMessages.size() - 1).getMsgIndex()) == 1) {
//
//                            } else {
//                                getOfflineMessages();
//                            }
//                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (!isMeSending) {

                                    if (chatMessages.size() > 0) {
                                        int count = 0;
                                        for (int i = 0; i < chatMessages.size(); i++) {
                                            ChatMessage compareMessage = chatMessages.get(i);
                                            count++;
                                            if (compareMessage.getMsgid() != null){
                                                if (chatMessage.getMsgid().equals(compareMessage.getMsgid())) {
                                                    removeOfflineMsgData(compareMessage);
//                                                chatMessages.remove(i);
                                                    break;
                                                }
                                            }
                                        }
                                        if (count == chatMessages.size()) {
                                            ChatMessage compareMessage = chatMessages.get(chatMessages.size() - 1);

                                            if (compareMessage.getMsgid() != null && chatMessage.getMsgid() != null) {
                                                if (!compareMessage.getMsgid().equals(chatMessage.getMsgid())) {
                                                    if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                                        if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                                            chatMessage.setFirstMsg(false);
                                                        } else {
                                                            chatMessage.setFirstMsg(true);
                                                        }
                                                    }
                                                    chatMessages.add(chatMessage);
                                                }
                                            }
                                        }

                                    } else {
//                                        msgIndex = 0;
                                        chatMessage.setFirstMsg(true);
                                        chatMessages.add(chatMessage);
                                    }

                                } else {
                                    if (chatMessages.size() > 0) {
                                        int count = 0;
                                        for (int i = 0; i < chatMessages.size(); i++) {
                                            ChatMessage compareMessage = chatMessages.get(i);
//                                            count++;
                                            if (compareMessage.getMsgid() != null){
                                                if (chatMessage.getMsgid().equals(compareMessage.getMsgid())) {
                                                    removeOfflineMsgData(compareMessage);
//                                                chatMessages.remove(i);
                                                    break;
                                                }
                                            }
                                        }
//                                        if (count == chatMessages.size()) {
                                        ChatMessage compareMessage = chatMessages.get(chatMessages.size() - 1);

                                        if (compareMessage.getMsgid() != null && chatMessage.getMsgid() != null){
                                            if (!compareMessage.getMsgid().equals(chatMessage.getMsgid())) {
                                                if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                                    if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                                        chatMessage.setFirstMsg(false);
                                                    } else {
                                                        chatMessage.setFirstMsg(true);
                                                    }
                                                }
                                                chatMessages.add(chatMessage);
                                            }
                                        }
//                                        }
                                    } else {
//                                        msgIndex = 0;
                                        chatMessage.setFirstMsg(true);
                                        chatMessages.add(chatMessage);
                                    }
                                    isMeSending = false;
                                }

                                chatAdapter.notifyDataSetChanged();
                                listView.setSelectionFromTop(chatAdapter.getCount() - 1, 0);

//                                if (json != null) {
//                                    Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
//                                    }.getType();
//                                    ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);
//                                    if (offlineChatImagesModels != null && offlineChatImagesModels.size() > 0) {
//                                        for (int i = 0; i < offlineChatImagesModels.size(); i++) {
//                                            if (offlineChatImagesModels.get(i).getPlaceId().equalsIgnoreCase(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID())) {
//
//                                                if (chatMessages.size() > 0) {
//                                                    if (offlineChatImagesModels.get(i).getMsgIndex() - msgIndex >= 1) {
//                                                        Log.i("Offline Images", "Getting data");
//                                                        if (chatMessages.get(0).getMsgIndex() > 0){
//                                                            if (chatMessages.get(0).getMsgIndex() + chatMessages.size() >= offlineChatImagesModels.get(i).getMsgIndex())
//                                                                getOfflineMessages();
//                                                        }else {
//                                                            if (chatMessages.size() >= offlineChatImagesModels.get(i).getMsgIndex())
//                                                                getOfflineMessages();
//                                                        }
//                                                    }
//                                                }
//
//                                            }
//                                        }
//                                    }
//                                }

                                SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
                                LocationListModel locationListModel = menuSingleton.getClickedRestaurant();

                                System.out.println("LOG: chat count: " + sendMsgCount);

//                                if (Integer.parseInt(locationListModel.getRestaurantChatAvailable()) > chatAdapter.getCount()){
//                                    locationListModel.setRestaurantChatAvailable(locationListModel.getRestaurantChatAvailable());
//                                }else {
                                locationListModel.setRestaurantChatAvailable(String.valueOf(chatAdapter.getCount()));
//                                }

                                SharedPreferences preferences = getSharedPreferences("chat_count_details", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("factual_id", locationListModel.getPlaceID());
                                editor.putString("chat_count", locationListModel.getRestaurantChatAvailable());
                                editor.apply();
                            }
                        });

                    }
                });

            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                new XMPPConnectTask().execute();
                isXmppException = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, "Oops! you have a connection issue, reconnecting ...", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (SmackException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (connection != null) {
                System.out.println("LOG: Displaying chats...");
//                chatAdapter = new ChatAdapter(ChatActivity.this, chatMessages);
//                listView.setAdapter(chatAdapter);
//                listView.setSelection(chatAdapter.getCount());
                chatLayout.setVisibility(View.VISIBLE);
                loadingImage.setVisibility(View.GONE);
                chatAdapter.notifyDataSetChanged();
                listView.setSelection(chatAdapter.getCount() - 1);

                if (!isXmppException) {
                    // Getting offline images Data
//                    new Handler().post(new Runnable() {
//                        @Override
//                        public void run() {
//                            SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
//                            Gson gson = new Gson();
//                            String json = offlineImagesData.getString("OfflineImages", null);
//                            if (json != null) {
//                                Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
//                                }.getType();
//                                ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);
//                                if (offlineChatImagesModels != null && offlineChatImagesModels.size() > 0) {
//                                    Log.i("OfflineImages", json);
//                                    for (int i = 0; i < offlineChatImagesModels.size(); i++) {
//                                        if (offlineChatImagesModels.get(i).getPlaceId().equals(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID())) {
//                                            Toast.makeText(ChatActivity.this, "Uploading Your Offline Images!", Toast.LENGTH_LONG).show();
//                                            ChatMessage chatMessage = new ChatMessage();
//                                            chatMessage.setMsgid(offlineChatImagesModels.get(i).getMsgid());
//                                            chatMessage.setTime(offlineChatImagesModels.get(i).getTime());
//                                            chatMessage.setMine(true);
//                                            chatMessage.setUserName(offlineChatImagesModels.get(i).getUserName());
//                                            chatMessage.setCaption(offlineChatImagesModels.get(i).getCaption());
//
//                                            ContentResolver cr = getContentResolver();
//                                            Uri imageUri = Uri.fromFile(new File(offlineChatImagesModels.get(i).getImagePath()));
//                                            Bitmap bitmap = null;
//                                            try {
//                                                bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
//                                            } catch (IOException e) {
//                                            }
//
//                                            ExifInterface exifInterface = null;
//                                            try {
//                                                exifInterface = new ExifInterface(imageUri.getPath());
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//
//                                            int rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//                                            System.out.println("rotation = " + rotation);
//
//                                            SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
//                                            String deviceName = preferences.getString("device_name", null);
//
//                                            if (deviceName.equals("samsung") || deviceName.equals("Sony") || deviceName.equalsIgnoreCase("LGE")) {
//
//                                                if (Build.VERSION.SDK_INT > 17) {
//                                                    if (rotation == 6) {
//                                                        // portrait == 6
//                                                        Matrix matrix = new Matrix();
//                                                        matrix.postRotate(90);
//                                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                                                    }
//
//                                                    if (rotation == 8) {
//                                                        // portrait == 8 front camera
//                                                        Matrix matrix = new Matrix();
//                                                        matrix.postRotate(270);
//                                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                                                    }
//                                                }
//                                            }
//
//                                            chatMessage.setImageUri(imageUri);
//                                            chatMessage.setBitmap(bitmap);
//
//                                            if (chatMessages.size() > 0) {
//                                                ChatMessage compareMessage = chatMessages.get(chatMessages.size() - 1);
//                                                if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
//                                                    if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
//                                                        chatMessage.setFirstMsg(false);
//                                                    } else {
//                                                        chatMessage.setFirstMsg(true);
//                                                    }
//                                                }
//                                                chatMessages.add(offlineChatImagesModels.get(i).getMsgIndex(), chatMessage);
//                                            } else {
//                                                chatMessage.setFirstMsg(true);
//                                                chatMessages.add(chatMessage);
//                                            }
//
//                                            chatAdapter.notifyDataSetChanged();
//                                            listView.setSelectionFromTop(chatAdapter.getCount() - 1, 0);
//                                            Log.i("Offline", "Update UI");
//
//                                            // upload offline images
//                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//                                            byte[] bytes = baos.toByteArray();
//                                            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
//                                            postImage(encoded, offlineChatImagesModels.get(i).getCaption(), imageUri.getPath(),
//                                                    offlineChatImagesModels.get(i).getChatTime(), offlineChatImagesModels.get(i).getMsgid());
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    });
                }
            } else {
                Toast.makeText(ChatActivity.this, "Failed to login!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void postImage(final String image, final String caption, final String imageUri, final String date, String msgId) {

        SharedPreferences sharedPreference = getSharedPreferences("user_info", MODE_PRIVATE);
        Config.SESSION_TOKEN_ID = sharedPreference.getString("session_token_id", null);

        final String url = Config.QT_IMAGE_UPLOAD;

        JSONObject object = new JSONObject();
        try {
            object.put("image", image);
            object.put("chat", true);
            object.put("city", SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantCity());
            object.put("restaurant_name", SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantName());
            object.put("address_line", SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantAddress());
            object.put("phone_number", userPhone);
            object.put("state", SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantState());
            object.put("url", msgId + "." + userPhone);
        } catch (JSONException e) {

        }

//        byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        int imageHeight = bitmap.getHeight();
//        int imageWidth = bitmap.getWidth();
//        Log.i("ImageSize", "height - " + imageHeight + "width - " + imageWidth);

//        Log.i("imageEncode", object.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.i("ImageUpload", response.toString());
                        if (ChatActivity.this != null) {
                            try {

                                SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
                                Gson gson = new Gson();
                                String json = offlineImagesData.getString("OfflineImages", null);
                                Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
                                }.getType();
                                ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);

                                for (int i = 0; i < offlineChatImagesModels.size(); i++) {
                                    if (offlineChatImagesModels.get(i).getImagePath() != null) {
                                        if (offlineChatImagesModels.get(i).getImagePath().equalsIgnoreCase(imageUri)) {
//                                    String path = offlineChatImagesModels.get(i).getImageUri().substring(7);

                                            String imageUrl = response.getString("url");
                                            Message message = new Message();
//                            String fullImageUrl = Config.QT_IMAGE + imageUrl;
                                            String fullImageUrl = Config.QUICK_CHAT_IMAGES + imageUrl;
                                            message.setBody(fullImageUrl + "@" + caption);
                                            Log.i("Image msg", fullImageUrl + "@" + caption);
//                                SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
                                            String subject = profileName + "@" + String.valueOf(offlineChatImagesModels.get(i).getMsgIndex());
                                            message.setSubject(subject);

                                            try {
                                                message.setThread(date);
                                                muc.sendMessage(message);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            File file = new File(offlineChatImagesModels.get(i).getImagePath());
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                            offlineChatImagesModels.remove(offlineChatImagesModels.get(i));

                                            SharedPreferences.Editor editor = offlineImagesData.edit();
                                            Gson gson1 = new Gson();
                                            String json1 = gson1.toJson(offlineChatImagesModels);
                                            editor.putString("OfflineImages", json1);
                                            editor.apply();
                                        }
                                    }
                                }

                            } catch (JSONException e) {

                            }
                        }

//                        Toast.makeText(ChatActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.i("Error", error.toString());
//                int responseCode = error.networkResponse.statusCode;
//                if(responseCode == 500){
//                Toast.makeText(ChatActivity.this, "Image uploaded failed!", Toast.LENGTH_SHORT).show();
//                postImage(image, caption);
//                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("QTAuthorization", Config.SESSION_TOKEN_ID);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void createChatGroup() {

        chatLayout.setVisibility(View.GONE);
        loadingImage.setVisibility(View.VISIBLE);

        String url = Config.QT_CREATE_CHAT_ROOM;

        JSONObject object = new JSONObject();
        try {
            object.put("placeId", SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
        } catch (JSONException e) {

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        new XMPPAsyncTask().execute();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    if (error instanceof TimeoutError) {
                        com.app.mobi.quicktabledemo.utils.Config.internetSlowError(ChatActivity.this);
                    } else if (error instanceof NoConnectionError) {
                        com.app.mobi.quicktabledemo.utils.Config.internetSlowError(ChatActivity.this);
                    } else if (error instanceof ServerError) {
                        com.app.mobi.quicktabledemo.utils.Config.serverError(ChatActivity.this);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("QTAuthorization", Config.SESSION_TOKEN_ID);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIMEOUT,  // 25 sec - timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }


    private Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        Bitmap bm = null;
// First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

// Calculate inSampleSize
        options.inScaled = false;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

// Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return rotateCapturedImage(bm, imageUri, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
// Height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private Bitmap rotateCapturedImage(Bitmap image, Uri imageURI,
                                       BitmapFactory.Options options) {
        Bitmap rotatedImage = null;
        try {
            ExifInterface exifInterface = new ExifInterface(imageURI.getPath());
            int orientation = exifInterface.
                    getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
            Log.d("Orientation", "\t" + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedImage = rotateImage(image, 90, options);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedImage = rotateImage(image, 180, options);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedImage = rotateImage(image, 270, options);
                    break;
                default:
                    rotatedImage = rotateImage(image, 90, options);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rotatedImage;
    }

    private Bitmap rotateImage(Bitmap img, int degree, BitmapFactory.Options options) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degree, (float) img.getWidth() / 2, (float) img.getHeight() / 2);
        Bitmap rotatedImg = Bitmap.createBitmap
                (img, 0, 0, options.outWidth, options.outHeight, matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    public void onBackPressed() {
//        handler.removeCallbacks(runnable);
        removeChatRoom();
        super.onBackPressed();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < chatMessages.size(); i++) {
            if (chatMessages.get(i).getImageUri() != null) {
//                getContentResolver().delete(chatMessages.get(i).getImageUri(), null, null);
            }
        }
        chatMessages.clear();
        finalTempChatMessages.clear();
        super.onDestroy();
    }

    private void showImagePreviewScreen(Bitmap bitmap) {

        chatScreen.setVisibility(View.GONE);
        imagePreviewScreen.setVisibility(View.VISIBLE);
        imagePreview.setImageBitmap(bitmap);

    }

    private void removeChatRoom() {
        String url = Config.QT_REMOVE_CHAT_ROOM;

        JSONObject object = new JSONObject();
        try {
            object.put("username", userPhone);
            object.put("placeId", SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("ChatRemoved", object.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("ChatRoomRemoved", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    if (error instanceof TimeoutError) {
                        com.app.mobi.quicktabledemo.utils.Config.internetSlowError(ChatActivity.this);
                    } else if (error instanceof NoConnectionError) {
                        com.app.mobi.quicktabledemo.utils.Config.internetSlowError(ChatActivity.this);
                    } else if (error instanceof ServerError) {
                        com.app.mobi.quicktabledemo.utils.Config.serverError(ChatActivity.this);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("QTAuthorization", Config.SESSION_TOKEN_ID);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIMEOUT,  // 25 sec - timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateAccuracy() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationRequest request = LocationRequest.create();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                GPSTracker gpsTracker = new GPSTracker(ChatActivity.this);
                if (gpsTracker.canGetLocation()) {
                    if (gpsTracker.getFinalLocation() != null) {
                        currentLatitude = gpsTracker.getFinalLocation().getLatitude();
                        currentLongitude = gpsTracker.getFinalLocation().getLongitude();
                        accuracy = gpsTracker.getFinalLocation().getAccuracy();
                        int accuracyInFeet = (int) ((int) accuracy * 3.2808);
                        accuracyText.setText("Accuracy - " + accuracyInFeet + "ft" + "  " + gpsTracker.getFinalLocation().getProvider());
                    }
                }
                updateAccuracy();
            }
        }, 1000);
    }

    public class XMPPConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chatLayout.setVisibility(View.GONE);
            loadingImage.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            SharedPreferences sharedPreference = getSharedPreferences("user_info", MODE_PRIVATE);
            String userPhone = sharedPreference.getString("user_phone", null);

            try {
                XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
                configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                configBuilder.setHost(Config.QUICK_CHAT_URL);
                configBuilder.setPort(5222);
                configBuilder.setServiceName(Config.QUICK_CHAT_URL);

                connection = new XMPPTCPConnection(configBuilder.build());
                connection.setPacketReplyTimeout(60000);
                connection.connect();

                Log.i("Login", "Try to login as " + userPhone);
//                Log.i("factualId", "FactualId " + SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
                connection.login(userPhone, userPhone);
//                connection.login("917569550492", "917569550492");
                Log.i("XMPPClient", "Logged in as " + connection.getUser());
                Presence presence = new Presence(Presence.Type.available);
                connection.sendPacket(presence);
//                manager = MultiUserChatManager.getInstanceFor(connection);

                SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
//                menuSingleton.setManager(manager);
                menuSingleton.setConnection(connection);

            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new XMPPAsyncTask().execute();
        }
    }

    private void shareApp() {
        final SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", null);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
//        share.putExtra(Intent.EXTRA_SUBJECT, "QuickTable sharing!");
        share.putExtra(Intent.EXTRA_TEXT, "Hey ! " + userName.toUpperCase() + " has personally invited you to download the new QuickTable app…it's like all your favorite restaurant apps rolled into one, and you will love the QuickChat feature!  Just click the link below or go to the app store and search for QuickTable (all one word).\n" +
                "\n" +
                "Have a great day!\n\n" + "https://play.google.com/store/apps/details?id=com.app.mobi.quicktabledemo&hl=en");

        startActivity(Intent.createChooser(share, "Share link!"));
    }

    private void getOfflineMessages() {
        SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = offlineImagesData.getString("OfflineImages", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
            }.getType();
            ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);
            if (offlineChatImagesModels != null && offlineChatImagesModels.size() > 0) {
                Log.i("OfflineImages", json.toString());
                for (int i = 0; i < offlineChatImagesModels.size(); i++) {
                    if (offlineChatImagesModels.get(i).getPlaceId().equalsIgnoreCase(SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID())) {
                        Toast.makeText(ChatActivity.this, "Uploading Your Offline Data!", Toast.LENGTH_LONG).show();
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMsgid(offlineChatImagesModels.get(i).getMsgid());
                        chatMessage.setTime(offlineChatImagesModels.get(i).getTime());
                        chatMessage.setMine(true);
                        chatMessage.setUserName(offlineChatImagesModels.get(i).getUserName());
                        chatMessage.setMsgIndex(offlineChatImagesModels.get(i).getMsgIndex());

                        if (offlineChatImagesModels.get(i).getChatMsg() != null) {

                            chatMessage.setBody(offlineChatImagesModels.get(i).getChatMsg());

                            getOfflineChatMsg(offlineChatImagesModels.get(i));

                        } else {

                            chatMessage.setCaption(offlineChatImagesModels.get(i).getCaption());

                            ContentResolver cr = getContentResolver();
                            Uri imageUri = Uri.fromFile(new File(offlineChatImagesModels.get(i).getImagePath()));
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                            } catch (IOException e) {
                            }

                            ExifInterface exifInterface = null;
                            try {
                                exifInterface = new ExifInterface(imageUri.getPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            int rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                            System.out.println("rotation = " + rotation);

                            SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
                            String deviceName = preferences.getString("device_name", null);

                            if (deviceName.equals("samsung") || deviceName.equals("Sony") || deviceName.equalsIgnoreCase("LGE")) {

                                if (Build.VERSION.SDK_INT > 17) {
                                    if (rotation == 6) {
                                        // portrait == 6
                                        Matrix matrix = new Matrix();
                                        matrix.postRotate(90);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                    }

                                    if (rotation == 8) {
                                        // portrait == 8 front camera
                                        Matrix matrix = new Matrix();
                                        matrix.postRotate(270);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                    }
                                }
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

//                            bitmap.recycle();

                            chatMessage.setImageUri(imageUri);
                            chatMessage.setBitmap(bitmap);

                            byte[] bytes = baos.toByteArray();
                            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                            postImage(encoded, offlineChatImagesModels.get(i).getCaption(), imageUri.getPath(), offlineChatImagesModels.get(i).getChatTime(),
                                    offlineChatImagesModels.get(i).getMsgid());

                        }

                        if (finalTempChatMessages.size() > 0) {
                            ChatMessage compareMessage = finalTempChatMessages.get(finalTempChatMessages.size() - 1);
                            if (compareMessage.getUserName() != null && chatMessage.getUserName() != null) {
                                if (compareMessage.getUserName().equals(chatMessage.getUserName())) {
                                    chatMessage.setFirstMsg(false);
                                } else {
                                    chatMessage.setFirstMsg(true);
                                }
                            }
                            finalTempChatMessages.add(chatMessage);
//                            if (chatMessages.get(0).getMsgIndex() > 0){
//                                if (offlineChatImagesModels.get(i).getMsgIndex() - chatMessages.get(0).getMsgIndex() > 0)
//                                    chatMessages.add(offlineChatImagesModels.get(i).getMsgIndex() - chatMessages.get(0).getMsgIndex(), chatMessage);
//                            }else {
//                                chatMessages.add(offlineChatImagesModels.get(i).getMsgIndex(), chatMessage);
//                            }
                        } else {
                            chatMessage.setFirstMsg(true);
                            finalTempChatMessages.add(chatMessage);
                        }

//                        chatAdapter.notifyDataSetChanged();
//                        listView.setSelection(chatAdapter.getCount() - 1);
                    }
                }
            }
        }
    }

    private void getOfflineChatMsg(OfflineChatImagesModel chatMessage) {

        Message message = new Message();
        message.setBody(chatMessage.getChatMsg());
//        msgIndex++;
        Log.i("Chat Msg Index", "" + chatMessage.getMsgIndex());
        Log.i("Chat Thread", "" + chatMessage.getChatTime());
        String subject = profileName + "@" + chatMessage.getMsgIndex();
        message.setSubject(subject);
        message.setThread(chatMessage.getChatTime());
        try {
            muc.sendMessage(message);

            SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = offlineImagesData.getString("OfflineImages", null);
            Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
            }.getType();
            ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);

            for (int i = 0; i < offlineChatImagesModels.size(); i++) {
                if (offlineChatImagesModels.get(i).getMsgid().equalsIgnoreCase(chatMessage.getMsgid())) {
                    offlineChatImagesModels.remove(i);
                }
            }

            SharedPreferences.Editor editor = offlineImagesData.edit();
            Gson gson1 = new Gson();
            String json1 = gson1.toJson(offlineChatImagesModels);
            editor.putString("OfflineImages", json1);
            editor.apply();

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    private void removeOfflineMsgData(ChatMessage message) {
        SharedPreferences offlineImagesData = getSharedPreferences("OfflineImagesData", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = offlineImagesData.getString("OfflineImages", null);
        Type type = new TypeToken<ArrayList<OfflineChatImagesModel>>() {
        }.getType();
        ArrayList<OfflineChatImagesModel> offlineChatImagesModels = gson.fromJson(json, type);

        if (offlineChatImagesModels != null) {
            for (int i = 0; i < offlineChatImagesModels.size(); i++) {
                if (offlineChatImagesModels.get(i).getMsgid().equalsIgnoreCase(message.getMsgid())) {
                    offlineChatImagesModels.remove(i);
                }
            }
            SharedPreferences.Editor editor = offlineImagesData.edit();
            Gson gson1 = new Gson();
            String json1 = gson1.toJson(offlineChatImagesModels);
            editor.putString("OfflineImages", json1);
            editor.apply();
        }
    }
}