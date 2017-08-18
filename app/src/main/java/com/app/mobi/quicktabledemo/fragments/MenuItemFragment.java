package com.app.mobi.quicktabledemo.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.activities.OrderConfirmationActivity;
import com.app.mobi.quicktabledemo.adapters.MenuItemRecyclerAdapter;
import com.app.mobi.quicktabledemo.adapters.PlaceOrderAdapter;
import com.app.mobi.quicktabledemo.modelClasses.MenuChoicesModel;
import com.app.mobi.quicktabledemo.modelClasses.MenuDetailsModel;
import com.app.mobi.quicktabledemo.modelClasses.MenuItemModel;
import com.app.mobi.quicktabledemo.modelClasses.MenuOptionModel;
import com.app.mobi.quicktabledemo.modelClasses.PlaceOrderModel;
import com.app.mobi.quicktabledemo.utils.CartSingleton;
import com.app.mobi.quicktabledemo.utils.CustomRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuItemFragment extends Fragment {

    private TextView menuItemName;
    private CustomRecyclerView menuItemRecyclerView;
    private ArrayList<MenuItemModel> categoryModel;
    private ArrayList<PlaceOrderModel> orderSizeList, orderToppingsList, orderExtraOptionsList;
    private PlaceOrderAdapter orderSizeAdapter, orderToppingsAdapter, orderOptionsAdapter;
    private PlaceOrderModel orderSizeModel, orderToppingsModel, orderExtraOptionsModel;
    private LinearLayout orderDescriptionLayout;
    private RelativeLayout orderSizeLayout;
    private RelativeLayout orderToppingsLayout;
    private RelativeLayout orderSpecialInstructionsLayout;
    private RelativeLayout orderOptionsLayout;
    private AlertDialog orderDialog;
    private boolean isAddToCartClicked = false;
    private String orderName;
    private int itemPosition;
    private int orderSizePosition, orderToppingPosition, orderOptionsPosition = 0;
    private static ArrayList<MenuItemModel> menuItemModels;
    private ArrayList<MenuItemModel> itemModels;
    private MenuDetailsModel menuDetailsModels;
    private ArrayList<MenuOptionModel> menuOptionModels;
    private static TextView cartCount;
    private static Integer itemCount = 0;
    private static MenuItemRecyclerAdapter adapter;
    private int choiceCount;
    private HashMap<String, ArrayList<MenuChoicesModel>> menuChoicesHashMap, menuChoices;
    private ArrayList<MenuChoicesModel> menuChoicesModels;

    public MenuItemFragment(MenuDetailsModel menuDetailsModels) {
        this.menuDetailsModels = menuDetailsModels;
        // Required empty public constructor
    }

    public MenuItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_item, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String itemName = getArguments().getString("itemName");
        itemPosition = getArguments().getInt("itemPosition");
        menuItemName = (TextView) getActivity().findViewById(R.id.item_name);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.app_bar);
        menuItemRecyclerView= (CustomRecyclerView) getActivity().findViewById(R.id.selectedCategoryRecyclerView);
        cartCount = (TextView) getActivity().findViewById(R.id.cart_items);

        toolbar.setTitle("Menu-Item");
        if(itemCount>0){
            cartCount.setVisibility(View.VISIBLE);
        }else {
            cartCount.setVisibility(View.GONE);
        }
//        cartCount.setText("0");
        menuItemName.setText(itemName);
        setupRecyclerViewAdapter(menuItemRecyclerView, itemPosition);

