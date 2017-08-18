package com.app.mobi.quicktabledemo.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.app.mobi.quicktabledemo.adapters.DrawerListAdapter;
import com.app.mobi.quicktabledemo.adapters.RestaurantServicesAdapter;
import com.app.mobi.quicktabledemo.fragments.MenuItemFragment;
import com.app.mobi.quicktabledemo.geofence.GeofenceStore;
import com.app.mobi.quicktabledemo.modelClasses.LocationListModel;
import com.app.mobi.quicktabledemo.network.VolleySingleton;
import com.app.mobi.quicktabledemo.utils.CartSingleton;
import com.app.mobi.quicktabledemo.utils.Config;
import com.app.mobi.quicktabledemo.utils.Globals;
import com.app.mobi.quicktabledemo.utils.SpecificMenuSingleton;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.Geofence;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class RestaurantServices extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    TextView userName, userLocation, restChats;
    DrawerLayout drawerLayout;
    ListView drawerList;
    ImageView homeBtn, userImage, favoriteLocationImg;
    Button callUs, getDirection;
    RelativeLayout cartContent;
    private ArrayList<LocationListModel> locationListModels;
    private boolean isQTSupported = false;
    private String restName;
    private int locationId, tenantId, clickPosition;
    private SpecificMenuSingleton menuSingleton;
    private double sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;
    private LocationListModel currentRest;
    private String restType;
    private Geofence geofence;
    private GeofenceStore geofenceStore;
    private String restMenuUrl = null;
    private String currentTime = null, openTime = null, closeTime = null;
    private boolean isRestFav = false;

    //    public RestaurantServices(ArrayList<LocationListModel> locationListModels){
//        this.locationListModels = locationListModels;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        RestaurantServices.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        TextView restaurantName = (TextView) findViewById(R.id.rest_name);
        TextView restaurantAddress = (TextView) findViewById(R.id.rest_address);
        TextView restaurantDistance = (TextView) findViewById(R.id.rest_distance);
        restChats = (TextView) findViewById(R.id.chats);
        TextView restEWT = (TextView) findViewById(R.id.rest_ewt);
        getDirection = (Button) findViewById(R.id.get_direction);
//        drawerList = (ListView) findViewById(R.id.drawer_list);
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        callUs = (Button) findViewById(R.id.call_us);
//        userName = (TextView) findViewById(R.id.user_name);
        userLocation = (TextView) findViewById(R.id.user_location);
        cartContent = (RelativeLayout) findViewById(R.id.cart_content);
//        userImage = (ImageView) findViewById(R.id.user_image);
        ImageView ratingImage = (ImageView) findViewById(R.id.rating_image);
        favoriteLocationImg = (ImageView) findViewById(R.id.location_favorite);

        favoriteLocationImg.setOnClickListener(this);

        toolbar.setTitle(" ");
        toolbar.getBackground().setAlpha(0);
        setSupportActionBar(toolbar);

        cartContent.setVisibility(View.GONE);

//        drawerList.setAdapter(new DrawerListAdapter(this));
        homeBtn = (ImageView) findViewById(R.id.menu_button);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        restName = intent.getStringExtra("RestName");
        String restAddress = intent.getStringExtra("RestAddress");
        Double restDistance = intent.getDoubleExtra("RestDistance", 0.0);
        String restChat = intent.getStringExtra("RestChat");
        String restEwt = intent.getStringExtra("RestEWT");
        final String restPhone = intent.getStringExtra("RestPhone");
        int restPosition = intent.getIntExtra("RestPosition", 0);
        locationId = intent.getIntExtra("LocationId", 0);
        tenantId = intent.getIntExtra("TenantId", 0);
        sourceLatitude = intent.getDoubleExtra("CurrentLatitude", 0);
        sourceLongitude = intent.getDoubleExtra("CurrentLongitude", 0);
        destinationLatitude = intent.getDoubleExtra("RestaurantLatitude", 0);
        destinationLongitude = intent.getDoubleExtra("RestaurantLongitude", 0);
        String restImage = intent.getStringExtra("RestaurantImage");
        restMenuUrl = intent.getStringExtra("RestaurantMenuUrl");
        restaurantName.setText(restName);

        String distance;
        if (Config.APP_NAME.equalsIgnoreCase("QuickTable")) {
            distance = String.format("%.2fmi", restDistance / 1609.344);
        } else {
            distance = String.format("%.2fmi", restDistance);
        }
        restaurantDistance.setText(distance);

        menuSingleton = SpecificMenuSingleton.getInstance();
        menuSingleton.setLatitude(destinationLatitude);
        menuSingleton.setLongitude(destinationLongitude);
        menuSingleton.setImageUrl(restImage);

        StringBuilder builder = new StringBuilder();
        if (menuSingleton.getClickedRestaurant().getRestaurantAddress() != null) {
            builder.append(menuSingleton.getClickedRestaurant().getRestaurantAddress());
            builder.append(", " + menuSingleton.getClickedRestaurant().getRestaurantCity());
            builder.append(", " + menuSingleton.getClickedRestaurant().getRestaurantState());
            restaurantAddress.setText(builder);
        }

        if (restEwt != null) {
            String[] timeArray = restEwt.split(":");
            if (timeArray[0].equals("00") && timeArray[1].equals("00")) {
                restEWT.setText("Wait-" + "0" + " mins");
            } else if (!timeArray[1].equals("00")) {
                restEWT.setText("Wait-" + timeArray[1] + " mins");
            } else {
                restEWT.setText("Wait-" + timeArray[0] + "hr " + timeArray[1] + "mins");
            }
        } else {
            restEWT.setText("Wait-N/A");
        }


        // to get location specific menu
        menuSingleton.setLocationId(locationId);
        menuSingleton.setTenantId(tenantId);
        menuSingleton.setRestaurantAddress(restAddress);
        menuSingleton.setRestaurantName(restName);

        currentRest = menuSingleton.getClickedRestaurant();
