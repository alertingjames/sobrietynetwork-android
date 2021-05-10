package com.sobriety.sobriety.main;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.firebase.client.FirebaseException;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.CallNotiListAdapter;
import com.sobriety.sobriety.adapters.NotificationListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<String> phoneList = new ArrayList<>();
    ArrayList<Message> messages = new ArrayList<>();
    NotificationListAdapter adapter = new NotificationListAdapter(this);
    ArrayList<String> phoneList2 = new ArrayList<>();
    ArrayList<Message> messages2 = new ArrayList<>();
    CallNotiListAdapter adapter2 = new CallNotiListAdapter(this);
    ArrayList<Message> totals = new ArrayList<>();

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_notification);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        title=(TextView)findViewById(R.id.title);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        ui_RefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        ui_RefreshLayout.setOnRefreshListener(this);
        ui_RefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);

        searchBar = (LinearLayout)findViewById(R.id.search_bar);
        searchButton = (ImageView)findViewById(R.id.searchButton);
        cancelButton = (ImageView)findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().toLowerCase(Locale.getDefault());
                if(text.length() == 0){
                    adapter.setDatas(messages);
                }else {
                    adapter.filter(text);
                }
            }
        });

        list = (ListView) findViewById(R.id.list);
        updateUI();

        if(Commons.messages.size() > 0){
            uploadNewNotifications(String.valueOf(Commons.thisUser.getIdx()));
        }else
            ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);

        new Timer().schedule(doAsynchronousTask3, 0, 2000);
    }

    Handler mHandler = new Handler();

    TimerTask doAsynchronousTask3 = new TimerTask() {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(Commons.event){
                        updateUI();
                    }
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        doAsynchronousTask3.cancel();
        Log.d("Destroy+++", "Yes");
    }

    @Override
    public void finish() {
        super.finish();
        doAsynchronousTask3.cancel();
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
    }

    public void updateUI(){
        messages.clear();
        messages2.clear();
        if(Commons.messages.size() > 0){
            for(int i=0; i<Commons.messages.size(); i++){
                if(Commons.messages.get(i).getCall_code().length() == 0){
                    if(Commons.messages.get(i).getOption().equals("invite"))
                        messages2.add(Commons.messages.get(i));
                    else
                        messages.add(Commons.messages.get(i));
                }else {
                    messages2.add(Commons.messages.get(i));
                }

                if(i == Commons.messages.size() - 1){
                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);
                    adapter.setDatas(filterData(messages));
                    adapter.notifyDataSetChanged();
                    ((ListView)findViewById(R.id.list)).setAdapter(adapter);
                    if(messages2.size() > 0){
                        ((FrameLayout)findViewById(R.id.callNotiFrame)).setVisibility(View.VISIBLE);
                        ((View)findViewById(R.id.layout)).setVisibility(View.VISIBLE);
                        adapter2.setDatas(filterData(messages2));
                        adapter2.notifyDataSetChanged();
                        ((ListView)findViewById(R.id.notiList)).setAdapter(adapter2);
                    }
                }
            }
        }else {
            ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
            adapter.setDatas(filterData(messages));
            adapter.notifyDataSetChanged();
            ((ListView)findViewById(R.id.list)).setAdapter(adapter);

            ((FrameLayout)findViewById(R.id.callNotiFrame)).setVisibility(View.GONE);
            ((View)findViewById(R.id.layout)).setVisibility(View.GONE);
            adapter2.setDatas(filterData(messages2));
            adapter2.notifyDataSetChanged();
            ((ListView)findViewById(R.id.notiList)).setAdapter(adapter2);
        }
        Commons.event = false;
    }

    private ArrayList<Message> filterData(ArrayList<Message> data){
        ArrayList<Integer> dataIdList = new ArrayList<>();
        ArrayList<Message> resultIdsList = new ArrayList<>();
        dataIdList.clear();
        resultIdsList.clear();
        for(int i=0; i<data.size(); i++){
            if(!dataIdList.contains(data.get(i).getIdx())) {
                dataIdList.add(data.get(i).getIdx());
                resultIdsList.add(data.get(i));
            }
            if(i == data.size() - 1){
                return resultIdsList;
            }
        }
        return resultIdsList;
    }

    public void toChat(Message message){
        updateUI();
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        User user = new User();
        user.setIdx(message.getSender_id());
        user.setName(message.getSender_name());
        user.setPhotoUrl(message.getSender_photo());
        user.setPhoneNumber(message.getSender_phone());
        Commons.user = user;
        startActivity(intent);
    }

    public void branch(Message message){
        updateUI();
        if(message.getCall_code().length() > 0) {
            Commons.message = message;
            Intent intent;
            if(message.getOption().equals("nearest") || message.getOption().equals("random"))
                intent = new Intent(getApplicationContext(), VoiceChatViewActivity.class);
            else
                intent = new Intent(getApplicationContext(), VoiceChatViewActivity.class);
            intent.putExtra("call_code", message.getCall_code());
            intent.putExtra("member_id", String.valueOf(message.getSender_id()));
            intent.putExtra("group_name", message.getGroup_name());
            intent.putExtra("network_id", String.valueOf(message.getNetwork_id()));
            startActivity(intent);
        }else if(message.getMessage().equals("I have accepted your invitation.")){
            message.getFirebase().removeValue();
            Intent intent = new Intent(getApplicationContext(), MyNetworkActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
        }else {
            message.getFirebase().removeValue();
            replyToNetworkInvitation(String.valueOf(message.getSender_id()), message.getSender_name(), "", "", String.valueOf(message.getNetwork_id()));
        }
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

    public void searchNotification(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
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

    public void dismissCallNotiFrame(View view){
        ((FrameLayout)findViewById(R.id.callNotiFrame)).setVisibility(View.GONE);
        ((View)findViewById(R.id.layout)).setVisibility(View.GONE);
    }

    public void openNotiHistoryMenu(View v){
        View view = findViewById(R.id.dotsmenuButton);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, view);
        popupMenu.inflate(R.menu.notification_menu);
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = android.widget.PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.

            Log.w("Error====>", "error forcing menu icons to show", e);
            popupMenu.show();
            return;
        }
        popupMenu.show();
    }

    public void callRequests(MenuItem item){
        if(messages2.size() > 0){
            ((FrameLayout)findViewById(R.id.callNotiFrame)).setVisibility(View.VISIBLE);
            ((View)findViewById(R.id.layout)).setVisibility(View.VISIBLE);
        }else showToast("No call request.");
    }

    String notisString = "";

    public String createNotisJsonString()throws JSONException {

        notisString = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        if (Commons.messages.size()>0){
            for(int i=0; i<Commons.messages.size(); i++){
                jsonObj=new JSONObject();
                try {
                    jsonObj.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                    jsonObj.put("message", Commons.messages.get(i).getMessage());
                    jsonObj.put("group_name", Commons.messages.get(i).getGroup_name());
                    jsonObj.put("network_id", String.valueOf(Commons.messages.get(i).getNetwork_id()));
                    jsonObj.put("call_code", Commons.messages.get(i).getCall_code());
                    jsonObj.put("option", Commons.messages.get(i).getOption());
                    jsonObj.put("date_time", Commons.messages.get(i).getDate_time());
                    jsonObj.put("sender_id", String.valueOf(Commons.messages.get(i).getSender_id()));
                    jsonObj.put("sender_name", Commons.messages.get(i).getSender_name());
                    jsonObj.put("sender_phone", Commons.messages.get(i).getSender_phone());
                    jsonObj.put("sender_photo", Commons.messages.get(i).getSender_photo());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArr.put(jsonObj);
            }
            JSONObject questionnaireObj = new JSONObject();
            questionnaireObj.put("notifications", jsonArr);
            notisString = questionnaireObj.toString();
            return notisString;
        }
        return notisString;
    }

    public void notifications(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), NotiHistoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void uploadNewNotifications(String member_id) {

        String url = ReqConst.SERVER_URL + "uploadNewNotifications";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

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
                try {
                    params.put("notifications", createNotisJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put("member_id", member_id);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void replyToNetworkInvitation(String memberId, String name, String groupName, String callCode, String networkId) {

        progressBar.setVisibility(View.VISIBLE);

        Firebase reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I have accepted your invitation.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("group_name", groupName);
        map.put("network_id", networkId);
        map.put("call_code", callCode);
        map.put("option", "invite");
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        map.put("sender_phone", Commons.thisUser.getPhoneNumber());
        map.put("sender_name", Commons.thisUser.getName());
        map.put("sender_photo", Commons.thisUser.getPhotoUrl());
        reference.removeValue();
        reference.push().setValue(map);

        addMemberToNetwork(String.valueOf(Commons.thisUser.getIdx()), name, networkId);
    }

    int networkId = 1;
    String networkName = "";

    public void addMemberToNetwork(String member_id, String name, String network_id) {

        String url = ReqConst.SERVER_URL + "addMemberToNetwork";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseAddMemberToNetworkResponse(response);

            }
        }, new Response.ErrorListener() {
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
                params.put("member_id", member_id);
                params.put("network_id", network_id);
                networkId = Integer.parseInt(network_id);
                networkName = name;
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseAddMemberToNetworkResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);
            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);
            if (success.equals("0")) {
                showToast("Accepted");
                Intent intent = new Intent(getApplicationContext(), NetworkUsersActivity.class);
                intent.putExtra("network_id", networkId);
                intent.putExtra("name", networkName);
                startActivity(intent);
            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}


/*



 */


























