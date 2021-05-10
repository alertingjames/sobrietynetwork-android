package com.sobriety.sobriety.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLng;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class VoiceChatViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = VoiceChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    private RtcEngine mRtcEngine;// Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }
    };

    Firebase reference = null;
    String callCode, memberId, groupName = "", networkId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_chat_view);

        callCode = getIntent().getStringExtra("call_code");
        try{
            memberId = getIntent().getStringExtra("member_id");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
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
            responseCall(memberId);
        }catch (NullPointerException e){
            e.printStackTrace();
            requestCall(memberId, groupName, callCode, networkId);
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel();
        }

        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "response/" + String.valueOf(Commons.thisUser.getIdx()) + "_" + memberId);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TextView tipMsg = (TextView) findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
                tipMsg.setText("Call Established");
                ref.removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel();               // Tutorial Step 2
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;

        try{
            if(reference != null)
                reference.removeValue();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        finish();
        overridePendingTransition(0,0);
    }

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 3
    public void onEncCallClicked(View view) {
        finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void joinChannel() {
        mRtcEngine.joinChannel(null, callCode, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        Log.d("INFO+++++", String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        TextView tipMsg = (TextView) findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
        tipMsg.setText("Call Ended");

    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        Log.d("INFO+++++", String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
        TextView tipMsg = (TextView) findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
        if(muted)tipMsg.setText("The remote user Muted");
        else tipMsg.setText("The remote user UnMuted");
    }

    @JsonProperty("group_name")
    private void requestCall(String memberId, String groupName, String callCode, String networkId) {
        reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I send you a call.");
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

        sendFcmNotification("I send you a call.");
    }

    private void responseCall(String memberId) {
        Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "response/" + memberId + "_" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I received your call request.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        ref.removeValue();
        ref.push().setValue(map);
        TextView tipMsg = (TextView) findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
        tipMsg.setText("Call Established");
    }

    public void sendFcmNotification(String message) {

        String url = ReqConst.SERVER_URL + "sendnotification";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseSendNotificationResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("debug", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("member_id", String.valueOf(memberId));
                params.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("text", message);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseSendNotificationResponse(String json) {
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESP=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                Log.d("ResultMessage+++", response.getString("message"));
            } else {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}






























