package com.app.mobi.quicktabledemo.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.fragments.SeatSelectionFragment;
import com.app.mobi.quicktabledemo.fragments.WaitConformationFragment;

public class BookYourTableActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dine_in);

        Fragment fragment = null;
        if (getIntent() != null) {
            if (getIntent().hasExtra("isDineIn")) {
                fragment = new WaitConformationFragment();

            } else {
                fragment = SeatSelectionFragment.newInstance();
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.pre_order, fragment).commit();

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
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            return;
        }
        super.onBackPressed();
    }

}
