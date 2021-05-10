package com.sobriety.sobriety.main;

import android.graphics.Typeface;
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
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.MyGroupUserListAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Group;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GroupUsersActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch, inputBox;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<User> users = new ArrayList<>();
    MyGroupUserListAdapter adapter = new MyGroupUserListAdapter(this);

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_group_users);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        String groupName = getIntent().getStringExtra("group_name");
        String groupId = getIntent().getStringExtra("group_id");

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        title = (TextView)findViewById(R.id.title);
        title.setText(groupName);

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

        getJoinedGroupMembers(groupId);
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
                params.put("group_id", groupId);
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
//                    if(user.getIdx() == Commons.thisUser.getIdx())
//                        continue;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}












































