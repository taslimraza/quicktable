package com.app.mobi.quicktabledemo.utils;

import com.app.mobi.quicktabledemo.modelClasses.FavoriteMenuItem;
import com.app.mobi.quicktabledemo.modelClasses.MenuItemModel;

import java.util.ArrayList;

/**
 * Created by mobi11 on 20/4/16.
 */
public interface passFavoriteData {
    void passData(ArrayList<MenuItemModel> menuItemModels, ArrayList<FavoriteMenuItem> favoriteMenuItems);
}
