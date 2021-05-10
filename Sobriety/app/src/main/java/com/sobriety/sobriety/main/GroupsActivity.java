package com.sobriety.sobriety.main;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import com.cunoraz.gifview.library.GifView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.GroupListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Group;
import com.sobriety.sobriety.models.Story;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GroupsActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView storyicon, networkicon, homeicon, groupsicon, settingsicon;
    TextView storyText, networkText, homeText, groupsText, settingsText;
    View storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator;
    ImageView searchButton, cancelButton;
    LinearLayout searchBar, createGroupFrame, groupDetailPage;
    EditText ui_edtsearch, inputBox;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<Group> groups = new ArrayList<>();
    GroupListAdapter adapter = new GroupListAdapter(this);

    ProgressBar progressBar;
    public static GroupsActivity groupsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_groups);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title2)).setTypeface(font);
        ((TextView)findViewById(R.id.title3)).setTypeface(font);

        createGroupFrame = (LinearLayout)findViewById(R.id.createGroupFrame);
        groupDetailPage = (LinearLayout)findViewById(R.id.groupDetailPage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dismisstoright);
                createGroupFrame.setAnimation(animation);
                createGroupFrame.setVisibility(View.GONE);
                getMeGroups();
            }
        });

        Toolbar toolbar2 = (Toolbar)findViewById(R.id.toolbar3);
        toolbar2.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dismisstoright);
                groupDetailPage.setAnimation(animation);
                groupDetailPage.setVisibility(View.GONE);
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

        inputBox = (EditText)findViewById(R.id.inputBox);

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
                    adapter.setDatas(groups);
                }else {
                    adapter.filter(text);
                }
            }
        });

        list = (ListView) findViewById(R.id.list);

        resetTabs();

        ((FrameLayout)findViewById(R.id.networkButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyNetworkActivity.class);
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

        ((FrameLayout)findViewById(R.id.storyButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StoryActivity.class);
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

        getMeGroups();
        new Timer().schedule(doAsynchronousTask5, 0, 2000);
    }

    Handler mHandler = new Handler();
    TimerTask doAsynchronousTask5 = new TimerTask() {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(Commons.messages.size() > 0){
                        ((GifView)findViewById(R.id.animNoticon)).setVisibility(View.VISIBLE);
                        ((GifView)findViewById(R.id.animNoticon)).play();
                    }
                    else ((GifView)findViewById(R.id.animNoticon)).setVisibility(View.GONE);
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        doAsynchronousTask5.cancel();
        Log.d("Destroy+++", "Yes");
    }

    @Override
    public void finish() {
        super.finish();
        doAsynchronousTask5.cancel();
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
    }

    public void submit(View view){
        if(inputBox.getText().length() == 0){
            showToast("Enter a group name.");
            return;
        }
        createGroup();
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
        texts[3].setTextColor(getResources().getColor(R.color.colorPrimary));
        indicators[3].setVisibility(View.VISIBLE);
        icons[3].getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    public void searchGroup(View view){
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

    public void openGroupMenu(View v){
        View view = findViewById(R.id.dotsmenuButton);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, view);
        popupMenu.inflate(R.menu.group_menu);
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

    public void createGroup(MenuItem item){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translatefromright);
        createGroupFrame.setAnimation(animation);
        createGroupFrame.setVisibility(View.VISIBLE);
    }

    public void myGroups(MenuItem item){
        Commons.isMyGroup = true;
        Intent intent = new Intent(getApplicationContext(), MyGroupsActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    public void joinGroup(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), JoinGroupActivity.class);
        startActivity(intent);
    }

    private void getTestGroupData(){
        groups.clear();
        for(int i=0; i<20; i++){
            Group group = new Group();
            group.setName("Friends Club");
            ArrayList<User> users = new ArrayList<>();
            User user1 = new User();
            user1.setPhotoUrl("https://101status.in/wp-content/uploads/2018/06/77a5411bccf1d6782ebacc1552bf0476.jpg");
            users.add(user1);
            User user2 = new User();
            user2.setPhotoUrl("https://gdb.voanews.com/AA03776A-1D60-4AFD-8DCD-18E019668CC0_cx0_cy7_cw0_w1023_r1_s.jpg");
            users.add(user2);
            User user3 = new User();
            user3.setPhotoUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRFKPlDg7OKygJm7SxMxIBtY76MmsrKLUb02zLoPzhA4TympVeX5w");
            users.add(user3);
            group.setUsers(users);
            groups.add(group);
        }

        adapter.setDatas(groups);
        adapter.notifyDataSetChanged();
        list.setAdapter(adapter);
    }

    public void createGroup() {

        String url = ReqConst.SERVER_URL + "createGroup";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseCreateGroupResponse(response);

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
                params.put("group_name", inputBox.getText().toString().trim());
                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseCreateGroupResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {

            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                showToast("Successfully created Group.");
                JSONArray groupArr = response.getJSONArray("data");
                Log.d("GROUPS===",groupArr.toString());

                JSONObject jsonGroup = (JSONObject) groupArr.get(0);
                Group group = new Group();
                group.setIdx(jsonGroup.getInt("id"));
                group.setUser_id(jsonGroup.getInt("member_id"));
                group.setName(jsonGroup.getString("name"));
                group.setCode(jsonGroup.getString("code"));
                group.setDateTime(jsonGroup.getString("date_time"));

                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translatefromright);
                groupDetailPage.setAnimation(animation);
                groupDetailPage.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.detailText)).setText("YOUR GROUP - " + group.getName() + " - HAS BEEN ASSIGNED GROUP CODE ...");
                ((TextView)findViewById(R.id.groupCode)).setText(group.getCode());

            } else if(success.equals("1")){
                showToast("Your Group name already exists. Try again with another name.");
            }else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void getMeGroups() {

        String url = ReqConst.SERVER_URL + "getMeGroups";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetGroupsResponse(response);

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
                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseGetGroupsResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESPONSE=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                JSONArray groupArr = response.getJSONArray("data");
                groups.clear();
                for(int i=0; i<groupArr.length(); i++){
                    JSONObject jsonGroup = (JSONObject) groupArr.get(i);
                    Group group = new Group();
                    group.setIdx(jsonGroup.getInt("id"));
                    group.setUser_id(jsonGroup.getInt("member_id"));
                    group.setName(jsonGroup.getString("name"));
                    group.setCode(jsonGroup.getString("code"));
                    group.setDateTime(jsonGroup.getString("date_time"));

                    ArrayList<User> users = new ArrayList<>();
                    JSONArray userInfo = jsonGroup.getJSONArray("users");
                    for(int j=0; j<userInfo.length(); j++){
                        JSONObject object = (JSONObject) userInfo.get(j);
                        User user = new User();
                        user.setIdx(object.getInt("id"));
                        user.setName(object.getString("name"));
                        user.setUsername(object.getString("username"));
                        user.setGender(object.getString("gender"));
                        user.setPhoneNumber(object.getString("phone_number"));
                        user.setPhotoUrl(object.getString("photo_url"));
                        user.setCleanDate(object.getString("clean_date"));
                        users.add(user);
                    }
                    group.setUsers(users);
//                    if(group.getUser_id() != Commons.thisUser.getIdx())
                        groups.add(group);
                }

                if(groups.isEmpty()){
                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                }
                adapter.setDatas(groups);
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

    @Override
    public void onBackPressed() {
        if(((LinearLayout)findViewById(R.id.createGroupFrame)).getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
            ((LinearLayout)findViewById(R.id.createGroupFrame)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.createGroupFrame)).setVisibility(View.GONE);
        }else if(((LinearLayout)findViewById(R.id.groupDetailPage)).getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
            ((LinearLayout)findViewById(R.id.groupDetailPage)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.groupDetailPage)).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}





























