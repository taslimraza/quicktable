package com.app.mobi.quicktabledemo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.modelClasses.MainMenuListModel;
import com.app.mobi.quicktabledemo.utils.Config;
import com.app.mobi.quicktabledemo.utils.Globals;

import java.util.ArrayList;

/**
 * Created by mobi11 on 26/9/16.
 */
public class MainMenuListAdapter extends BaseAdapter {

    private ArrayList<MainMenuListModel> mainMenuListModels;
    private Context context;

    public MainMenuListAdapter( Context context, ArrayList<MainMenuListModel> mainMenuListModels) {
        this.mainMenuListModels = mainMenuListModels;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mainMenuListModels.size();
    }

    @Override
    public Object getItem(int position) {
        return mainMenuListModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.main_menu_single, parent, false);
        }

        ImageView mainMenuImage = (ImageView) convertView.findViewById(R.id.main_menu_image);
        TextView mainMenuTitle = (TextView) convertView.findViewById(R.id.main_menu_title);
        TextView mainMenuDescription = (TextView) convertView.findViewById(R.id.main_menu_description);

        MainMenuListModel mainMenuListModel = mainMenuListModels.get(position);

        mainMenuImage.setImageResource(mainMenuListModel.getMainMenuImage());
        mainMenuTitle.setText(mainMenuListModel.getMainMenuTitle());
        mainMenuDescription.setText(mainMenuListModel.getMainMenuDescription());

        return convertView;
    }
}
