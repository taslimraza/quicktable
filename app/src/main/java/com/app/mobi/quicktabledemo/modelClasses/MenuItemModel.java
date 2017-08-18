package com.app.mobi.quicktabledemo.modelClasses;

import java.util.ArrayList;

/**
 * Created by mobi11 on 27/9/15.
 */
public class MenuItemModel {

    private Integer menuItemId;
    private String menuItemImage;
    private String menuItemName;
    private String menuItemDescription = null;
    private String menuItemCount;
    private Integer menuSectionId;
    private Integer itemQuantity;
    private String specialRequest;
    private boolean isFavorite = false;
    private int favoriteId = 0;
    private ArrayList<MenuOptionModel> menuOptionModels;

    public String getSpecialRequest() {
        return specialRequest;
    }

    public void setSpecialRequest(String specialRequest) {
        this.specialRequest = specialRequest;
    }

    public Integer getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Integer menuItemId) {
        this.menuItemId = menuItemId;
    }

    public Integer getMenuSectionId() {
        return menuSectionId;
    }

    public void setMenuSectionId(Integer menuSectionId) {
        this.menuSectionId = menuSectionId;
    }

    public String getMenuItemImage() {
        return menuItemImage;
    }

    public void setMenuItemImage(String menuItemImage) {
        this.menuItemImage = menuItemImage;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemCount() {
        return menuItemCount;
    }

    public void setMenuItemCount(String menuItemCount) {
        this.menuItemCount = menuItemCount;
    }

    public String getMenuItemDescription() {
        return menuItemDescription;
    }

    public void setMenuItemDescription(String menuItemDescription) {
        this.menuItemDescription = menuItemDescription;
    }

    public Integer getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(Integer itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public ArrayList<MenuOptionModel> getMenuOptionModels() {
        return menuOptionModels;
    }

    public void setMenuOptionModels(ArrayList<MenuOptionModel> menuOptionModels) {
        this.menuOptionModels = menuOptionModels;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }
}
