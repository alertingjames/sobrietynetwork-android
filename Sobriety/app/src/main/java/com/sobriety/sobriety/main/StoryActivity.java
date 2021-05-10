package com.sobriety.sobriety.main;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.cunoraz.gifview.library.GifView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.StoryListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Story;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;

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

public class StoryActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView storyicon, networkicon, homeicon, groupsicon, settingsicon;
    TextView storyText, networkText, homeText, groupsText, settingsText;
    View storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator;
    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    StoryListAdapter adapter = new StoryListAdapter(this);
    ArrayList<Story> stories = new ArrayList<>();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_story);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

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
                    adapter.setDatas(stories);
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

        getStories();
        new Timer().schedule(doAsynchronousTask4, 0, 2000);
    }

    Handler mHandler = new Handler();
    TimerTask doAsynchronousTask4 = new TimerTask() {

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
        doAsynchronousTask4.cancel();
        Log.d("Destroy+++", "Yes");
    }

    @Override
    public void finish() {
        super.finish();
        doAsynchronousTask4.cancel();
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
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
        texts[0].setTextColor(getResources().getColor(R.color.colorPrimary));
        indicators[0].setVisibility(View.VISIBLE);
        icons[0].getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    public void searchStory(View view){
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

    public void openStoryMenu(View v){
        View view = findViewById(R.id.dotsmenuButton);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, view);
        popupMenu.inflate(R.menu.story_menu);
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

    public void addStory(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), AddStoryActivity.class);
        startActivity(intent);
    }

    public void myStories(MenuItem item){
        Commons.isMyStory = true;
        Intent intent = new Intent(getApplicationContext(), MyStoryActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private void getTestData(){
        for(int i=0; i<20; i++){
            Story story = new Story();
            story.setTitle("Big Buck Bunny");
            story.setUserName("Linda C.");
            story.setDateTime("8/19/2018 9:29 AM");
            story.setUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            story.setThumbnail("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT_BYnvMtN4fmx6wrzu0-4PCoKe1okPtIvwq-HgAOAdngcMEs3k");
            stories.add(story);

            story = new Story();
            story.setTitle("Urban Coyotes");
            story.setUserName("Massi W.");
            story.setDateTime("8/12/2018 4:45 PM");
            story.setUrl("https://www.sample-videos.com/audio/mp3/crowd-cheering.mp3");
            story.setThumbnail("");
            stories.add(story);
        }

        adapter.setDatas(stories);
        adapter.notifyDataSetChanged();
        list.setAdapter(adapter);
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

    private void getStories(){
        String url = ReqConst.SERVER_URL + "getStories";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetStoriesResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressBar.setVisibility(View.GONE);
                showToast("Server issue");

            }
        }) {

        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);
    }

    private void parseGetStoriesResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                JSONArray storyArr = response.getJSONArray("data");
                Log.d("STORIES===",storyArr.toString());

                for (int i = 0; i < storyArr.length(); i++) {
                    JSONObject jsonStory = (JSONObject) storyArr.get(i);

                    Story story = new Story();
                    story.setIdx(jsonStory.getInt("id"));
                    story.setUser_id(jsonStory.getInt("member_id"));
                    story.setUserName(jsonStory.getString("member_name"));
                    story.setDateTime(jsonStory.getString("date_time"));
                    story.setTitle(jsonStory.getString("title"));
                    story.setThumbnail(jsonStory.getString("thumbnail_url"));
                    story.setUrl(jsonStory.getString("url"));

                    // except me
//                    if (story.getUser_id() == Commons.thisUser.getIdx())
//                        continue;
                    stories.add(story);
                }
                if(stories.isEmpty()){
                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                }
                adapter.setDatas(stories);
                adapter.notifyDataSetChanged();
                list.setAdapter(adapter);
            }else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}



























