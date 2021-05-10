package com.sobriety.sobriety.main;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.eyalbira.loadingdots.LoadingDots;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.utils.BitmapUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import me.leolin.shortcutbadger.ShortcutBadger;

import static android.os.Environment.getExternalStorageDirectory;

public class ChatActivity extends AppCompatActivity {

    LinearLayout layout;
    LinearLayout layout_2;
    ImageView sendButton, cameraButton;
    EditText messageArea;
    ScrollView scrollView;
    TextView statusView,ok, cancel, history, typeStatus;
    Firebase reference1, reference2, reference3, reference4, reference5, reference7;
    CircleImageView photo;
    int is_talking=0;
    int is_talkingR=0;
    boolean is_typing=false;
    ProgressBar progressBar;
    boolean startTalking=false;

    LinearLayout pictureItemFrame;
    FrameLayout alertBackground;

    ImageView image;
    LinearLayout imagePortion;
    String imageFile="";
    String messageReceivedTime = "";
    Uri downloadUri = null;
    String videoThumbStr = "";
    String imageStr = "";
    boolean userOnlineF = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

 //       Commons.user.setIdx(2);     // for test

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Firebase.setAndroidContext(this);
        reference1 = new Firebase(ReqConst.FIREBASE_URL + "message/" + Commons.thisUser.getIdx() + "_" + Commons.user.getIdx());
        reference2 = new Firebase(ReqConst.FIREBASE_URL + "message/" + Commons.user.getIdx() + "_" + Commons.thisUser.getIdx());
        reference3 = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + Commons.user.getIdx() + "/" + Commons.thisUser.getIdx());
        reference4 = new Firebase(ReqConst.FIREBASE_URL + "status/" + Commons.user.getIdx() + "_" + Commons.thisUser.getIdx());
        reference5 = new Firebase(ReqConst.FIREBASE_URL + "status/" + Commons.thisUser.getIdx() + "_" + Commons.user.getIdx());
        reference7 = new Firebase(ReqConst.FIREBASE_URL + "notification/user" + Commons.thisUser.getIdx() + "/" + Commons.user.getIdx());

        messageArea = (EditText)findViewById(R.id.messageArea);
        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (LinearLayout) findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        statusView=(TextView)findViewById(R.id.status);
        imagePortion=(LinearLayout)findViewById(R.id.imagePortion);
        image=(ImageView) findViewById(R.id.image);
        ((ImageView)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        pictureItemFrame = (LinearLayout) findViewById(R.id.pictureItemFrame);
        alertBackground = (FrameLayout) findViewById(R.id.layout);
        cancel=(TextView)findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePortion.setVisibility(View.GONE);
            }
        });
        ok=(TextView)findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                imagePortion.setVisibility(View.GONE);
            }
        });
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        photo=(CircleImageView) findViewById(R.id.imv_photo);
        Picasso.with(getApplicationContext())
                .load(Commons.user.getPhotoUrl())
                .error(R.drawable.usericon)
                .placeholder(R.drawable.usericon)
                .into(photo, new Callback() {
                    @Override
                    public void onSuccess() {
                        ((ProgressBar)findViewById(R.id.progressBar2)).setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        ((ProgressBar)findViewById(R.id.progressBar2)).setVisibility(View.INVISIBLE);
                    }
                });
        cameraButton=(ImageView)findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertPhoto();
            }
        });

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
        TextView name = (TextView)findViewById(R.id.txv_name);
        name.setTypeface(font);
        name.setText(Commons.user.getName());

        typeStatus = (TextView)findViewById(R.id.typeStatus);

        messageArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String messageText = messageArea.getText().toString().trim();
                if(!is_typing){
                    is_typing=true;
                    if(messageText.length() > 0){
                        sendButton.setVisibility(View.VISIBLE);
                        cameraButton.setVisibility(View.GONE);
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("online", "typing");
                        map.put("time", String.valueOf(new Date().getTime()));
                        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                        reference4.removeValue();
                        reference4.push().setValue(map);
                    }
                }else {
                    if(messageText.length()==0){
                        sendButton.setVisibility(View.GONE);
                        cameraButton.setVisibility(View.VISIBLE);
                        is_typing=false;
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("online", "online");
                        map.put("time", String.valueOf(new Date().getTime()));
                        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                        reference4.removeValue();
                        reference4.push().setValue(map);
                    }
                }
            }
        });

        online("true");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString().trim();
                if(messageText.length() > 0){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("time", String.valueOf(new Date().getTime()));
                    map.put("image", "");
                    map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                    map.put("name", Commons.thisUser.getName());
                    map.put("photo", Commons.thisUser.getPhotoUrl());

                    online("true");
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                    is_typing=false;

                    Map<String, String> map2 = new HashMap<String, String>();
                    map2.put("message", messageText);
                    map2.put("time", String.valueOf(new Date().getTime()));
                    map2.put("group_name", "");
                    map2.put("network_id", String.valueOf(Commons.myNetworkId));
                    map2.put("call_code", "");
                    map2.put("option", "");
                    map2.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                    map2.put("sender_phone", Commons.thisUser.getPhoneNumber());
                    map2.put("sender_name", Commons.thisUser.getName());
                    map2.put("sender_photo", Commons.thisUser.getPhotoUrl());
                    reference3.removeValue();
                    reference3.push().setValue(map2);

                    sendFcmNotification(messageText);

                }
                sendButton.setVisibility(View.GONE);
                cameraButton.setVisibility(View.VISIBLE);
            }
        });

        reference5.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String online = map.get("online").toString();
                String time = map.get("time").toString();

                if(online.equals("online")){
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setText("Online");
                    typeStatus.setVisibility(View.GONE);
                    userOnlineF = true;
                }else if(online.equals("offline")){
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setText("Last seen at "+ DateFormat.format("MM/dd/yyyy hh:mm a",
                            Long.parseLong(time)));
                    userOnlineF = false;
                }else {

                    statusView.setVisibility(View.VISIBLE);
                    statusView.setText("is typing . . .");
                    typeStatus.setVisibility(View.VISIBLE);
                    try{
                        if(Commons.user.getName()!=null){
                            typeStatus.setText(Commons.user.getName()+" "+"is typing ...");
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }catch (Exception e){}

                    userOnlineF = true;

                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });

                }

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

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String image = map.get("image").toString();
                String senderId = map.get("sender_id").toString();
                String name = map.get("name").toString();
                String photo = map.get("photo").toString();
                String time = map.get("time").toString();
                LatLng latLng=null;
                String key = dataSnapshot.getKey();
                messageReceivedTime = time;

                if(senderId.equals(String.valueOf(Commons.thisUser.getIdx()))){
                    addMessageBox(message, time, image, key, 1);
                }
                else{
                    addMessageBox(message, time, image, key, 2);
                }
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

        reference7.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    Map map = dataSnapshot.getValue(Map.class);
                    String dateTime = map.get("time").toString();
                    if(Math.abs(Long.parseLong(dateTime) - Long.parseLong(messageReceivedTime)) < 5000) reference7.child(dataSnapshot.getKey()).removeValue();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }catch (FirebaseException e){
                    e.printStackTrace();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
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

    public void online(String status){
        Map<String, String> map = new HashMap<String, String>();
        if(status.equals("true"))
            map.put("online", "online");
        else map.put("online", "offline");
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
        reference4.removeValue();
        reference4.push().setValue(map);
        if(status.equals("false")){
            reference7 = null;
            finish();
            overridePendingTransition(R.anim.left_in,R.anim.right_out);
        }
    }

    public void addMessageBox(final String message, String time, final String imageStr, String key, final int type){

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.chat_history_area, null);

        FrameLayout photoFrame = (FrameLayout)dialogView.findViewById(R.id.photoFrame);
        final CircleImageView photo=(CircleImageView) dialogView.findViewById(R.id.photo);
        Picasso.with(getApplicationContext())
                .load(Commons.user.getPhotoUrl())
                .error(R.drawable.usericon)
                .placeholder(R.drawable.usericon)
                .into(photo);

        final LinearLayout read=(LinearLayout)dialogView.findViewById(R.id.read);
        final LinearLayout write=(LinearLayout)dialogView.findViewById(R.id.write);
        final LinearLayout dotrec=(LinearLayout)dialogView.findViewById(R.id.receiverdots);
        final LinearLayout dotsend=(LinearLayout)dialogView.findViewById(R.id.senderdots);
        final TextView text = (TextView) dialogView.findViewById(R.id.text);
        final TextView text2 = (TextView) dialogView.findViewById(R.id.text2);
        final TextView datetime = (TextView) dialogView.findViewById(R.id.datetime);
        final TextView datetime2 = (TextView) dialogView.findViewById(R.id.datetime2);
        final TextView writespace = (TextView) dialogView.findViewById(R.id.writespace);
        ImageView image=(ImageView) dialogView.findViewById(R.id.image);
        ImageView image2=(ImageView) dialogView.findViewById(R.id.image2);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {

            if(is_talking==1){
                dotsend.setVisibility(View.INVISIBLE);
            }else {
                dotsend.setVisibility(View.VISIBLE);
            }

            photoFrame.setVisibility(View.GONE);
            read.setVisibility(View.GONE);

            dotrec.setVisibility(View.GONE);
            writespace.setVisibility(View.VISIBLE);
            write.setVisibility(View.VISIBLE);
            text2.setText(message);
            text2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(text2.getText().length() > 0){
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, text2.getText().toString());
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                    }
                }
            });

            text2.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
