package com.sobriety.sobriety.main;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.firebase.client.Firebase;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.NetworkUserListAdapter;
import com.sobriety.sobriety.adapters.UserListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NetworkUsersActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView storyicon, networkicon, homeicon, groupsicon, settingsicon;
    TextView storyText, networkText, homeText, groupsText, settingsText;
    View storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator;
    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<User> users = new ArrayList<>();
    UserListAdapter adapter = new UserListAdapter(this);

    ProgressBar progressBar;
    int networkId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_network_users);

        networkId = getIntent().getIntExtra("network_id", 1);
        String nName = getIntent().getStringExtra("name");

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title)).setText(nName);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        storyicon = (ImageView)findViewById(R.id.storyicon);
        networkicon = (ImageView)findViewById(R.id.networkicon);
        homeicon = (ImageView)findViewById(R.id.homeicon);
        groupsicon = (ImageView)findViewById(R.id.groupsicon);
        settingsicon = (ImageView)findViewById(R.id.settingsicon);

        storyText = (TextView) findViewById(R.id.storyText);
        networkText = (TextView) findViewById(R.id.networkText);
        homeText = (TextView)findViewById(R.id.homeText);
        groupsText = (TextView)findViewById(R.id.groupsText);
        settingsText = (TextView)findViewById(R.id.settingsText);

        storyIndicator = (View) findViewById(R.id.indicator_story);
        networkIndicator = (View) findViewById(R.id.indicator_network);
        homeIndicator = (View) findViewById(R.id.indicator_home);
        groupsIndicator = (View) findViewById(R.id.indicator_group);
        settingsIndicator = (View) findViewById(R.id.indicator_settings);

        title=(TextView)findViewById(R.id.title);

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

        resetTabs();

        ((FrameLayout)findViewById(R.id.storyButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StoryActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
            }
        });

        ((FrameLayout)findViewById(R.id.homeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
            }
        });

        ((FrameLayout)findViewById(R.id.groupsButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GroupsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
            }
        });

        ((FrameLayout)findViewById(R.id.settingsButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
            }
        });

        getNetworkMembers(String.valueOf(networkId));
    }

    private void resetTabs(){
        ImageView[] icons = new ImageView[]{storyicon, networkicon, homeicon, groupsicon, settingsicon};
        TextView[] texts = new TextView[]{storyText, networkText, homeText, groupsText, settingsText};
        View[] indicators = new View[]{storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator};
        Drawable[] drawables = new Drawable[]{
                getResources().getDrawable(R.drawable.ic_hear_story).mutate(),
                getResources().getDrawable(R.drawable.ic_view_my_network).mutate(),
                getResources().getDrawable(R.drawable.ic_home).mutate(),
                getResources().getDrawable(R.drawable.ic_group).mutate(),
                getResources().getDrawable(R.drawable.ic_preferences).mutate()
        };
        for(int i=0;i<drawables.length; i++) {
            drawables[i].setColorFilter(getResources().getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP);
            icons[i].setImageDrawable(drawables[i]);
        }
        for(int i=0;i<texts.length; i++) {
            texts[i].setTextColor(getResources().getColor(R.color.gray));
        }
        texts[1].setTextColor(getResources().getColor(R.color.colorPrimary));
        indicators[1].setVisibility(View.VISIBLE);
        icons[1].getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    public void searchNetwork(View view){
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

    private void getTestNetworkUserData(){
        for(int i=0; i<20; i++){
            User user = new User();
            user.setName("Massi W.");
            user.setPhotoUrl("https://101status.in/wp-content/uploads/2018/06/77a5411bccf1d6782ebacc1552bf0476.jpg");
            users.add(user);

            user = new User();
            user.setName("Serena C.");  //
            user.setPhotoUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRFKPlDg7OKygJm7SxMxIBtY76MmsrKLUb02zLoPzhA4TympVeX5w");
            users.add(user);

            user = new User();
            user.setName("David T.");
            user.setPhotoUrl("https://gdb.voanews.com/AA03776A-1D60-4AFD-8DCD-18E019668CC0_cx0_cy7_cw0_w1023_r1_s.jpg");
            users.add(user);
        }

        adapter.setDatas(users);
        adapter.notifyDataSetChanged();
        list.setAdapter(adapter);
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

    public void getNetworkMembers(String networkId) {

        String url = ReqConst.SERVER_URL + "getNetworkMembers";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetNetworkMembersResponse(response);

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
                params.put("network_id", networkId);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseGetNetworkMembersResponse(String json) {
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

    @JsonProperty("group_name")
    public void sendInvitationNoti(String memberId, String groupName, String callCode) {
        Firebase reference = new Firebase(ReqConst.FIREBASE_URL + "reqcall/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I invite you to our Network.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("group_name", groupName);
        map.put("network_id", String.valueOf(Commons.myNetworkId));
        map.put("call_code", callCode);
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        map.put("sender_phone", Commons.thisUser.getPhoneNumber());
        map.put("sender_name", Commons.thisUser.getName());
        map.put("sender_photo", Commons.thisUser.getPhotoUrl());
        reference.removeValue();
        reference.push().setValue(map);

        showToast("Sent message for invitation.");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}





























