package com.app.mobi.quicktabledemo.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.adapters.CategoryAdapter;
import com.app.mobi.quicktabledemo.modelClasses.CategoryModel;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.ArrayList;

// American -> newamerican, tradamerican
// BBQ-> bbq, Breakfast->breakfast_brunch, Burgers-> burgers
// Chinese -> chinese, Cafes-> cafes
// Diners -> diners
// French->french
// German -> german,Greek->greek , Gastro Pubs-> gastropubs
// Indian->indpak ,Irish->irish ,Italian->italian
// Japanese->japanese ,Jewish-> jewish
// Mexican-> mexican
// Pizza-> pizza
// Seafood->seafood ,Steakhouse ->steak , Sandwiches->sandwiches ,Sushi->sushi
// Tex Mex->tex-mex , Thai->thai
// Mediterranean -> mediterranean, Tapas -> tapasmallplates, Spanish -> spanish, Fast Food-> hotdogs, Vegan -> vegan


public class FindRestaurantActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private String[] categoriesListName = {"All", "BBQ", "American New", "Breakfast", "American Trad.","Buffets","Chinese", "Burgers", "French", "Cafes", "German",
            "Caribbean", "Greek", "Chicken Wings", "Indian", "Delis", "Irish", "Diners", "Italian", "Pizza", "Japanese",
            "Sandwiches", "Jewish", "Seafood", "Mexican", "Steakhouse", "Tex-Mex", "Sushi", "Thai","Vegetarian"/*, "Mediterranean", "Spanish",
            "Tapas", "Vegan", "Fast Food"*/};
    private ArrayList<CategoryModel> categoryModels;
    private String searchedRestaurantName;
    private TextView searchRestaurantName;
    private int categoryCount = 0;
    private CategoryAdapter categoryAdapter;
    private boolean isSearchByName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_restaurant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RelativeLayout cartContent = (RelativeLayout) findViewById(R.id.cart_content);
        ImageView menu = (ImageView) findViewById(R.id.menu_button);
        GridView categoriesList = (GridView) findViewById(R.id.category_list);
        Button findMyFoodBtn = (Button) findViewById(R.id.find_food_btn);
        searchRestaurantName = (TextView) findViewById(R.id.search_name);

        findMyFoodBtn.setOnClickListener(this);
        searchRestaurantName.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
        }

        toolbar.setTitle("Find Restaurants");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        cartContent.setVisibility(View.GONE);
        menu.setVisibility(View.GONE);

        setUpData();

        categoryAdapter = new CategoryAdapter(this, categoryModels);
        categoriesList.setAdapter(categoryAdapter);
        categoriesList.setOnItemClickListener(this);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categoriesListName);
//        categoriesList.setAdapter(adapter);

    }

    private void setUpData() {
        categoryModels = new ArrayList<>();
        for (int i = 0; i < categoriesListName.length; i++) {
            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setCategoryName(categoriesListName[i]);
            categoryModel.setSelected(false);
            categoryModels.add(categoryModel);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.find_food_btn:
                Intent intent = new Intent(this, ListOfRestaurantActivity.class);
                intent.putExtra("searched_restaurant_name", searchedRestaurantName);
                StringBuilder category = new StringBuilder();
                for (int i = 0; i < categoryModels.size(); i++) {
                    if (categoryModels.get(i).isSelected()) {
                        if (category.toString().isEmpty()) {
                            if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("american new")) {
                                category.append("newamerican");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("American Trad.")) {
                                category.append("tradamerican");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("breakfast")) {
                                category.append("breakfast_brunch");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("steakhouse")) {
                                category.append("steak");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("indian")) {
                                category.append("indpak");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Chicken Wings")) {
                                category.append("chicken_wings");
                            }/* else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Tapas")) {
                                category.append("tapasmallplates");
                            }*//*  else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Fast Food")) {
                                category.append("hotdogs");
                            }*/ else {
                                if (categoryModels.get(i).getCategoryName().contains(" ")) {
                                    category.append(categoryModels.get(i).getCategoryName().toLowerCase().replace(" ", ""));
                                } else {
                                    category.append(categoryModels.get(i).getCategoryName().toLowerCase());
                                }
                            }
                        } else {
                            if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("american new")) {
                                category.append("," + "newamerican");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("American Trad.")) {
                                category.append(",tradamerican");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("breakfast")) {
                                category.append(",breakfast_brunch");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("steakhouse")) {
                                category.append(",steak");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("indian")) {
                                category.append(",indpak");
                            } else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Chicken Wings")) {
                                category.append(",chicken_wings");
                            }/* else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Tapas")) {
                                category.append(",tapasmallplates");
                            }*/ /* else if (categoryModels.get(i).getCategoryName().equalsIgnoreCase("Fast Food")) {
                                category.append(",hotdogs");
                            } */else {
                                if (categoryModels.get(i).getCategoryName().contains(" ")) {
                                    category.append("," + categoryModels.get(i).getCategoryName().toLowerCase().replace(" ", ""));
                                } else {
                                    category.append("," + categoryModels.get(i).getCategoryName().toLowerCase());
                                }
                            }
                        }
                    }
                }
                intent.putExtra("searched_category", category.toString());
                intent.putExtra("isSearchByName", isSearchByName);
                startActivity(intent);
                break;

            case R.id.search_name:
//                showSearchDialog();
                startActivityForResult(new Intent(FindRestaurantActivity.this, SearchLocationActivity.class), 1);
                break;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                searchedRestaurantName = data.getStringExtra("search_location");
//                String[] restName = searchedRestaurantName.split(",");
//                searchedRestaurantName = restName[0];
                searchRestaurantName.setText(searchedRestaurantName);
                boolean currentLocation = data.getBooleanExtra("current_location", false);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0) {
            categoryCount = 0;
            if (categoryModels.get(0).isSelected()) {
                categoryModels.get(0).setSelected(false);
            } else {
                categoryModels.get(0).setSelected(true);
                for (int i = 1; i < categoryModels.size(); i++) {
                    categoryModels.get(i).setSelected(false);
                }
            }
            categoryAdapter.notifyDataSetChanged();
        } else {
            if (categoryModels.get(position).isSelected()) {
                categoryCount--;
                categoryModels.get(position).setSelected(false);
            } else {
                categoryCount++;
                if (categoryCount <= 5) {
                    categoryModels.get(position).setSelected(true);
                }
            }
            categoryModels.get(0).setSelected(false);
            categoryAdapter.notifyDataSetChanged();
        }

        if (categoryCount > 5) {
            categoryCount--;
            Toast.makeText(this, "You have already selected 5 categories! ", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search by:-");
        final String[] searchBy = {"Restaurant name", "Location/Address"};

        builder.setItems(searchBy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which){
                    case 0:
                        isSearchByName = true;
                        break;
                    case 1:
                        isSearchByName = false;
                        break;
                }
                startActivityForResult(new Intent(FindRestaurantActivity.this, SearchLocationActivity.class), 1);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
