package com.sobriety.sobriety.main;

import android.app.Activity;
import android.graphics.Typeface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.firebase.client.Firebase;
import com.marozzi.roundbutton.RoundButton;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.MyGroupUserListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyGroupUsersActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch, inputBox;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<User> users = new ArrayList<>();
    MyGroupUserListAdapter adapter = new MyGroupUserListAdapter(this);
    String usersString = "";

    ProgressBar progressBar;
    String groupId = "1";
    public static String groupName = "";
    Firebase reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_my_group_users);

        Firebase.setAndroidContext(this);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        Commons.selectedUserIds.clear();

        groupName = getIntent().getStringExtra("group_name");
        groupId = getIntent().getStringExtra("group_id");
        reference = new Firebase(ReqConst.FIREBASE_URL + "gmsg_user");

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title2)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        ((RoundButton)findViewById(R.id.submitButton)).setTypeface(font);

        inputBox = (EditText)findViewById(R.id.inputBox);

        toolbar = (Toolbar)findViewById(R.id.toolbar2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dismisstoright);
                ((LinearLayout)findViewById(R.id.textGroupFrame)).setAnimation(animation);
                ((LinearLayout)findViewById(R.id.textGroupFrame)).setVisibility(View.GONE);
                inputBox.setText("");
                hideKeyboard(MyGroupUsersActivity.this);
            }
        });

        title = (TextView)findViewById(R.id.title);
        title.setText(groupName);

        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(inputBox.getText().toString().trim().length() > 0) ((RoundButton)findViewById(R.id.submitButton)).setVisibility(View.VISIBLE);
                else ((RoundButton)findViewById(R.id.submitButton)).setVisibility(View.GONE);
            }
        });

        ((RoundButton)findViewById(R.id.submitButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RoundButton)findViewById(R.id.submitButton)).startAnimation();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitTextToMembers();
                    }
                }, 1200);
            }
        });

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
                    adapter.setDatas(users);
                }else {
                    adapter.filter(text);
                }
            }
        });

        list = (ListView) findViewById(R.id.list);

//        getTestUserData();
        getJoinedGroupMembers(groupId);
    }

    @JsonProperty("group_name")
    private void submitTextToMembers() {
        for(int i=0; i<users.size(); i++){
            reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + String.valueOf(users.get(i).getIdx()) + "/" + String.valueOf(Commons.thisUser.getIdx()));
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", inputBox.getText().toString().trim());
            map.put("time", String.valueOf(new Date().getTime()));
            map.put("group_name", groupName);
            map.put("network_id", String.valueOf(Commons.myNetworkId));
            map.put("call_code", "");
            map.put("option", "");
            map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
            map.put("sender_phone", Commons.thisUser.getPhoneNumber());
            map.put("sender_name", Commons.thisUser.getName());
            map.put("sender_photo", Commons.thisUser.getPhotoUrl());
//            reference.removeValue();
            reference.push().setValue(map);
            if(i == users.size()-1){
                ((RoundButton)findViewById(R.id.submitButton)).revertAnimation();
                inputBox.setText("");
                showToast("Sent message.");
                break;
            }
        }
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

    public void searchGroupUser(View view){
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public void openMyGroupMenu(View v){
        View view = findViewById(R.id.dotsmenuButton);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, view);
        popupMenu.inflate(R.menu.my_group_menu);
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

    public void deleteMember(MenuItem item){
        if(Commons.selectedUserIds.size() > 0){
            ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.memberDeleteAlert)).setVisibility(View.VISIBLE);
        }else showToast("Select member.");
    }

    public void deleteSelectedUsers(View view){
        ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.memberDeleteAlert)).setVisibility(View.GONE);
        deleteSelectedMembers(groupId);
    }

    public void groupText(MenuItem item){
        if(Commons.selectedUserIds.size() > 0){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translatefromright);
            ((LinearLayout)findViewById(R.id.textGroupFrame)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.textGroupFrame)).setVisibility(View.VISIBLE);
        }else showToast("Select member.");
    }

    public void closeMemberDeleteAlert(View view){
        ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.memberDeleteAlert)).setVisibility(View.GONE);
    }

    private void getTestUserData(){
        for(int i=0; i<20; i++){
            User user = new User();
            user.setName("Alianna J.");
            user.setPhotoUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQoeNpiq6CNM-p4NAVsiy4sstTwnjeh0KOVsoVahlXaTvrP-tZq");
            users.add(user);

            user = new User();
            user.setName("Michael W.");
            user.setPhotoUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQC4oxwkmGPXdDy77xG7Qhn8-H-7_yaTQTmKdcIUQLT-f2LFnsm");
            users.add(user);

            user = new User();
            user.setName("Maria R.");
            user.setPhotoUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSOJTP4srUuwaD7P_9jAKtKVLroL6koyRWEd2KZ0-Hm8pnWAtXW");
            users.add(user);
        }

        adapter.setDatas(users);
        adapter.notifyDataSetChanged();
        list.setAdapter(adapter);
    }

    public void getJoinedGroupMembers(String groupId) {

        String url = ReqConst.SERVER_URL + "getJoinedGroupMembers";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetGroupMembersResponse(response);

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
                params.put("group_id", groupId);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseGetGroupMembersResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);
            if (success.equals("0")) {
                JSONArray userInfo = response.getJSONArray("data");
                users.clear();
                for(int i=0; i<userInfo.length(); i++){
                    JSONObject object = (JSONObject) userInfo.get(i);
                    User user = new User();
                    user.setIdx(object.getInt("id"));
                    user.setName(rename(object.getString("name")));
                    user.setUsername(object.getString("username"));
                    user.setGender(object.getString("gender"));
                    user.setPhoneNumber(object.getString("phone_number"));
                    user.setPhotoUrl(object.getString("photo_url"));
                    user.setCleanDate(object.getString("clean_date"));
                    if(user.getIdx() == Commons.thisUser.getIdx())
                        continue;
                    users.add(user);
                }

                if(users.isEmpty()){
                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                }
                adapter.setDatas(users);
                adapter.notifyDataSetChanged();
                list.setAdapter(adapter);

            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void deleteSelectedMembers(String groupId) {

        String url = ReqConst.SERVER_URL + "deleteSelectedMembers";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseDeleteSelectedMembersResponse(response);

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
                try {
                    params.put("members", createUsersJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put("group_id", groupId);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseDeleteSelectedMembersResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);
            if (success.equals("0")) {
                showToast("Deleted");
                getJoinedGroupMembers(groupId);
            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public String createUsersJsonString()throws JSONException {

        usersString = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        if (Commons.selectedUserIds.size()>0){
            for(int i=0; i<Commons.selectedUserIds.size(); i++){
                jsonObj=new JSONObject();
                try {
                    jsonObj.put("user_id", String.valueOf(Commons.selectedUserIds.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArr.put(jsonObj);
            }
            JSONObject userObj = new JSONObject();
            userObj.put("userIds", jsonArr);
            usersString = userObj.toString();
            return usersString;
        }
        return usersString;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}













