//        restType = currentRest.getRestaurantType();

        if (restChat != null) {
            restChats.setText(restChat + " Posts");
        }

        if (menuSingleton.getClickedRestaurant().isQtSupported()) {
            isQTSupported = true;
        } else {
            isQTSupported = false;
        }

        isRestFav = SpecificMenuSingleton.getInstance().getClickedRestaurant().isFavoriteRestaurant();

        if (isRestFav){
            favoriteLocationImg.setImageResource(R.mipmap.ic_favorite_selected);
        }else {
            favoriteLocationImg.setImageResource(R.mipmap.ic_favorite_unselected);
        }

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                drawerLayout.openDrawer(Gravity.LEFT);
//                return true;

                Intent intent = new Intent(RestaurantServices.this, MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

        callUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleySingleton.getInstance(RestaurantServices.this).postEventRequest("CallUs", SpecificMenuSingleton.getInstance().getClickedRestaurant());
//                if (isQTSupported) {
                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantPhone() != null) {
                    startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + restPhone)));
                } else {
                    Toast.makeText(RestaurantServices.this, "Phone number is not available!", Toast.LENGTH_SHORT).show();
                }
//                } else {
//                    showNotQTSupportedMessage();
//                }
            }
        });

        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VolleySingleton.getInstance(RestaurantServices.this).postEventRequest("GetDirection", SpecificMenuSingleton.getInstance().getClickedRestaurant());
//                if (isQTSupported) {
                String getDirectionUrl = String.format("http://maps.google.com/maps?saddr=%f,%f+&daddr=%f,%f",
                        sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude);
                Log.i("GetDirection", getDirectionUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getDirectionUrl));
                startActivity(intent);
//                } else {
//                    showNotQTSupportedMessage();
//                }
            }
        });

