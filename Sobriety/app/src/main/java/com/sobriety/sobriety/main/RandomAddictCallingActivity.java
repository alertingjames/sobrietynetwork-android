package com.sobriety.sobriety.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.firebase.client.Firebase;
import com.gruveo.sdk.Gruveo;
import com.gruveo.sdk.model.CallEndReason;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.MemberLocation;
import com.sobriety.sobriety.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RandomAddictCallingActivity extends AppCompatActivity {

    private static final int REQUEST_CALL = 1;
    private static final String SIGNER_URL = "https://api-demo.gruveo.com/signer";

    ProgressBar progressBar;
    ArrayList<MemberLocation> locations = new ArrayList<>();
    TextView name, status;
    CircleImageView photo;
    int userId = 1;
    ArrayList<Integer> oldMemberIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_random_addict_calling);

        oldMemberIds.clear();

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        status = (TextView)findViewById(R.id.connectionStatus);
        photo = (CircleImageView)findViewById(R.id.photo);
        name = (TextView)findViewById(R.id.name);

        Picasso.with(getApplicationContext())
                .load(Commons.thisUser.getPhotoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(photo);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "ag-futura-58e274b5588ad.ttf");
        name.setTypeface(font);
        name.setText(rename(Commons.thisUser.getName()));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        status.setText("Searching ...");
        getRandomMember(String.valueOf(Commons.thisUser.getIdx()));
    }

    private String rename(String name){
        String firstName = "", lastName = "";
        if(name.contains(" ")){
            if(name.indexOf(" ") >= 1) {
                firstName = name.substring(0, name.indexOf(" "));
                lastName=name.substring(name.indexOf(" ")+1,name.length());
            }
            else {
                firstName=name;
                lastName="";
            }
        }else {
            firstName=name;
            lastName="";
        }
        if(lastName.length() != 0)
            return firstName + " " + lastName.substring(0, 1) + ".";
        else return firstName;
    }

    public void findMore(View view){
        status.setText("Searching ...");
        getAnotherRandomMember(String.valueOf(Commons.thisUser.getIdx()));
    }

    private void initCall(String code) {
        final Bundle otherExtras = new Bundle();
        otherExtras.putBoolean(Gruveo.GRV_EXTRA_VIBRATE_IN_CHAT, false);
        otherExtras.putBoolean(Gruveo.GRV_EXTRA_DISABLE_CHAT, false);

        final String result = new Gruveo.Builder(this)
                .callCode(code)
                .videoCall(false)
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

    public void getRandomMember(String memberId) {

        String url = ReqConst.SERVER_URL + "getRandomMember";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetMemberResponse(response);

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressBar.setVisibility(View.GONE);
                showToast("Server issue");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("member_id", memberId);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void showToast(String content){
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.toast_view, null);
        TextView textView=(TextView)dialogView.findViewById(R.id.text);
        textView.setText(content);
        Toast toast=new Toast(this);
        toast.setView(dialogView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public void parseGetMemberResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                JSONObject object = response.getJSONObject("data");
                User user = new User();
                user.setIdx(object.getInt("id"));
                user.setName(object.getString("name"));
                user.setUsername(object.getString("username"));
                user.setGender(object.getString("gender"));
                user.setPhoneNumber(object.getString("phone_number"));
                user.setPhotoUrl(object.getString("photo_url"));
                user.setCleanDate(object.getString("clean_date"));

                userId = user.getIdx();
                if(!oldMemberIds.contains(userId))
                    oldMemberIds.add(userId);
                String callCode = user.getUsername() + String.valueOf(Commons.thisUser.getUsername());
//                requestCall(String.valueOf(user.getIdx()), "", callCode, String.valueOf(Commons.myNetworkId));
//                initCall(callCode);
                connectVoiceCall(String.valueOf(user.getIdx()), "", callCode, String.valueOf(Commons.myNetworkId));

            } else if(success.equals("1")){
                status.setText("You don't have any member around you for call.");
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    @JsonProperty("group_name")
    private void requestCall(String memberId, String groupName, String callCode, String networkId) {
        Firebase reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I send you a random call.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("group_name", groupName);
        map.put("network_id", networkId);
        map.put("call_code", callCode);
        map.put("option", "random");
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        map.put("sender_phone", Commons.thisUser.getPhoneNumber());
        map.put("sender_name", Commons.thisUser.getUsername());
        map.put("sender_photo", Commons.thisUser.getPhotoUrl());
        reference.removeValue();
        reference.push().setValue(map);
    }

    public void getAnotherRandomMember(String memberId) {

        String url = ReqConst.SERVER_URL + "getAnotherRandomMember";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(com.android.volley.Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetAnotherMemberResponse(response);

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressBar.setVisibility(View.GONE);
                showToast("Server issue");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("member_id", memberId);
                params.put("old_user_id", String.valueOf(userId));
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseGetAnotherMemberResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                JSONObject object = response.getJSONObject("data");
                User user = new User();
                user.setIdx(object.getInt("id"));
                user.setName(object.getString("name"));
                user.setUsername(object.getString("username"));
                user.setGender(object.getString("gender"));
                user.setPhoneNumber(object.getString("phone_number"));
                user.setPhotoUrl(object.getString("photo_url"));
                user.setCleanDate(object.getString("clean_date"));

                userId = user.getIdx();
                if(!oldMemberIds.contains(userId))
                    oldMemberIds.add(userId);
                String callCode = user.getUsername() + String.valueOf(Commons.thisUser.getUsername());
//                requestCall(String.valueOf(user.getIdx()), "", callCode, String.valueOf(Commons.myNetworkId));
//                initCall(callCode);
                connectVoiceCall(String.valueOf(user.getIdx()), "", callCode, String.valueOf(Commons.myNetworkId));

            } else if(success.equals("1")){
                status.setText("You don't have any member around you for call.");
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    private void connectVoiceCall(String memberId, String groupName, String callCode, String networkId){
        status.setText("Disconnected");
        Intent intent = new Intent(getApplicationContext(), VoiceChatViewActivity.class);
        intent.putExtra("call_code", callCode);
        intent.putExtra("member_id", memberId);
        intent.putExtra("group_name", groupName);
        intent.putExtra("network_id", networkId);
        startActivity(intent);
    }
}











