//                    showEditTextAlert(key, text2, dialogView);
                    return false;
                }
            });

            datetime2.setText(DateFormat.format("MM/dd/yyyy hh:mm a",
                    Long.parseLong(time)));

            if(imageStr.length()>0){
                image2.setVisibility(View.VISIBLE);
                Picasso.with(getApplicationContext())
                        .load(imageStr)
                        .error(R.drawable.noresult)
                        .placeholder(R.drawable.noresult)
                        .into(image2);
                image2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                        intent.putExtra("image", imageStr);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });
                text2.setVisibility(View.GONE);
                image2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
//                        showDeleteFileAlert(key, dialogView);
                        return false;
                    }
                });
            }else {
                image2.setVisibility(View.GONE);
                text2.setVisibility(View.VISIBLE);
            }

            if(is_talking==0){
                is_talking=1; is_talkingR=0;
            }
        }
        else{
            if(is_talkingR==1){
                photoFrame.setVisibility(View.INVISIBLE);
                dotrec.setVisibility(View.INVISIBLE);
            }else {
                photoFrame.setVisibility(View.VISIBLE);
                dotrec.setVisibility(View.VISIBLE);
            }

            read.setVisibility(View.VISIBLE);
            dotsend.setVisibility(View.GONE);

            writespace.setVisibility(View.GONE);
            write.setVisibility(View.GONE);

            text.setText(message);
            //        reference.removeValue();
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(text.getText().length()>0){
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                    }
                }
            });
            datetime.setText(DateFormat.format("MM/dd/yyyy hh:mm a",
                    Long.parseLong(time)));

            if(imageStr.length()>0){
                image.setVisibility(View.VISIBLE);
                Picasso.with(getApplicationContext())
                        .load(imageStr)
                        .error(R.drawable.noresult)
                        .placeholder(R.drawable.noresult)
                        .into(image);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                        intent.putExtra("image", imageStr);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });
                text.setVisibility(View.GONE);
            }else {
                image.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
            }

            if(is_talkingR==0){
                is_talking=0; is_talkingR=1;
            }
        }

        layout.addView(dialogView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startTalking=true;
            }
        }, 2000);

    }

    public Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return base64ToBitmap(encodedString);
        }
    }

    public Bitmap base64ToBitmap(String base64Str){
        Bitmap bitmap;
        final String pureBase64Encoded = base64Str.substring(base64Str.indexOf(",")  + 1);
        final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        return bitmap;
    }

    public void showAlertPhoto(){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
        pictureItemFrame.startAnimation(animation);
        pictureItemFrame.setVisibility(View.VISIBLE);
        alertBackground.setVisibility(View.VISIBLE);
    }

    public void dismisFrame(View view){
        if(pictureItemFrame.getVisibility() == View.VISIBLE){
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_down);
            pictureItemFrame.startAnimation(animation);
            pictureItemFrame.setVisibility(View.GONE);
            alertBackground.setVisibility(View.GONE);
        }
    }

    public void takePhoto(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.TAKE_FROM_CAMERA);
        pictureItemFrame.setVisibility(View.GONE);
        alertBackground.setVisibility(View.GONE);
    }

    public void pickPhoto(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_GALLERY);
        pictureItemFrame.setVisibility(View.GONE);
        alertBackground.setVisibility(View.GONE);
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

    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                File file = new File(Environment.getExternalStorageDirectory()+"/Pictures",
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    file.createNewFile();
                    fo = new FileOutputStream(file);
                    fo.write(byteArrayOutputStream.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imagePortion.setVisibility(View.VISIBLE);
                image.setImageBitmap(bitmap);
                imageToDonwloadUrl(file.getPath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Pictures");
        if (!dir.exists())
            dir.mkdirs();
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File file = new File(getExternalStorageDirectory()+"/Pictures",
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imagePortion.setVisibility(View.VISIBLE);
        image.setImageBitmap(thumbnail);
        imageToDonwloadUrl(file.getPath());
    }

    public void imageBitmapToString(Bitmap bitmap){
        if(bitmap!=null){
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            //        bitmap.recycle();
            byte[] byteArray = bYtE.toByteArray();
            imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
            bitmap=null;
        }
    }

    public void uploadImage(){

        if(imageStr.length()>0){

            Map<String, String> map = new HashMap<String, String>();
            map.put("message", "");
            map.put("time", String.valueOf(new Date().getTime()));
            map.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
            map.put("name", Commons.thisUser.getName());
            map.put("photo", Commons.thisUser.getPhotoUrl());
            map.put("image", imageStr);

            online("true");
            reference1.push().setValue(map);
            reference2.push().setValue(map);
            is_typing=false;

            Map<String, String> map2 = new HashMap<String, String>();
            map2.put("message", "Shared a picture");
            map2.put("time", String.valueOf(new Date().getTime()));
            map2.put("group_name", "");
            map2.put("network_id", String.valueOf(Commons.myNetworkId));
            map2.put("call_code", "");
            map2.put("option", "");
            map2.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
            map2.put("sender_phone", Commons.thisUser.getPhoneNumber());
            map2.put("sender_name", Commons.thisUser.getName());
            map2.put("sender_photo", Commons.thisUser.getPhotoUrl());
            reference3.removeValue();
            reference3.push().setValue(map2);

            sendFcmNotification("Sent a picture.");

            imageStr="";
        }
    }

    private void imageToDonwloadUrl(String path){
        progressBar.setVisibility(View.VISIBLE);
        final String[] url = {""};
        final Uri[] uri = {Uri.fromFile(new File(path))};
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://sobriety-cf2c1.appspot.com");
        StorageReference fileReference = firebaseStorage.getReference();

        UploadTask uploadTask = fileReference.child(uri[0].getLastPathSegment()+ ".jpg").putFile(uri[0]);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBar.setVisibility(View.GONE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                imageStr = downloadUri.toString();
                Log.d("IMAGE===>",imageStr);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void thumbToDonwloadUrl(String path){
        final String[] url = {""};
        final Uri[] uri = {Uri.fromFile(new File(path))};
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance("gs://sobriety-cf2c1.appspot.com");
        StorageReference fileReference = firebaseStorage.getReference();

        UploadTask uploadTask = fileReference.child(uri[0].getLastPathSegment()+ ".jpg").putFile(uri[0]);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                Log.d("ImageUrl===>", downloadUri.toString());
                videoThumbStr = downloadUri.toString();
            }
        });
    }

    private String saveImage(Bitmap finalBitmap, String image_name) {
        String path = "";
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            path = file.getPath();
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public void sendFcmNotification(String message) {

        String url = ReqConst.SERVER_URL + "sendnotification";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseSendNotificationResponse(response);

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
                params.put("member_id", String.valueOf(Commons.user.getIdx()));
                params.put("sender_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("text", message);
                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseSendNotificationResponse(String json) {
        try {
            JSONObject response = new JSONObject(json);   Log.d("RESP=====> :",response.toString());

            String success = response.getString("result_code");
            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                Log.d("ResultMessage+++", response.getString("message"));
            } else {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        online("false");
    }
}



























