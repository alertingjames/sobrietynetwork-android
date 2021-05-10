package com.sobriety.sobriety.main;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;
import com.sobriety.sobriety.utils.MultiPartRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    ImageView storyicon, networkicon, homeicon, groupsicon, settingsicon;
    TextView storyText, networkText, homeText, groupsText, settingsText;
    View storyIndicator, networkIndicator, homeIndicator, groupsIndicator, settingsIndicator;
    File imageFile = null;
    static EditText dateBox;
    static int yy=2018, mm=1, dd = 1;
    static long timeStamp = 0;
    TextView name, username;
    LinearLayout male, female;
    int makingcalls_option = 0;
    int receivingcalls_option = 0;
    RadioGroup radioGroup1, radioGroup2;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Picasso.with(getApplicationContext())
                .load(Commons.thisUser.getPhotoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(((CircleImageView)findViewById(R.id.picture)));

        Picasso.with(getApplicationContext())
                .load(Commons.thisUser.getPhotoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(((ImageView)findViewById(R.id.profilePicture)));

        makingcalls_option = Preference.getInstance().getValue(getApplicationContext(), PrefConst.PREF_MAKINGCALL_OPTION, 0);
        receivingcalls_option = Preference.getInstance().getValue(getApplicationContext(), PrefConst.PREF_RECEIVINGCALL_OPTION, 0);

        radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int ii) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(ii);
                if(radioButton.isChecked()){
                    if(radioButton.getText().equals("Show name"))makingcalls_option = 1;
                    else if(radioButton.getText().equals("Show profile photo"))makingcalls_option = 2;
                }
            }
        });
        radioGroup2 = (RadioGroup)findViewById(R.id.radioGroup2);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int ii) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(ii);
                if(radioButton.isChecked()){
                    if(radioButton.getText().equals("Show name to callers"))receivingcalls_option = 1;
                    else if(radioButton.getText().equals("Show profile photo to callers"))receivingcalls_option = 2;
                }
            }
        });

        if(makingcalls_option == 1)((RadioButton) radioGroup1.getChildAt(0)).setChecked(true);
        else if(makingcalls_option == 2)((RadioButton) radioGroup1.getChildAt(1)).setChecked(true);

        if(receivingcalls_option == 1)((RadioButton) radioGroup2.getChildAt(0)).setChecked(true);
        else if(receivingcalls_option == 2)((RadioButton) radioGroup2.getChildAt(1)).setChecked(true);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title2)).setTypeface(font);
        ((Button)findViewById(R.id.logoutButton)).setTypeface(font);

        username = ((TextView)findViewById(R.id.usernameBox));
        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
        username.setTypeface(font);
        ((EditText)findViewById(R.id.dateBox)).setTypeface(font);
        ((TextView)findViewById(R.id.maleLabel)).setTypeface(font);
        ((TextView)findViewById(R.id.femaleLabel)).setTypeface(font);

        name = ((TextView)findViewById(R.id.name));
        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "ag-futura-58e274b5588ad.ttf");
        name.setTypeface(font);
        name.setText(rename(Commons.thisUser.getName()));
        username.setText(Commons.thisUser.getUsername());

        male = (LinearLayout)findViewById(R.id.male);
        female = (LinearLayout)findViewById(R.id.female);

        if(Commons.thisUser.getGender().equals("Male")){
            male.setVisibility(View.VISIBLE);
            female.setVisibility(View.GONE);
        }else if(Commons.thisUser.getGender().equals("Female")){
            male.setVisibility(View.GONE);
            female.setVisibility(View.VISIBLE);
        }

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

        ((FrameLayout)findViewById(R.id.homeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
            }
        });

