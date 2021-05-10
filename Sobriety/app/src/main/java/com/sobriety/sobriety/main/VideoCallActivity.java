package com.sobriety.sobriety.main;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.firebase.client.Firebase;
import com.gruveo.sdk.Gruveo;
import com.gruveo.sdk.model.CallEndReason;
import com.marozzi.roundbutton.RoundButton;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoCallActivity extends AppCompatActivity {

    private static final int REQUEST_CALL = 1;
    private static final String SIGNER_URL = "https://api-demo.gruveo.com/signer";
    Firebase reference = null;

    String callCode, memberId, groupName = "", networkId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        callCode = getIntent().getStringExtra("call_code");
        memberId = getIntent().getStringExtra("member_id");
        try{
            groupName = getIntent().getStringExtra("group_name");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try{
            networkId = getIntent().getStringExtra("network_id");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try{
            Commons.message.getFirebase().removeValue();
            Commons.message = null;
        }catch (NullPointerException e){
            e.printStackTrace();
            requestCall(memberId, groupName, callCode, networkId);
        }

        initCall(callCode);
    }

    @JsonProperty("group_name")
    private void requestCall(String memberId, String groupName, String callCode, String networkId) {
        reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I send you a video call.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("group_name", groupName);
        map.put("network_id", networkId);
        map.put("call_code", callCode);
        map.put("option", "");
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        map.put("sender_phone", Commons.thisUser.getPhoneNumber());
        map.put("sender_name", Commons.thisUser.getUsername());
        map.put("sender_photo", Commons.thisUser.getPhotoUrl());
        reference.removeValue();
        reference.push().setValue(map);
    }

    private void initCall(String callCode) {
        final Bundle otherExtras = new Bundle();
        otherExtras.putBoolean(Gruveo.GRV_EXTRA_VIBRATE_IN_CHAT, false);
        otherExtras.putBoolean(Gruveo.GRV_EXTRA_DISABLE_CHAT, false);

        final String result = new Gruveo.Builder(this)
                .callCode(callCode)
                .videoCall(true)
                .clientId("demo")
                .requestCode(REQUEST_CALL)
                .otherExtras(otherExtras)
                .eventsListener(eventsListener)
                .build();

        switch (result) {
            case Gruveo.GRV_INIT_MISSING_CALL_CODE: {
                break;
            }
            case Gruveo.GRV_INIT_INVALID_CALL_CODE: {
                break;
            }
            case Gruveo.GRV_INIT_MISSING_CLIENT_ID: {
                break;
            }
            case Gruveo.GRV_INIT_OFFLINE: {
                break;
            }
            default: {
                break;
            }
        }
    }

    private Gruveo.EventsListener eventsListener = new Gruveo.EventsListener() {
        @Override
        public void callInit(boolean videoCall, String code) {
        }

        @Override
        public void requestToSignApiAuthToken(String token) {
            try {
                Gruveo.Companion.authorize(signToken(token));
            } catch (IOException ignored) {
            }
        }

        @Override
        public void callEstablished(String code) {

        }

        @Override
        public void callEnd(Intent data, boolean isInForeground) {
            parseCallExtras(data);
            try{
                if(reference != null)
                    reference.removeValue();
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            finish();
            overridePendingTransition(0,0);
        }

        @Override
        public void recordingStateChanged(boolean us, boolean them) {
        }

        @Override
        public void recordingFilename(String filename) {
        }
    };

    private String signToken(String token) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), token);
        Request request = new Request.Builder()
                .url(SIGNER_URL)
                .post(body)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CALL && resultCode == RESULT_OK && data != null) {
            parseCallExtras(data);
        }
    }

    private void parseCallExtras(Intent data) {
        CallEndReason endReason = (CallEndReason) data.getSerializableExtra(Gruveo.GRV_RES_CALL_END_REASON);
        String callCode = data.getStringExtra(Gruveo.GRV_RES_CALL_CODE);
        String leftMessageTo = data.getStringExtra(Gruveo.GRV_RES_LEFT_MESSAGE_TO);
        int duration = data.getIntExtra(Gruveo.GRV_RES_CALL_DURATION, 0);
        int messagesSent = data.getIntExtra(Gruveo.GRV_RES_MESSAGES_SENT, 0);

        switch (endReason) {
            case BUSY: {
                break;
            }
            case HANDLE_BUSY: {
                break;
            }
            case HANDLE_UNREACHABLE: {
                break;
            }
            case HANDLE_NONEXIST: {
                break;
            }
            case FREE_DEMO_ENDED: {
                break;
            }
            case ROOM_LIMIT_REACHED: {
                break;
            }
            case NO_CONNECTION: {
                break;
            }
            case INVALID_CREDENTIALS: {
                break;
            }
            case UNSUPPORTED_PROTOCOL_VERSION: {
                break;
            }
            case OTHER_PARTY: {
                break;
            }
            default: {      // USER - we hanged up the call
                break;
            }
        }
    }
}






