//         FOR USER PROFILE OPTIONS
//        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                switch (position) {
//                    case 1:
//                        // User Profile
//                        startActivity(new Intent(RestaurantServices.this, UserProfileActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 2:
//                        // User Favorites Orders List
//                        startActivity(new Intent(RestaurantServices.this, OrderHistoryActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                    case 3:
//                        // Order Booking Status - Show user estimated wait time
//                        startActivity(new Intent(RestaurantServices.this, OrderConfirmationActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//
//                    case 4:
//                        shareApp();
//                        break;
//
//                    case 5:
//                        startActivity(new Intent(RestaurantServices.this, ChatImages.class));
//                        drawerLayout.closeDrawers();
//                        break;
//
//                    case 6:
//                        startActivity(new Intent(RestaurantServices.this, AboutUsActivity.class));
//                        drawerLayout.closeDrawers();
//                        break;
//                }
//            }
//        });

        Glide.with(this).load(SpecificMenuSingleton.getInstance().getClickedRestaurant().getRatingImage())
                .placeholder(R.mipmap.rating_bg)
                .into(ratingImage);

        ListView listView = (ListView) findViewById(R.id.homeScreenListView);
        listView.setAdapter(new RestaurantServicesAdapter(RestaurantServices.this));
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreferences sharedPreference = getSharedPreferences("user_info", MODE_PRIVATE);
//        String profileName = sharedPreference.getString("user_name", null);
//        String userAddress = sharedPreference.getString("user_address", null);
//        String userImageUrl = sharedPreference.getString("user_image", null);
//        userName.setText(profileName);
////        userLocation.setText(userAddress);
//        userName.setTypeface(Globals.robotoBold);
////        userLocation.setTypeface(Globals.robotoRegular);
//        Glide.with(this).load(Config.QUICK_CHAT_IMAGE + userImageUrl)
//                .asBitmap().placeholder(R.mipmap.default_profile_pic)
//                .into(userImage);
////        Glide.with(this).load("https://s3-us-west-2.amazonaws.com/stagingquicktable/profile/" + userImageUrl)
////                .asBitmap().placeholder(R.mipmap.default_profile_pic)
////                .into(userImage);
//
//        userImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        if (SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantChatAvailable() != null) {
            restChats.setText(SpecificMenuSingleton.getInstance().getClickedRestaurant()
                    .getRestaurantChatAvailable() + " Posts");
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        clickPosition = position;
        String restAccountType = SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantAccountName();
        LocationListModel restDetails = SpecificMenuSingleton.getInstance().getClickedRestaurant();
        switch (position) {

            case 0:
//                if (Config.APP_NAME.equalsIgnoreCase("QuickTable")){
                VolleySingleton.getInstance(this).postEventRequest("ViewMenus", restDetails);
                if (isQTSupported) {
                    if (SpecificMenuSingleton.getInstance().getClickedRestaurant().getRestaurantAccountName().equalsIgnoreCase("custom")){
                        if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isInteractiveMenu()){
                            Config.viewMenu = true;
                            Config.takeAway = false;
                            Intent intent = new Intent(this, MenuActivity.class);
                            intent.putExtra("RestaurantMenuUrl", restMenuUrl);
                            startActivity(intent);
                        }else {
                            showNotQTSupportedMessage();
                        }
                    }else {
                        Config.viewMenu = true;
                        Config.takeAway = false;
                        Intent intent = new Intent(this, MenuActivity.class);
                        intent.putExtra("RestaurantMenuUrl", restMenuUrl);
                        startActivity(intent);
                    }
                } else {
                    showNotQTSupportedMessage();
                }
//                }else {
//                    Config.viewMenu = true;
//                    Intent intent = new Intent(this, MenuActivity.class);
//                    intent.putExtra("RestaurantMenuUrl", restMenuUrl);
//                    startActivity(intent);
//                }
                break;

            case 1:
                if (restDetails.isDealActive() || restDetails.isSpecialOffer()){
                    Intent intent = new Intent(this, OrderHistoryActivity.class);
                    intent.putExtra("is_from_rest_list", true);
                    startActivity(intent);
                }else {
                    noOffersDialog();
                }
                break;

            case 2:
                startActivity(new Intent(this, ChatActivity.class));
                break;

            case 3:
