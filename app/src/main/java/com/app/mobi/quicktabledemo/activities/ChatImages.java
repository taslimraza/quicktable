package com.app.mobi.quicktabledemo.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.fragments.QCGridImagesFragment;
import com.app.mobi.quicktabledemo.fragments.QCImagesFragment;

public class ChatImages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_images);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView homeBtn = (ImageView) findViewById(R.id.menu_button);
        ImageView cartButton = (ImageView) findViewById(R.id.cart_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
        }

        toolbar.setTitle("Review All QuickPics");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cartButton.setVisibility(View.GONE);

//        homeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ChatImages.this, MainMenuActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//
//            }
//        });

        QCImagesFragment fragment = new QCImagesFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.QCImagesFrameLayout, fragment)
                .commit();
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
    public void onBackPressed() {
        super.onBackPressed();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null){
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
