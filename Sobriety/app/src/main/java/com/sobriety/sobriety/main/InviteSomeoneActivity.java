package com.sobriety.sobriety.main;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.hbb20.CountryCodePicker;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InviteSomeoneActivity extends AppCompatActivity {


    EditText inputBox;
    ImageView contactButton;
    CheckBox checkBox;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_invite_someone);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        inputBox = (EditText)findViewById(R.id.inputBox);
        contactButton = (ImageView)findViewById(R.id.contactButton);
        checkBox = (CheckBox)findViewById(R.id.checkBox);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void inviteToApp(View view){
        if(inputBox.getText().toString().trim().length() == 0){
            showToast("Please enter phone number for invitation.");
            return;
        }
        String cc = ((CountryCodePicker)findViewById(R.id.cc)).getSelectedCountryCode();

        if(!inputBox.getText().toString().trim().startsWith("+")){
            showToast("Please add country code to the phone number.");
            return;
        }
        getMemberToInvite(inputBox.getText().toString().trim());
    }

    private void getMemberToInvite(String phone_number) {
        String url = ReqConst.SERVER_URL + "inviteSomeone";
        progressBar.setVisibility(View.VISIBLE);

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetMemberToInviteResponse(response, phone_number);

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
                params.put("phone_number", phone_number);
                if(checkBox.isChecked())
                    params.put("network_id", String.valueOf(Commons.myNetworkId));
                else
                    params.put("network_id", "0");
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);
    }

    public void parseGetMemberToInviteResponse(String json, String phoneNumber) {
        progressBar.setVisibility(View.GONE);
        try {

            JSONObject response = new JSONObject(json);

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                String code = response.getString("code");
//                showToast(code);
                String msg = "Hi I am inviting you to our app. You can download the app from this link:\n"
                        + Constants.APP_LINK + "\n"
                        + "Verification code: " + code;
//                sendSMS(phoneNumber.replace("+","").replace("-","")
//                        .replace("(","").replace(")","").replace(" ",""), msg);
                showToast("Sent invitation.");
            } else if(success.equals("1")){
                showToast("This member's profile hasn't been completed yet. Try again with another one.");
            } else if(success.equals("2")){
                showToast("This member is not a new one with the app.");
            }else if(success.equals("3")){
                showToast("This member already is in your Network. Try again with another one.");
            }else if(success.equals("4")){
               // showToast("This member is not a new one but you can invite him/her to your Network.");
                // Invite to my Network
                int memberId = response.getInt("member_id");
                sendInvitationNoti(String.valueOf(memberId), "", "");
            } else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    final int PICK_CONTACT = 100;

    public void getContacts(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String phone_number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // TODO Fetch other Contact details as you want to use
                        inputBox.setText(phone_number);
                    }
                }
                break;
        }
    }

    public void sendSMS(String phoneNo, String msg) {
        Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address",phoneNo);
        smsIntent.putExtra("sms_body",msg);
        startActivity(smsIntent);
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

    @JsonProperty("group_name")
    private void sendInvitationNoti(String memberId, String groupName, String callCode) {
        Firebase reference = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + memberId + "/" + String.valueOf(Commons.thisUser.getIdx()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "I invite you to our Network.");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("group_name", groupName);
        map.put("network_id", String.valueOf(Commons.myNetworkId));
        map.put("call_code", callCode);
        map.put("option", "");
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        map.put("sender_phone", Commons.thisUser.getPhoneNumber());
        map.put("sender_name", Commons.thisUser.getName());
        map.put("sender_photo", Commons.thisUser.getPhotoUrl());
        reference.removeValue();
        reference.push().setValue(map);

        showToast("Sent message for invitation.");
    }

}
