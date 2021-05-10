package com.sobriety.sobriety.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;

// Created by batman on 3/4/18.

public class FcmInstanceIdService extends FirebaseInstanceIdService
{
    public void onTokenRefresh()
    {
        String recent_token = FirebaseInstanceId.getInstance().getToken();

//        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(getString(R.string.FCM_TOKEN), recent_token);
//        editor.commit();

        Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_TOKEN, recent_token);
        Log.d("TOKEN+++", recent_token);

    }
}

