package com.app.mobi.quicktabledemo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.modelClasses.LocationListModel;
import com.app.mobi.quicktabledemo.utils.SpecificMenuSingleton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mobi11 on 25/9/15.
 */
public class RestaurantServicesAdapter extends BaseAdapter {

    private String[] titleList = {"View Menus", "Special Offers", "Quick Chat","Dine In", "Carry Out"};
    private String[] subTitleList = {"Check Out Today's Menu", "See Discount Offers", "View and Post when at a Restaurant",
                        "Get-In line & Pre order your meal", "Place your order"};

    private int[] imageList= {R.mipmap.icon_menu,
            R.mipmap.ic_rest_special_offers,
            R.mipmap.ic_rest_quickchat,
            R.mipmap.icon_dinein,
            R.mipmap.icon_takeaway};
    private Context mContext;
    private LocationListModel restDetails;
    private String currentTime = null, openTime = null, closeTime = null;

    public RestaurantServicesAdapter(Context context) {
        this.mContext = context;
        restDetails = SpecificMenuSingleton.getInstance().getClickedRestaurant();
        setTimeZone();
    }


    @Override
    public int getCount() {
        return titleList.length;
    }

    @Override
    public Object getItem(int position) {
        return titleList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.homescreen_single_listview, parent, false);
        TextView title = (TextView) view.findViewById(R.id.single_title);
        TextView subTitle = (TextView) view.findViewById(R.id.single_description);
        ImageView imageView= (ImageView) view.findViewById(R.id.single_image);
        title.setText(titleList[position]);
        subTitle.setText(subTitleList[position]);
        imageView.setImageResource(imageList[position]);

        if (position == 0) {
            if (!restDetails.isQtSupported()) {
                // Gray out
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(cf);
                title.setTextColor(Color.GRAY);
                subTitle.setTextSize(Color.GRAY);
            }
        }

        if (position == 1) {
            if (!(restDetails.isSpecialOffer() || restDetails.isDealActive())) {
                // Gray out
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(cf);
                title.setTextColor(Color.GRAY);
                subTitle.setTextSize(Color.GRAY);
            }
        }

        if (position == 3) {
         if (!(restDetails.isGetInLine() && SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline() &&
                 (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0))){
//             view.setBackgroundColor(Color.parseColor("#c0c0c0"));
             ColorMatrix matrix = new ColorMatrix();
             matrix.setSaturation(0);  //0 means grayscale
             ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
             imageView.setColorFilter(cf);
             title.setTextColor(Color.GRAY);
             subTitle.setTextSize(Color.GRAY);
         }
        }

        if (position == 4) {
            if (!(restDetails.isCarryOut() && SpecificMenuSingleton.getInstance().getClickedRestaurant().isHostessOnline() &&
                    (currentTime.compareToIgnoreCase(openTime) >= 0 && currentTime.compareToIgnoreCase(closeTime) <= 0))){
//                view.setBackgroundColor(Color.parseColor("#c0c0c0"));
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);  //0 means grayscale
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                imageView.setColorFilter(cf);
                title.setTextColor(Color.GRAY);
                subTitle.setTextSize(Color.GRAY);
            }
        }

        return view;
    }

    private void setTimeZone() {
        SpecificMenuSingleton menuSingleton = SpecificMenuSingleton.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        if (menuSingleton.getClickedRestaurant().isQtSupported()) {
            f.setTimeZone(TimeZone.getTimeZone(menuSingleton.getClickedRestaurant().getTimezone()));
            currentTime = f.format(new Date());
            openTime = menuSingleton.getClickedRestaurant().getRestaurantOpenTiming();
            closeTime = menuSingleton.getClickedRestaurant().getRestaurantCloseTiming();
        }
    }
}
