package com.sobriety.sobriety.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.eyalbira.loadingdots.LoadingDots;
import com.hbb20.CountryCodePicker;
import com.marozzi.roundbutton.RoundButton;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.sobriety.sobriety.main.MyGroupUsersActivity.hideKeyboard;

public class RegisterActivity extends AppCompatActivity {

    EditText inputBox, codeBox;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        inputBox = (EditText)findViewById(R.id.inputBox);
        codeBox = (EditText)findViewById(R.id.codeBox);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    inputBox.performClick();
                    if(inputBox.getText().length() == 0){
                        showToast("Please enter your phone number.");
                    }else
                        loadPhoneNumber();
                    return true;
                }
                return false;
            }
        });

        codeBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    codeBox.performClick();
                    if(inputBox.getText().toString().trim().length() > 0 && codeBox.getText().toString().trim().length()> 0){
                        submitVerificationCode();
                    }else if(inputBox.getText().toString().trim().length() == 0){
                        showToast("Please enter your phone number.");
                    }else if(codeBox.getText().toString().trim().length() == 0){
                        showToast("Please enter your verification code.");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void requestCode(View view){
        if(inputBox.getText().length() > 0 && codeBox.getText().length()> 0){
            submitVerificationCode();
        }else if(inputBox.getText().length() == 0){
            showToast("Please enter your phone number.");
        }else if(codeBox.getText().length() == 0){
            showToast("Please enter your verification code.");
        }
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            showToast("We have sent a verification code to your phone.");
        } catch (Exception ex) {
            showToast(ex.getMessage());
            ex.printStackTrace();
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

    public void loadPhoneNumber() {

        String url = ReqConst.SERVER_URL + "loadPhoneNumber";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseRestUrlsResponse(response);

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
                String cc = ((CountryCodePicker)findViewById(R.id.cc)).getSelectedCountryCode();
                params.put("phone_number", cc + inputBox.getText().toString().trim());
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseRestUrlsResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {

            JSONObject response = new JSONObject(json);

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                showToast("We have sent your code to your phone. Please check it.");
            }else if(success.equals("1")){
                showToast("Sorry, we can't identify your phone number. Try again with another one.");
            }else {
                showToast("Server issue");
            }

            hideKeyboard(RegisterActivity.this);

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void submitVerificationCode() {

        String url = ReqConst.SERVER_URL + "submitVerificationCode";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseSubmitVerificationCodeResponse(response);

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
                params.put("code", codeBox.getText().toString().trim());
                params.put("phone_number", inputBox.getText().toString().trim());
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseSubmitVerificationCodeResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                showToast("Your phone has been verified successfully.");
                Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_CODE, response.getString("phone_number"));
                Intent intent = new Intent(getApplicationContext(), PrefRegisterActivity.class);
                intent.putExtra("member_id", response.getString("member_id"));
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else if(success.equals("1")){
                showToast("You have already been registered. Please login.");
            }else if(success.equals("2")){
                showToast("Your code is wrong. Try again with another code.");
            }else if(success.equals("3")){
                showToast("Sorry but we couldn't identify you.");
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }
}

























