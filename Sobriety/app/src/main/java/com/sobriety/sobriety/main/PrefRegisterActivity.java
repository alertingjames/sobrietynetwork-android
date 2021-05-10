package com.sobriety.sobriety.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
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
import com.firebase.client.Firebase;
import com.marozzi.roundbutton.RoundButton;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.classes.ColorHelper;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.preference.PrefConst;
import com.sobriety.sobriety.preference.Preference;
import com.sobriety.sobriety.utils.ColorUtil;
import com.sobriety.sobriety.utils.MultiPartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PrefRegisterActivity extends AppCompatActivity {

    RadioGroup radioGroup1, radioGroup2;
    LinearLayout male, female;
    EditText fullnameBox, usernameBox, phoneBox;
    static TextView dateBox;
    LinearLayout pictureItemFrame;
    FrameLayout layout;
    File imageFile = null;
    static int yy=2018, mm=1, dd = 1;
    static long timeStamp = 0;
    ProgressBar progressBar;
    String gender = "";
    int makingcalls_option = 0;
    int receivingcalls_option = 0;
    String phone_number = "13175184250";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pref_register);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        phone_number = getIntent().getStringExtra("phone_number");
        phoneBox = (EditText)findViewById(R.id.phoneInputBox);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        final Calendar c = Calendar.getInstance();
        yy = c.get(Calendar.YEAR);
        mm = c.get(Calendar.MONTH);
        dd = c.get(Calendar.DAY_OF_MONTH);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.maleLabel)).setTypeface(font);
        ((TextView)findViewById(R.id.femaleLabel)).setTypeface(font);

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

        fullnameBox = (EditText)findViewById(R.id.fullnameBox);
        usernameBox = (EditText)findViewById(R.id.usernameBox);
        dateBox = (TextView)findViewById(R.id.dateBox);
        dateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTruitonDatePickerDialog();
            }
        });

        font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
        fullnameBox.setTypeface(font);
        usernameBox.setTypeface(font);
        dateBox.setTypeface(font);

        male = (LinearLayout)findViewById(R.id.male);
        female = (LinearLayout)findViewById(R.id.female);

        pictureItemFrame = (LinearLayout) findViewById(R.id.pictureItemFrame);
        layout = (FrameLayout) findViewById(R.id.layout);

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGenderWidget();
                ((ImageView)findViewById(R.id.maleIcon)).setImageResource(R.drawable.ic_male_selected);
                gender = "Male";
            }
        });

        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGenderWidget();
                ((ImageView)findViewById(R.id.femaleIcon)).setImageResource(R.drawable.ic_female_selected);
                gender = "Female";
            }
        });
    }

    private void resetGenderWidget(){
        ((ImageView)findViewById(R.id.maleIcon)).setImageResource(R.drawable.ic_male);
        ((ImageView)findViewById(R.id.femaleIcon)).setImageResource(R.drawable.ic_female);
    }

    public void showAlertTakePhoto(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
        pictureItemFrame.startAnimation(animation);
        pictureItemFrame.setVisibility(View.VISIBLE);
        layout.setVisibility(View.VISIBLE);
    }

    public void dismisFrame(View view){
        if(pictureItemFrame.getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_down);
            pictureItemFrame.startAnimation(animation);
            pictureItemFrame.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        }
    }

    public void takePhoto(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.TAKE_FROM_CAMERA);
        pictureItemFrame.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
    }

    public void pickPhoto(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_GALLERY);
        pictureItemFrame.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
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
        ((ImageView)findViewById(R.id.cameraicon)).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.picture)).setImageBitmap(thumbnail);
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

                ((ImageView)findViewById(R.id.cameraicon)).setVisibility(View.GONE);
                ((ImageView)findViewById(R.id.picture)).setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean validCellPhone(String number)
    {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }

    public void signUp(View view){
        if(fullnameBox.getText().length() == 0){
            showToast("Enter your full name.");
            return;
        }
        if(usernameBox.getText().length() == 0){
            showToast("Enter your username.");
            return;
        }

        if(!validCellPhone(phoneBox.getText().toString().trim())){
            showToast("Enter valid phone number.");
            return;
        }

        if(phoneBox.getText().length() == 0){
            showToast("Enter your phone number.");
            return;
        }
//        if(timeStamp == 0){
//            showToast("Enter Sober/Clean date.");
//            return;
//        }
        if(((EditText)findViewById(R.id.dateInputBox)).getText().length() == 0){
            showToast("Enter Sober/Clean date.");
            return;
        }

        if(gender.length() == 0){
            showToast("Select your gender.");
            return;
        }
//        if(imageFile == null){
//            showToast("Please take your photo.");
//            return;
//        }
        if(makingcalls_option == 0){
//            showToast("Select one of the options when making calls.");
//            return;
        }
        if(receivingcalls_option == 0){
//            showToast("Select one of the options when receiving calls.");
//            return;
        }
        registerUser();
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

    public void registerUser() {
        String url = ReqConst.SERVER_URL + "registerMember";
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

                params.put("phone_number", phoneBox.getText().toString().trim());
                params.put("name", fullnameBox.getText().toString().trim());
                params.put("username", usernameBox.getText().toString().trim());
                params.put("gender", gender);
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
                String member_id = response.getString("member_id");
                if(imageFile != null){
                    uploadImage(member_id);
                }else {
                    ((ImageView)findViewById(R.id.picture)).setImageBitmap(createBitmapFromLayoutWithText());
                    uploadImage(member_id);
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

    public void uploadImage(String id) {
        try {

            final Map<String, String> params = new HashMap<>();
            params.put("member_id", id);

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
                Preference.getInstance().put(getApplicationContext(), PrefConst.PREF_CODE, user.getUsername());
                getInvitedMembers(Commons.thisUser.getPhoneNumber());
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
        Intent intent=new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.right_in,R.anim.left_out);
    }

    private void getInvitedMembers(String phone_number) {
        String url = ReqConst.SERVER_URL + "getInvitedMembers";

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
                showToast("Server issue");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phone_number", phone_number);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);
    }

    public void parseLoginResponse(String json) {

        try {
            JSONObject response = new JSONObject(json);

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                JSONArray userInfo = response.getJSONArray("data");
                if(userInfo.length() == 0){
                    processResult();
                    return;
                }
                for(int i=0; i<userInfo.length(); i++){
                    int finalI = i;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject object = null;
                            try {
                                object = (JSONObject) userInfo.get(finalI);
                                int memberId = object.getInt("id");
                                String memberName = rename(object.getString("name"));
                                String memberUsername = object.getString("username");
                                String memeberGender = object.getString("gender");
                                String memberPhoneNumber = object.getString("phone_number");
                                String memberPhotoUrl = object.getString("photo_url");
                                String memberCleanDate = object.getString("clean_date");
                                int networkId = object.getInt("network_id");

                                Firebase reference = new Firebase(ReqConst.FIREBASE_URL + "reqcall/user" + String.valueOf(Commons.thisUser.getIdx()) + "/" + String.valueOf(memberId));
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("message", "I invite you to our Network.");
                                map.put("time", String.valueOf(new Date().getTime()));
                                map.put("group_name", "");
                                map.put("network_id", String.valueOf(networkId));
                                map.put("call_code", "");
                                map.put("sender_id", String.valueOf(memberId));
                                map.put("sender_phone", memberPhoneNumber);
                                map.put("sender_name", memberName);
                                map.put("sender_photo", memberPhotoUrl);
                                reference.removeValue();
                                reference.push().setValue(map);

                                if(finalI == userInfo.length() - 1)
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            processResult();
                                        }
                                    }, 500);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);
                }
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

    String APP_PATH_SD_CARD = "/Icons/";
    String APP_THUMBNAIL_PATH_SD_CARD = "thumbnails";

    private boolean saveImageToExternalStorage(Bitmap image) {
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SD_CARD + APP_THUMBNAIL_PATH_SD_CARD;

        try {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OutputStream fOut = null;
            File file = new File(fullPath, System.currentTimeMillis() + ".png");
            file.createNewFile();
            fOut = new FileOutputStream(file);

// 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            imageFile = file;

            return true;

        } catch (Exception e) {
            Log.e("saveToExternalStorage()", e.getMessage());
            return false;
        }
    }

    public Bitmap createBitmapFromLayoutWithText() {
        LayoutInflater  mInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Inflate the layout into a view and configure it the way you like
        RelativeLayout view = new RelativeLayout(getApplicationContext());
        mInflater.inflate(R.layout.icon, view, true);
        FrameLayout icon = (FrameLayout)view.findViewById(R.id.icon);
        icon.setBackgroundColor(ColorHelper.getRandomMaterialColor(getApplicationContext()));
        TextView tv = (TextView) view.findViewById(R.id.icon_name);
        tv.setText(fullnameBox.getText().toString().trim().substring(0,1));

        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        view.setLayoutParams(new WindowManager.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        if(saveImageToExternalStorage(bitmap)){
            return bitmap;
        }else {
            //Get the image to be changed from the drawable, drawable-xhdpi, drawable-hdpi,etc folder.
            Drawable sourceDrawable = getResources().getDrawable(R.drawable.photo);
            //Convert drawable in to bitmap
            Bitmap sourceBitmap = ColorUtil.convertDrawableToBitmap(sourceDrawable);
            //Pass the bitmap and color code to change the icon color dynamically.
            bitmap = ColorUtil.changeImageColor(sourceBitmap, ColorHelper.getRandomMaterialColor(getApplicationContext()));
            if(saveImageToExternalStorage(bitmap)){
                return bitmap;
            }
        }
        return bitmap;
    }

    public void back(View view){
        onBackPressed();
    }
}
























