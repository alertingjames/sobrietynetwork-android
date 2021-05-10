package com.sobriety.sobriety.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
import com.sobriety.sobriety.utils.MultiPartRequest;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddAudioStoryActivity extends AppCompatActivity {

    boolean recordFlag=false;
    boolean playFlag=false;

    private MediaRecorder myAudioRecorder=null;
    private MediaPlayer myPlayer;
    private String outputFile = "";
    private Handler mHandler = new Handler();

    SeekBar seekBar;
    ProgressBar progressBar, progressBar2;
    TextView sendAudio;
    ImageView record, play;
    public static final boolean NATIVE_WEB_P_SUPPORT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final int RECORD_AUDIO = 0;

    GifView gifView;
    EditText inputBox;
    FrameLayout nameBox;
    TextView cancelBoxButton, sendButton;
    View boxBackground;
    File audioFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_audio_story);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        inputBox = (EditText)findViewById(R.id.inputBox);
        nameBox = (FrameLayout)findViewById(R.id.nameBox);
        cancelBoxButton = (TextView)findViewById(R.id.cancel_button);
        sendButton = (TextView)findViewById(R.id.send_button);
        boxBackground = (View)findViewById(R.id.layout);

        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(inputBox.getText().toString().trim().length() > 0) sendButton.setVisibility(View.VISIBLE);
                else sendButton.setVisibility(View.GONE);
            }
        });

        cancelBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameBox.setVisibility(View.GONE);
                boxBackground.setVisibility(View.GONE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputBox.getText().length() > 0){
                    nameBox.setVisibility(View.GONE);
                    boxBackground.setVisibility(View.GONE);
                    uploadAudioInfo();
                }
            }
        });

        gifView = (GifView) findViewById(R.id.gif);
        gifView.play();

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.boxTitle)).setTypeface(font);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);

        } else {
            seekBar=(SeekBar)findViewById(R.id.seekBar);

            play=(ImageView) findViewById(R.id.play);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!playFlag){
                        if(outputFile.length()==0){
                            showToast("Please record audio.");
                            return;
                        }
                        playFlag=true;
                        try {
                            myPlayer = new MediaPlayer();
                            myPlayer.setDataSource(outputFile);
                            myPlayer.prepare();
                            myPlayer.start();
                            seekBar.setVisibility(View.VISIBLE);
                            seekBar.setMax(myPlayer.getDuration());
                            gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                            AddAudioStoryActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if(myPlayer != null){
                                        int mCurrentPosition = myPlayer.getCurrentPosition();
                                        seekBar.setProgress(mCurrentPosition);
                                        if(mCurrentPosition>=seekBar.getMax()){
                                            record.setEnabled(true);
                                            playFlag=false;
                                            play.setImageResource(R.drawable.audioplayicon);
                                            seekBar.setVisibility(View.INVISIBLE);
                                            gifView.setVisibility(View.GONE);
                                        }
                                    }
                                    mHandler.postDelayed(this, 1000);
                                }
                            });

                            showToast("Playing audio");

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audiostopicon);
                        //        play.setTextSize(11.00f);
                        record.setEnabled(false);
                    }else {
                        playFlag=false;
                        gifView.setVisibility(View.GONE);
                        try {
                            if (myPlayer != null) {
                                myPlayer.stop();
                                myPlayer.release();
                                myPlayer = null;
                                showToast("Stopped playing ...");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audioplayicon);
                        //        play.setTextSize(11.00f);
                        record.setEnabled(true);
                    }
                }
            });
            record=(ImageView) findViewById(R.id.record);
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!recordFlag){
                        recordFlag=true;
                        progressBar.setVisibility(View.VISIBLE);
                        try {
                            //        initAudio();
                            outputFile = Environment.getExternalStorageDirectory().
                                    getAbsolutePath() + "/"+"my_audio"+new Date().getTime()+".3gp";
                            myAudioRecorder = new MediaRecorder();
                            myAudioRecorder.reset();
                            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                            myAudioRecorder.setOutputFile(outputFile);
                            seekBar.setVisibility(View.INVISIBLE);
                            myAudioRecorder.prepare();
                            myAudioRecorder.start();
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        record.setImageResource(R.drawable.audiorecordstopicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(false);
                        showToast("Recording started");
                    }else{
                        recordFlag=false;
                        progressBar.setVisibility(View.INVISIBLE);
                        myAudioRecorder.stop();
                        myAudioRecorder.release();
                        myAudioRecorder = null;

                        record.setImageResource(R.drawable.audiorecordicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(true);
                        showToast("Audio recorded");
                    }

                }
            });
            sendAudio=(TextView)findViewById(R.id.sendAudio);
            font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Regular.ttf");
            sendAudio.setTypeface(font);
            sendAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    audioFile = new File(outputFile);
                    Log.d("FILExtension===>", FilenameUtils.getExtension(audioFile.getName()));

                    if(audioFile != null && FilenameUtils.getExtension(audioFile.getName()).length()>0) {
      //                  showUploadAlert(Uri.fromFile(f));
                        record.setImageResource(R.drawable.audiorecordicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(true);
                        record.setEnabled(true);
                        seekBar.setVisibility(View.INVISIBLE);
                        recordFlag=false;
                        nameBox.setVisibility(View.VISIBLE);
                        boxBackground.setVisibility(View.VISIBLE);
                    }
                    else showToast("Please record audio.");
                }
            });
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

    public void uploadAudioInfo() {
        String url = ReqConst.SERVER_URL + "uploadStoryInfo";
        progressBar2.setVisibility(View.VISIBLE);

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
                progressBar2.setVisibility(View.GONE);
                showToast("Server issue");

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("member_id", String.valueOf(Commons.thisUser.getIdx()));
                params.put("member_name", Commons.thisUser.getName());
                params.put("title", inputBox.getText().toString().trim());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);
    }

    int storyId = 0;

    public void parseRestUrlsResponse(String json) {

        try {

            JSONObject response = new JSONObject(json);   Log.d("response=====> :",response.toString());

            String success = response.getString("result_code");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                storyId = response.getInt("story_id");
                uploadAudioFile(storyId);
            } else if(success.equals("101")){
                progressBar2.setVisibility(View.GONE);
                showToast("Someone is using the same title. Try again with another title.");
            }
            else {
                progressBar2.setVisibility(View.GONE);
                showToast("Server issue");
            }

        } catch (JSONException e) {
            progressBar2.setVisibility(View.GONE);
            showToast("Server issue");
            e.printStackTrace();
        }
    }

    private void uploadAudioFile(int storyId) {
        try {

            final Map<String, String> params = new HashMap<>();
            params.put("story_id", String.valueOf(storyId));

            String url = ReqConst.SERVER_URL + "uploadAudioFile";

            MultiPartRequest reqMultiPart = new MultiPartRequest(url, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar2.setVisibility(View.GONE);
                    showToast("Server issue");
                }
            }, new Response.Listener<String>() {

                @Override
                public void onResponse(String json) {

                    ParseUploadAudioFileResponse(json);
                }
            }, audioFile, "file", params);

            reqMultiPart.setRetryPolicy(new DefaultRetryPolicy(
                    Constants.VOLLEY_TIME_OUT, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            SobrietyApplication.getInstance().addToRequestQueue(reqMultiPart, url);

        } catch (Exception e) {
            e.printStackTrace();
            progressBar2.setVisibility(View.GONE);
            showToast("Server issue");
        }
    }

    private void ParseUploadAudioFileResponse(String json) {
        progressBar2.setVisibility(View.GONE);

        try {
            JSONObject response = new JSONObject(json);
            int result_code = response.getInt("result_code");

            if (result_code == 0) {
                showToast("Uploaded");
                finish();
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Server issue");
        }
    }
}








