//        menuItemRecyclerView.addOnItemTouchListener(
//                new RecyclerViewClickListener(getActivity(),
//                        new RecyclerViewClickListener.OnClickListener() {
//                            @Override
//                            public void onClick(View view, int position) {
//                                switch(view.getId()){
//                                    case R.id.option_button:
//                                        Toast.makeText(getActivity(), "Options"+position, Toast.LENGTH_SHORT).show();
//                                        break;
//                                }
//                            }
//                        })
//        );
//        menuItemRecyclerView.addOnItemTouchListener(
//                new RecyclerViewItemClickListener(getActivity(),
//                        new RecyclerViewItemClickListener.OnItemClickListener() {
//                            @Override
//                            public void onItemClick(View view, int position) {
//                                Log.d("Position", "" + position);
//                                Log.d("Item Name", "" + categoryModel.get(position).getMenuSectionName());
//                                orderName = categoryModel.get(position).getMenuSectionName();
//                                handleOrderDialog(orderName);
//                            }
//                        }));
    }

    private void setupRecyclerViewAdapter(RecyclerView selectedCategoryRecyclerView, int itemPosition) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        selectedCategoryRecyclerView.setLayoutManager(linearLayoutManager);
        categoryModel = new ArrayList<>();
        menuDetailsModels = MenuDetailsModel.getInstance().getMenuDetailsModel();
        categoryModel = menuDetailsModels.getMenuItemModels();
        menuItemModels =  new ArrayList<>();
        for (int i=0; i<categoryModel.size(); i++){
            if(menuDetailsModels.getOrderMenuModels().get(itemPosition).getMenuSectionId() == categoryModel.get(i).getMenuSectionId()){
                menuItemModels.add(categoryModel.get(i));
            }
        }
        itemModels = new ArrayList<>();
        itemModels.addAll(menuItemModels);

        menuOptionModels = new ArrayList<>();
        ArrayList<Map<String, ArrayList<MenuChoicesModel>>> menuChoicesArrayList = new ArrayList<>();
        choiceCount = 0;
        for(int k=0; k<itemModels.size(); k++){
            Map<String, ArrayList<MenuChoicesModel>> menuChoicesHashMap = new LinkedHashMap<>();
            for (int i = 0; i < menuDetailsModels.getMenuOptionModels().size(); i++) {
                if (itemModels.get(k).getMenuItemId() == menuDetailsModels.getMenuOptionModels().get(i).getMenuItemId()) {
                    menuOptionModels.add(menuDetailsModels.getMenuOptionModels().get(i));
                    if (menuOptionModels.size() > 0) {
                        menuChoicesModels = new ArrayList<>();
                        for (int j = 0; j < menuDetailsModels.getMenuChoicesModels().size(); j++) {
                            if (menuOptionModels.size() != choiceCount) {
                                // if choice size and option size matches no need to compare anymore
                                if (menuOptionModels.get(choiceCount).getMenuOptionId() == menuDetailsModels.getMenuChoicesModels().get(j).getMenuOptionId()) {
                                    MenuChoicesModel menuChoicesModel = new MenuChoicesModel();
                                    menuChoicesModel = menuDetailsModels.getMenuChoicesModels().get(j);
                                    menuChoicesModels.add(menuChoicesModel);
                                }
                            }
                        }
                        menuChoicesHashMap.put(menuOptionModels.get(choiceCount).getMenuOptionName(), menuChoicesModels);
                        choiceCount++;
                    }
                }
            }
            menuChoicesArrayList.add(menuChoicesHashMap);
        }

        MenuDetailsModel.getInstance().setMenuChoicesHashMap(menuChoicesHashMap);
        adapter = new MenuItemRecyclerAdapter(getActivity(),menuItemModels,menuDetailsModels,itemPosition, menuChoicesArrayList, menuOptionModels);
        selectedCategoryRecyclerView.setItemAnimator(null);
        selectedCategoryRecyclerView.setAdapter(adapter);
    }

    public static Integer getCountItem(){
        return itemCount;
    }

    public static void updateCartCount() {
        CartSingleton cartSingleton = CartSingleton.getInstance();
        itemCount = cartSingleton.getItemCount();
        if (cartCount != null){
            if (itemCount == 0) {
                cartCount.setVisibility(View.GONE);
            } else {
                cartCount.setVisibility(View.VISIBLE);
                cartCount.setText(itemCount.toString());
            }

            if (cartSingleton.getCartItem().size() > 0) {
                for (int i = 0; i < menuItemModels.size(); i++) {
                    for (int j = 0; j < cartSingleton.getCartItem().size(); j++) {
                        if (menuItemModels.get(i).getMenuItemId() == cartSingleton.getCartItem().get(j).getItemId()) {
                            menuItemModels.get(i).setItemQuantity(Integer.valueOf(cartSingleton.getCartItem().get(j).getItemQuantity()));
                            break;
                        }
                        menuItemModels.get(i).setItemQuantity(0);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else if (cartSingleton.getCartItem().size() == 0) {
                for (int i = 0; i < menuItemModels.size(); i++) {
                    menuItemModels.get(i).setItemQuantity(0);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

}