//                if (Config.APP_NAME.equalsIgnoreCase("QuickTable")){
                if (isQTSupported) {
                    setTimeZone();
                    switch (restAccountType) {
                        case "BRONZE":
                            showNotQTSupportedMessage();
                            VolleySingleton.getInstance(this).postEventRequest("GetInLine", restDetails);
                            break;
                        case "SILVER":
                            if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                    Config.takeAway = false;
                                    Config.viewMenu = false;
                                    OrderConfirmationActivity.currentTime = null;
                                    startActivity(new Intent(this, BookYourTableActivity.class));
                                } else {
                                    Log.i("Hostess Offline", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.i("Business time", "true");
                                Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "GOLD":
                            if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                    Config.takeAway = false;
                                    Config.viewMenu = false;
                                    OrderConfirmationActivity.currentTime = null;
                                    startActivity(new Intent(this, BookYourTableActivity.class));
                                } else {
                                    Log.i("Hostess Offline", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.i("Business time", "true");
                                Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "PLATINUM":
                            if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                    Config.takeAway = false;
                                    Config.viewMenu = false;
                                    OrderConfirmationActivity.currentTime = null;
                                    startActivity(new Intent(this, BookYourTableActivity.class));
                                } else {
                                    Log.i("Hostess Offline", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.i("Business time", "true");
                                Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "CUSTOM":
                            if (restDetails.isGetInLine()) {
                                if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                    if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                        Config.takeAway = false;
                                        Config.viewMenu = false;
                                        OrderConfirmationActivity.currentTime = null;
                                        startActivity(new Intent(this, BookYourTableActivity.class));
                                    } else {
                                        Log.i("Hostess Offline", "true");
                                        Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.i("Business time", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                showNotQTSupportedMessage();
                            }
                    }
                } else {
                    showNotQTSupportedMessage();
                    VolleySingleton.getInstance(this).postEventRequest("GetInLine", restDetails);
                }
                break;

            case 4:
                SharedPreferences preferences = getSharedPreferences("ewt_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
//                if (Config.APP_NAME.equalsIgnoreCase("QuickTable")){
                VolleySingleton.getInstance(this).postEventRequest("CarryOut", restDetails);
                if (isQTSupported) {
                    setTimeZone();
                    switch (restAccountType) {
                        case "BRONZE":
                            showNotQTSupportedMessage();
                            break;
                        case "SILVER":
                            showNotQTSupportedMessage();
                            break;

                        case "GOLD":
                            if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                    if (CartSingleton.getInstance().getCartItem() != null && CartSingleton.getInstance().getCartItem().size() > 0) {
                                        if (CartSingleton.getInstance().getCartItem().get(0).getTenantId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getTenantID()
                                                && CartSingleton.getInstance().getCartItem().get(0).getLocationId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getLocationID()) {
                                            Config.viewMenu = false;
                                            Config.takeAway = true;
                                            editor.putBoolean("isDineInOnly", false);
                                            editor.putBoolean("is_patron_seated", false);
                                            editor.commit();
                                            startActivity(new Intent(this, MenuActivity.class));
                                        } else {
                                            showRestaurantVaryError();
                                        }
                                    } else {
                                        Config.viewMenu = false;
                                        Config.takeAway = true;
                                        editor.putBoolean("isDineInOnly", false);
                                        editor.putBoolean("is_patron_seated", false);
                                        editor.commit();
                                        startActivity(new Intent(this, MenuActivity.class));
                                    }
                                } else {
                                    Log.i("Hostess Offline", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.i("Business time", "true");
                                Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "PLATINUM":
                            if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                    if (CartSingleton.getInstance().getCartItem() != null && CartSingleton.getInstance().getCartItem().size() > 0) {
                                        if (CartSingleton.getInstance().getCartItem().get(0).getTenantId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getTenantID()
                                                && CartSingleton.getInstance().getCartItem().get(0).getLocationId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getLocationID()) {
                                            Config.viewMenu = false;
                                            Config.takeAway = true;
                                            editor.putBoolean("isDineInOnly", false);
                                            editor.putBoolean("is_patron_seated", false);
                                            editor.commit();
                                            startActivity(new Intent(this, MenuActivity.class));
                                        } else {
                                            showRestaurantVaryError();
                                        }
                                    } else {
                                        Config.viewMenu = false;
                                        Config.takeAway = true;
                                        editor.putBoolean("isDineInOnly", false);
                                        editor.putBoolean("is_patron_seated", false);
                                        editor.commit();
                                        startActivity(new Intent(this, MenuActivity.class));
                                    }
                                } else {
                                    Log.i("Hostess Offline", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.i("Business time", "true");
                                Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        case "CUSTOM":
                            if (restDetails.isCarryOut()) {
                                if (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0) {
                                    if (SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline()) {
                                        if (CartSingleton.getInstance().getCartItem() != null && CartSingleton.getInstance().getCartItem().size() > 0) {
                                            if (CartSingleton.getInstance().getCartItem().get(0).getTenantId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getTenantID()
                                                    && CartSingleton.getInstance().getCartItem().get(0).getLocationId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getLocationID()) {
                                                Config.viewMenu = false;
                                                Config.takeAway = true;
                                                editor.putBoolean("isDineInOnly", false);
                                                editor.commit();
                                                startActivity(new Intent(this, MenuActivity.class));
                                            } else {
                                                showRestaurantVaryError();
                                            }
                                        } else {
                                            Config.viewMenu = false;
                                            Config.takeAway = true;
                                            editor.putBoolean("isDineInOnly", false);
                                            editor.commit();
                                            startActivity(new Intent(this, MenuActivity.class));
                                        }
                                    } else {
                                        Log.i("Hostess Offline", "true");
                                        Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.i("Business time", "true");
                                    Toast.makeText(this, R.string.hostess_offline_msg, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                showNotQTSupportedMessage();
                            }
                    }
                } else {
                    showNotQTSupportedMessage();
                }
//                }else {
//                    if (CartSingleton.getInstance().getCartItem() != null && CartSingleton.getInstance().getCartItem().size() > 0){
//                        if (CartSingleton.getInstance().getCartItem().get(0).getTenantId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getTenantID()
//                                && CartSingleton.getInstance().getCartItem().get(0).getLocationId() == SpecificMenuSingleton.getInstance().getClickedRestaurant().getLocationID()) {
//                            Config.viewMenu = false;
//                            Config.takeAway = true;
//                            editor.putBoolean("isDineInOnly", false);
//                            editor.commit();
//                            startActivity(new Intent(this, MenuActivity.class));
//                        }else {
//                            showRestaurantVaryError();
//                        }
//                    }else {
//                        Config.viewMenu = false;
//                        Config.takeAway = true;
//                        editor.putBoolean("isDineInOnly", false);
//                        editor.commit();
//                        startActivity(new Intent(this, MenuActivity.class));
//                    }
//                }
                break;
        }

    }

    private void showNotQTSupportedMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage(restName + " doesn’t support this feature yet...would you like us to tell them to add this feature?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void noOffersDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("We are sorry, but there are no special offers at this restaurant today.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void postRestaurantData() {

        String url = "";

        final SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String patronName = sharedPreferences.getString("user_name", null);

        JSONObject postData = new JSONObject();
        try {
            postData.put("", restName);
            postData.put("", patronName);
            postData.put("", clickPosition);
        } catch (JSONException e) {
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private void showRestaurantVaryError() {
        SharedPreferences preferences = getSharedPreferences("ewt_info", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Items already in cart!");
        builder.setMessage("Your cart contains items from other restaurant. Would you like to reset your cart before browsing this restaurant?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CartSingleton.getInstance().clearCart();
                MenuItemFragment.updateCartCount();
                Config.viewMenu = false;
                Config.takeAway = true;
                editor.putBoolean("isDineInOnly", false);
                editor.commit();
                startActivity(new Intent(RestaurantServices.this, MenuActivity.class));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    private void setTimeZone() {
        SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone(menuSingleton.getClickedRestaurant().getTimezone()));
        currentTime = f.format(new Date());
        openTime = menuSingleton.getClickedRestaurant().getRestaurantOpenTiming();
        closeTime = menuSingleton.getClickedRestaurant().getRestaurantCloseTiming();
        int time1 = currentTime.compareToIgnoreCase(openTime);
        int time2 = currentTime.compareToIgnoreCase(closeTime);
        Log.i("Timings", "CurrentTime= " + currentTime + " OpenTime= " + openTime + " CloseTime= " + closeTime + " timestatus= " + time1 + " timestatus= " + time2);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.location_favorite:
                if (isRestFav){
                    removeFavoriteLocation();
                }else {
                    addFavoriteLocation();
                }
                break;
        }
    }

    private void addFavoriteLocation() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Adding restaurant to your favorite list.");
        dialog.show();

        String url = Config.FAVORITE_REST_LIST;

        JSONObject params = new JSONObject();
        try {
            params.put("placeId", SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (dialog.isShowing()){
                            dialog.dismiss();
                        }
                        isRestFav = true;
                        favoriteLocationImg.setImageResource(R.mipmap.ic_favorite_selected);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (dialog.isShowing()){
                    dialog.dismiss();
                }

                if (error != null) {
                    if (error instanceof TimeoutError) {
                        Config.internetSlowError(RestaurantServices.this);
                    } else if (error instanceof NoConnectionError) {
                        Config.internetSlowError(RestaurantServices.this);
                    } else if (error instanceof ServerError) {
                        Config.serverError(RestaurantServices.this);
                    }
                }

            }
        }){
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

    private void removeFavoriteLocation() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Removing restaurant from your favorite list.");
        dialog.show();

        String url = Config.FAVORITE_REST_LIST + SpecificMenuSingleton.getInstance().getClickedRestaurant().getPlaceID() + "/delete";
        Log.i("RemoveFavorite", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (dialog.isShowing()){
                            dialog.dismiss();
                        }

                        isRestFav = false;
                        favoriteLocationImg.setImageResource(R.mipmap.ic_favorite_unselected);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (dialog.isShowing()){
                    dialog.dismiss();
                }

                if (error != null) {
                    if (error instanceof TimeoutError) {
                        Config.internetSlowError(RestaurantServices.this);
                    } else if (error instanceof NoConnectionError) {
                        Config.internetSlowError(RestaurantServices.this);
                    } else if (error instanceof ServerError) {
                        Config.serverError(RestaurantServices.this);
                    }
                }

            }
        }){
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
}
