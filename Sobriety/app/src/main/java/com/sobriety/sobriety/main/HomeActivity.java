package com.sobriety.sobriety.main;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.adapters.CallNotiListAdapter;
import com.sobriety.sobriety.classes.ColorHelper;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.Group;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;
import com.sobriety.sobriety.utils.ColorUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class HomeActivity extends AppCompatActivity {

    ImageView storyicon, networkicon, homeicon, groupsicon, settingsicon;
    TextView storyText, networkText, homeText, groupsText, settingsText;
    View storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator;
    TextView title;
    Timer mTimer = new Timer();
    Handler mHandler = new Handler();
    Location location = null;
    CircleImageView photo;
    TextView countBox;
    FrameLayout notiButton;
    ImageView whiteNoticon;
    GifView animNoticon;
    ProgressBar progressBar;
    Map<Integer, Object> notiMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        notiMap.clear();

        String state = Preference.getInstance().getValue(getApplicationContext(), PrefConst.PREF_STATE, "available");
        if(state.equals("available")){
            Commons.available = true;
            ((ImageView)findViewById(R.id.disturb)).setVisibility(View.GONE);
        }
        else {
            Commons.available = false;
            ((ImageView)findViewById(R.id.disturb)).setVisibility(View.VISIBLE);
        }

        photo = (CircleImageView)findViewById(R.id.photo);
        Picasso.with(getApplicationContext())
                .load(Commons.thisUser.getPhotoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(photo);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Commons.available){
                    Commons.available = false;
                    ((ImageView)findViewById(R.id.disturb)).setVisibility(View.VISIBLE);
                    Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_STATE, "disturbed");
                    onRestart();
                }else {
                    Commons.available = true;
                    ((ImageView)findViewById(R.id.disturb)).setVisibility(View.GONE);
                    Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_STATE, "available");
                    onRestart();
                }
            }
        });

        if(Commons.thisUser.getPhoneNumber().equals(Constants.ADMIN_PHONENUMBER)) {
            ((TextView) findViewById(R.id.buttonText)).setText("Verify Payment");
            ((TextView) findViewById(R.id.caption)).setVisibility(View.INVISIBLE);
        }

        notiButton = (FrameLayout)findViewById(R.id.notiButton);
        animNoticon = (GifView) findViewById(R.id.animNoticon);
        animNoticon.play();
        whiteNoticon = (ImageView)findViewById(R.id.whiteNoticon);
        notiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
        ((TextView)findViewById(R.id.nearestText)).setTypeface(font);
        ((TextView)findViewById(R.id.randomText)).setTypeface(font);

        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "ag-futura-58e274b5588ad.ttf");
        ((TextView)findViewById(R.id.name)).setTypeface(font);
        ((TextView)findViewById(R.id.name)).setText(rename(Commons.thisUser.getName()));

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

        resetTabs();

        getMyNetworkID();

        ((FrameLayout)findViewById(R.id.networkButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyNetworkActivity.class);
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

        String token = Preference.getInstance().getValue(getApplicationContext(), PrefConst.PREF_TOKEN, "");
        if(token.length() > 0){
            uploadNewToken(token);
        }

        Log.d("Messages+++", String.valueOf(Commons.messages.size()));

        mTimer.schedule(doAsynchronousTask, 0, 2000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNotifications(String.valueOf(Commons.thisUser.getIdx()));
            }
        }).start();
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

    int cnt = 0;

    TimerTask doAsynchronousTask = new TimerTask() {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    cnt++;
                    if(Commons.messages.size() > 0){
                        whiteNoticon.setVisibility(View.GONE);
                        animNoticon.setVisibility(View.VISIBLE);
                        animNoticon.play();
                    }else {
                        whiteNoticon.setVisibility(View.VISIBLE);
                        animNoticon.setVisibility(View.GONE);
                    }
                    if(cnt >= 15){
                        cnt = 0;
                        checkForLocationPermission();
                    }
                    if(cnt % 10 == 0){
                        Log.i("notifications", "+++" + Commons.messages.size() + "+++event+++" + String.valueOf(Commons.event));
                    }
                }
            });
        }
    };

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
        texts[2].setTextColor(getResources().getColor(R.color.colorPrimary));
        indicators[2].setVisibility(View.VISIBLE);
        icons[2].getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    public void gotoDonate(View view){
        if(Commons.thisUser.getPhoneNumber().equals(Constants.ADMIN_PHONENUMBER)) {
            newPaymentAccount();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), DonateActivity.class);
        startActivity(intent);
    }

    public void nearestCall(View view){
        Intent intent = new Intent(getApplicationContext(), NearestAddictCallingActivity.class);
        startActivity(intent);
    }

    public void randomCall(View view){
        Intent intent = new Intent(getApplicationContext(), RandomAddictCallingActivity.class);
        startActivity(intent);
    }

    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null) {
                Log.d("MyLoc+++", String.valueOf(location));
                refreshLocation(location);
            }
        }
    }

    public void refreshLocation(final Location location) {

        String url = ReqConst.SERVER_URL + "refreshLocation";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());
                parseRegisterResponse(response);
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

                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("lat", String.valueOf(location.getLatitude()));
                params.put("lng", String.valueOf(location.getLongitude()));

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseRegisterResponse(String json) {

        try {
            JSONObject response = new JSONObject(json);

            int result_code = response.getInt("result_code");
            Log.d("result===",String.valueOf(result_code));

            if (result_code == 0) {

            }
            else {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getNotifications(final String me_id) {
        Commons.messages.clear();
        final Firebase ref = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + me_id);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Firebase childRef = ref.child(dataSnapshot.getKey());
                childRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String msg = "", dateTime = "", groupName = "", senderPhone = "", senderName = "", senderPhoto = "", callCode = "", option = "";
                        int senderId = 1, networkId = 0;
                        try{
                            Map map = dataSnapshot.getValue(Map.class);
                            msg = map.get("message").toString();
                            groupName = map.get("group_name").toString();
                            networkId = Integer.parseInt(map.get("network_id").toString());
                            callCode = map.get("call_code").toString();
                            option = map.get("option").toString();
                            senderPhoto = map.get("sender_photo").toString();
                            senderPhone = map.get("sender_phone").toString();
                            senderId = Integer.parseInt(map.get("sender_id").toString());
                            senderName = rename(map.get("sender_name").toString());
                            dateTime = map.get("time").toString();

                            Message message = new Message();
                            message.setMessage(msg);
                            message.setGroup_name(groupName);
                            message.setNetwork_id(networkId);
                            message.setCall_code(callCode);
                            message.setOption(option);
                            message.setDate_time(dateTime);
                            message.setSender_id(senderId);
                            message.setSender_photo(senderPhoto);
                            message.setSender_phone(senderPhone);
                            message.setSender_name(senderName);
                            message.setFirebase(childRef.child(dataSnapshot.getKey()));
                            message.setKey(dataSnapshot.getKey());

                            if(Commons.messages.size() == 0) {
                                Commons.messages.add(message);
                                notiMap.put(senderId, message);
                                ShortcutBadger.applyCount(getApplicationContext(), Commons.messages.size());
                                whiteNoticon.setVisibility(View.GONE);
                                animNoticon.setVisibility(View.VISIBLE);
                                animNoticon.play();
                                shownot(senderPhoto, senderName, msg);
                            }

                            for(int i=0; i < Commons.messages.size(); i++){
                                if(Commons.messages.get(i).getDate_time().equals(message.getDate_time()) && Commons.messages.get(i).getSender_id() == message.getSender_id())
                                    break;
                                else if(i == Commons.messages.size()-1){
                                    if(!notiMap.containsKey(senderId) && Commons.messages.get(i).getSender_id() != message.getSender_id()){
                                        Commons.messages.add(0, message);
                                        notiMap.put(senderId, message);
                                        ShortcutBadger.applyCount(getApplicationContext(), Commons.messages.size());
                                        shownot(senderPhoto, senderName, msg);
                                    }
                                }
                            }

                            Commons.event = true;
                            Log.d("new noti+++", "yes");

                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }catch (FirebaseException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d("Key===>", dataSnapshot.getKey());
                        String msg = "", dateTime = "", groupName = "", senderPhone = "", senderName = "", senderPhoto = "", callCode = "";
                        int senderId = 1, networkId = 0;
                        try{
                            Map map = dataSnapshot.getValue(Map.class);
                            msg = map.get("message").toString();
                            groupName = map.get("group_name").toString();
                            networkId = Integer.parseInt(map.get("network_id").toString());
                            callCode = map.get("call_code").toString();
                            senderPhoto = map.get("sender_photo").toString();
                            senderPhone = map.get("sender_phone").toString();
                            senderId = Integer.parseInt(map.get("sender_id").toString());
                            senderName = rename(map.get("sender_name").toString());
                            dateTime = map.get("time").toString();

                            Message message = new Message();
                            message.setMessage(msg);
                            message.setGroup_name(groupName);
                            message.setNetwork_id(networkId);
                            message.setCall_code(callCode);
                            message.setDate_time(dateTime);
                            message.setSender_id(senderId);
                            message.setSender_photo(senderPhoto);
                            message.setSender_phone(senderPhone);
                            message.setSender_name(senderName);
                            message.setFirebase(childRef.child(dataSnapshot.getKey()));
                            message.setKey(dataSnapshot.getKey());

                            Commons.messages.remove((Message) notiMap.get(senderId));
                            ShortcutBadger.applyCount(getApplicationContext(), Commons.messages.size());
                            if(Commons.messages.size() <= 0){
                                Commons.notificationManager.cancelAll();
                                ShortcutBadger.removeCount(getApplicationContext());
                                whiteNoticon.setVisibility(View.VISIBLE);
                                animNoticon.setVisibility(View.GONE);
                            }

                            Commons.event = true;
                            Log.d("new noti---", "yes");

                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }catch (FirebaseException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

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

    public void shownot(String senderPhoto, String senderName, String message) {
        Notification.Builder n;
        NotificationManager notification_manager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String chanel_id = "3000";
            CharSequence name = "Channel Name";
            String description = "Chanel Description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            notification_manager.createNotificationChannel(mChannel);
            n = new Notification.Builder(this, chanel_id);
        } else {
            n = new Notification.Builder(this);
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] v = {500,1000};
        Bitmap bitmapPhoto = null;

        if(senderPhoto.length() > 0){
            try {
                bitmapPhoto = BitmapFactory.decodeStream((InputStream) new URL(senderPhoto).getContent());
            } catch (IOException e) {
                e.printStackTrace();
                bitmapPhoto = BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.photo);
            }
        }

        Intent intent = new Intent(this, NotificationActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        PendingIntent pIntent = PendingIntent.getActivity(this, 113, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        n.setContentTitle(senderName)
                .setFullScreenIntent(pIntent,true)
                .setContentText(message)
                .setSmallIcon(R.drawable.noti_animation).setLargeIcon(bitmapPhoto)
//                .setContentIntent(pIntent)
                .setSound(uri)
                .setVibrate(v)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true);
        notification_manager.notify(0, n.build());
        Commons.notificationManager = notification_manager;
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

    public void getMyNetworkID() {

        String url = ReqConst.SERVER_URL + "getMeNetworkID";
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
            JSONObject response = new JSONObject(json);   Log.d("RESP=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {

                Commons.myNetworkId = response.getInt("network_id");

            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void newPaymentAccount() {

        String url = ReqConst.SERVER_URL + "newPaymentAccount";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseNewPaymentAccountResponse(response);

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

    public void parseNewPaymentAccountResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESP=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                Intent intent = new Intent(getApplicationContext(), RegisterPaymentInfoActivity.class);
                startActivity(intent);
            }else if (success.equals("1")) {
                showToast("Your payment has already been verified.");
            }else if (success.equals("2")) {
                showToast("Stripe account error!");
                Log.d("Error===>", response.getString("error"));
            }else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void uploadNewToken(String token) {

        String url = ReqConst.SERVER_URL + "uploadfcmtoken";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseUploadTokenResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                showToast("Server issue");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("fcm_token", token);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseUploadTokenResponse(String json) {
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESP=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {

            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

