//        Calendar c = Calendar.getInstance();
//        yy = c.get(Calendar.YEAR);
//        mm = c.get(Calendar.MONTH);
//        dd = c.get(Calendar.DAY_OF_MONTH);
//
//        dateBox = (EditText) findViewById(R.id.dateBox);
//        dateBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showTruitonDatePickerDialog();
//            }
//        });
//
//        String[] monthes={"January","February","March","April","May","June","July","August","September","October","November","December"};
//
//        c = Calendar.getInstance();
//        //Set time in milliseconds
//        c.setTimeInMillis(Long.parseLong(Commons.thisUser.getCleanDate()));
//        int mYear = c.get(Calendar.YEAR);
//        int mMonth = c.get(Calendar.MONTH);
//        int mDay = c.get(Calendar.DAY_OF_MONTH);
//        if(mDay<10)
//            dateBox.setText(monthes[mMonth] + " 0" + mDay + ", " + mYear);
//        else
//            dateBox.setText(monthes[mMonth] + " " + mDay + ", " + mYear);
//        timeStamp = getTimeStamp(mMonth, mDay, mYear);

        ((EditText)findViewById(R.id.dateInputBox)).setText(Commons.thisUser.getCleanDate());

        new Timer().schedule(doAsynchronousTask6, 0, 2000);
    }

    Handler mHandler = new Handler();
    TimerTask doAsynchronousTask6 = new TimerTask() {

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
        doAsynchronousTask6.cancel();
        Log.d("Destroy+++", "Yes");
    }

    @Override
    public void finish() {
        super.finish();
        doAsynchronousTask6.cancel();
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            progressBar = null;
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
        texts[4].setTextColor(getResources().getColor(R.color.colorPrimary));
        indicators[4].setVisibility(View.VISIBLE);
        icons[4].getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    public void gotoLoadPicturePage(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        ((LinearLayout)findViewById(R.id.editPictureFrame)).setAnimation(animation);
        ((LinearLayout)findViewById(R.id.editPictureFrame)).setVisibility(View.VISIBLE);
    }

    public void cancelEditPicture(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
        ((LinearLayout)findViewById(R.id.editPictureFrame)).setAnimation(animation);
        ((LinearLayout)findViewById(R.id.editPictureFrame)).setVisibility(View.GONE);
    }

    public void takePhoto(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.TAKE_FROM_CAMERA);
    }

    public void pickPhoto(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case Constants.PICK_FROM_GALLERY:

                if (resultCode == RESULT_OK) {
                    onSelectFromGalleryResult(data);
                }
                break;

            case Constants.TAKE_FROM_CAMERA: {

                if (resultCode == RESULT_OK) {
                    onCaptureImageResult(data);
                }

                break;
            }
        }
    }

    Bitmap image = null;

    private void onCaptureImageResult(Intent data) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Pictures");
        if (!dir.exists())
            dir.mkdirs();

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        imageFile = new File(Environment.getExternalStorageDirectory()+"/Pictures",
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            imageFile.createNewFile();
            fo = new FileOutputStream(imageFile);
            fo.write(byteArrayOutputStream.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((ImageView)findViewById(R.id.profilePicture)).setImageBitmap(thumbnail);
        image = thumbnail;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                imageFile = new File(Environment.getExternalStorageDirectory()+"/Pictures",
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    imageFile.createNewFile();
                    fo = new FileOutputStream(imageFile);
                    fo.write(byteArrayOutputStream.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ((ImageView)findViewById(R.id.profilePicture)).setImageBitmap(bitmap);
                image = bitmap;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadPicture(View view){
        if(image != null){
            ((ImageView)findViewById(R.id.cameraicon)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.picture)).setImageBitmap(image);

            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
            ((LinearLayout)findViewById(R.id.editPictureFrame)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.editPictureFrame)).setVisibility(View.GONE);
        }else showToast("Please take picture.");
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

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        String[] monthes={"January","February","March","April","May","June","July","August","September","October","November","December"};

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            if(day<10)
                dateBox.setText(monthes[month] + " 0" + day + ", " + year);
            else
                dateBox.setText(monthes[month] + " " + day + ", " + year);
            timeStamp = getTimeStamp(month, day, year);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String myDate = dateFormat.format(new Date(timeStamp));
            String myDate0 = dateFormat.format(new Date());

            Log.d("Date2===>", myDate);
            Log.d("Date1===>", myDate0);
        }
    }

    public static long getTimeStamp(int month, int day, int year) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        return c.getTimeInMillis();
    }

    public void showTruitonDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void logOut(View view){
        Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_CODE, "");
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        if(((LinearLayout)findViewById(R.id.editPictureFrame)).getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
            ((LinearLayout)findViewById(R.id.editPictureFrame)).setAnimation(animation);
            ((LinearLayout)findViewById(R.id.editPictureFrame)).setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    public void saveSettings(View view){
        if(username.getText().length() == 0){
            showToast("Enter your username.");
            return;
        }

        if(((EditText)findViewById(R.id.dateInputBox)).getText().length() == 0){
            showToast("Enter Sober/Clean date.");
            return;
        }
        updateUser();
    }

    public void updateUser() {
        String url = ReqConst.SERVER_URL + "updateMember";
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

                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("username", username.getText().toString().trim());
//                params.put("clean_date", String.valueOf(timeStamp));
                params.put("clean_date", ((EditText)findViewById(R.id.dateInputBox)).getText().toString().trim());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseRestUrlsResponse(String json) {

        try {
            JSONObject response = new JSONObject(json);   Log.d("response=====> :",response.toString());

            String success = response.getString("result_code");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                if(imageFile != null)
                    uploadImage(Commons.thisUser.getIdx());
                else {
                    progressBar.setVisibility(View.GONE);
                    JSONObject object = response.getJSONObject("data");
                    User user = new User();
                    user.setIdx(object.getInt("id"));
                    user.setName(object.getString("name"));
                    user.setUsername(object.getString("username"));
                    user.setGender(object.getString("gender"));
                    user.setPhoneNumber(object.getString("phone_number"));
                    user.setPhotoUrl(object.getString("photo_url"));
                    user.setCleanDate(object.getString("clean_date"));
                    Commons.thisUser = user;
                    processResult();
                }
            } else if(success.equals("101")){
                progressBar.setVisibility(View.GONE);
                showToast("Someone is using the same username. Try again with another username.");
            }
            else {
                progressBar.setVisibility(View.GONE);
                showToast("Server issue");
            }

        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    public void uploadImage(int id) {
        try {

            final Map<String, String> params = new HashMap<>();
            params.put("member_id", String.valueOf(id));

            String url = ReqConst.SERVER_URL + "uploadMemberPicture";

            MultiPartRequest reqMultiPart = new MultiPartRequest(url, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    progressBar.setVisibility(View.GONE);
                    showToast("Server issue");
                }
            }, new Response.Listener<String>() {

                @Override
                public void onResponse(String json) {

                    ParseUploadImgResponse(json);
                    Log.d("imageJson===",json.toString());
                }
            }, imageFile, "file", params);

            reqMultiPart.setRetryPolicy(new DefaultRetryPolicy(
                    Constants.VOLLEY_TIME_OUT, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            SobrietyApplication.getInstance().addToRequestQueue(reqMultiPart, url);

        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            showToast("Server issue");
        }
    }


    public void ParseUploadImgResponse(String json) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject response = new JSONObject(json);
            int result_code = response.getInt("result_code");
            if (result_code == 0) {
                JSONObject object = response.getJSONObject("data");
                User user = new User();
                user.setIdx(object.getInt("id"));
                user.setName(object.getString("name"));
                user.setUsername(object.getString("username"));
                user.setGender(object.getString("gender"));
                user.setPhoneNumber(object.getString("phone_number"));
                user.setPhotoUrl(object.getString("photo_url"));
                user.setCleanDate(object.getString("clean_date"));
                Commons.thisUser = user;
                processResult();
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Server issue");
        }
    }

    private void processResult(){
        Preference.getInstance().put(getApplicationContext(),
                PrefConst.PREF_MAKINGCALL_OPTION, makingcalls_option);
        Preference.getInstance().put(getApplicationContext(),
                PrefConst.PREF_RECEIVINGCALL_OPTION, receivingcalls_option);
        Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_CODE, Commons.thisUser.getUsername());
        showToast("Saved");
    }
}































