package com.app.mobi.quicktabledemo.gcmServices;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by mobigesture-hp on 28/7/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent=new Intent(this,RegistrationIntentService.class);
        startService(intent);
    }
}
