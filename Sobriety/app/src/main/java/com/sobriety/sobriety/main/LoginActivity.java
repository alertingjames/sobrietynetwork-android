package com.sobriety.sobriety.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.hbb20.CountryCodePicker;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText inputBox;
    FrameLayout splashLayout;
    ProgressBar progressBar, progressBar2;

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INSTALL_PACKAGES,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.SET_TIME,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            android.Manifest.permission.WAKE_LOCK,
//            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.LOCATION_HARDWARE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        checkAllPermission();

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        inputBox = (EditText)findViewById(R.id.inputBox);
        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    inputBox.performClick();
                    if(inputBox.getText().toString().trim().length() == 0){
                        showToast("Please enter your username.");
                    }else {
                        String cc = ((CountryCodePicker)findViewById(R.id.cc)).getSelectedCountryCode();
                        login(inputBox.getText().toString().trim());
                    }
                    return true;
                }
                return false;
            }
        });

        splashLayout = ((FrameLayout)findViewById(R.id.splashLayout));
        splashLayout.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar)findViewById(R.id.progressBar2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashLayout.setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
                splashLayout.setAnimation(animation);
                String code = Preference.getInstance().getValue(getApplicationContext(), PrefConst.PREF_CODE, "");
                if(code.length() > 0){
                    login(code);
                }else {
                    Commons.thisUser = null;
                }
            }
        }, 1500);

    }

    public void login(View view){
        if(inputBox.getText().toString().trim().length() == 0){
            showToast("Please enter your username.");
        }else {
            String cc = ((CountryCodePicker)findViewById(R.id.cc)).getSelectedCountryCode();
            login(inputBox.getText().toString().trim());
        }
    }

    private void login(String username) {
        String url = ReqConst.SERVER_URL + "usernameLogin";
        progressBar2.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseLoginResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressBar2.setVisibility(View.GONE);
                showToast("Server issue");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_name", username);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);
    }

    public void parseLoginResponse(String json) {
        progressBar2.setVisibility(View.GONE);
        try {

            JSONObject response = new JSONObject(json);

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
//                String code = object.getString("code");
                Commons.thisUser = user;
                Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_CODE, user.getUsername());
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else if(success.equals("1")){
                showToast("No exists any user that has such username.");
            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
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

    public void gotoRegister(View view){
        Intent intent = new Intent(getApplicationContext(), PrefRegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public void checkAllPermission() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (hasPermissions(this, PERMISSIONS)){

        }else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}















































