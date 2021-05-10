package com.sobriety.sobriety.main;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.firebase.client.Firebase;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.NotiHistoryAdapter;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NotiHistoryActivity extends AppCompatActivity implements SwipyRefreshLayout.OnRefreshListener {

    ImageView searchButton, cancelButton;
    LinearLayout searchBar;
    EditText ui_edtsearch, inputBox;
    TextView title;
    SwipyRefreshLayout ui_RefreshLayout;
    ListView list;
    ArrayList<Message> messages = new ArrayList<>();
    NotiHistoryAdapter adapter = new NotiHistoryAdapter(this);
    String notisString = "";

    ProgressBar progressBar;
    String groupId = "1";
    public static String groupName = "";
    Firebase reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti_history);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        Commons.selectedNotiIds.clear();

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        title = (TextView)findViewById(R.id.title);

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

        getNotifications(String.valueOf(Commons.thisUser.getIdx()));
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

    public void toTrash(View view){
        if(Commons.selectedNotiIds.size() > 0){
            ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.VISIBLE);
            ((LinearLayout)findViewById(R.id.notiDeleteAlert)).setVisibility(View.VISIBLE);
        }else showToast("Select message.");
    }

    public void deleteSelectedNotis(View view){
        ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.notiDeleteAlert)).setVisibility(View.GONE);
        deleteSelectedNotis();
    }

    public void closeNotiDeleteAlert(View view){
        ((FrameLayout)findViewById(R.id.layout)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.notiDeleteAlert)).setVisibility(View.GONE);
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

    public String createNotisJsonString()throws JSONException {

        notisString = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        if (Commons.selectedNotiIds.size()>0){
            for(int i=0; i<Commons.selectedNotiIds.size(); i++){
                jsonObj=new JSONObject();
                try {
                    jsonObj.put("message_id", String.valueOf(Commons.selectedNotiIds.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArr.put(jsonObj);
            }
            JSONObject questionnaireObj = new JSONObject();
            questionnaireObj.put("messageIds", jsonArr);
            notisString = questionnaireObj.toString();
            return notisString;
        }
        return notisString;
    }

    public void deleteSelectedNotis() {

        String url = ReqConst.SERVER_URL + "deleteSelectedNotis";
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
                    params.put("notifications", createNotisJsonString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                Commons.selectedNotiIds.clear();
                getNotifications(String.valueOf(Commons.thisUser.getIdx()));
            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
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

    public void getNotifications(String memberId) {

        String url = ReqConst.SERVER_URL + "getNotifications";
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
                params.put("member_id", memberId);
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
                JSONArray msgInfo = response.getJSONArray("data");
                messages.clear();
                for(int i=0; i<msgInfo.length(); i++){
                    JSONObject object = (JSONObject) msgInfo.get(i);
                    Message message = new Message();
                    message.setIdx(object.getInt("id"));
                    message.setMessage(object.getString("message"));
                    message.setGroup_name(object.getString("group_name"));
                    message.setCall_code(object.getString("call_code"));
                    message.setOption(object.getString("opt"));
                    message.setDate_time(object.getString("date_time"));
                    message.setSender_id(object.getInt("sender_id"));
                    message.setSender_photo(object.getString("sender_photo"));
                    message.setSender_phone(object.getString("sender_phone"));
                    message.setSender_name(object.getString("sender_name"));

                    messages.add(message);
                }

                if(messages.isEmpty()){
                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                }
                adapter.setDatas(messages);
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
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }
}












































