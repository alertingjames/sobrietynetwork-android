package com.sobriety.sobriety.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;
import com.sobriety.sobriety.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AudioPlayActivity extends AppCompatActivity {

    private MediaPlayer myPlayer = null;
    private String outputFile = "";
    private Handler mHandler = new Handler();

    SeekBar seekBar;
    ProgressBar progressBar;
    ImageView play;
    ImageButton audioRestartButton;
    public static final boolean NATIVE_WEB_P_SUPPORT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final int RECORD_AUDIO = 0;
    GifView gifView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_audio_play);

        String title = getIntent().getStringExtra("title");
        String audioUrl = getIntent().getStringExtra("audioUrl");

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title)).setText(title);

        gifView = (GifView) findViewById(R.id.gif);
        gifView.play();
        outputFile = audioUrl;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);

        } else {
            seekBar = (SeekBar)findViewById(R.id.seekBar);
            play = (ImageView) findViewById(R.id.playButton);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!myPlayer.isPlaying()){
                        try {
                            myPlayer.start();
                            seekBar.setVisibility(View.VISIBLE);
                            seekBar.setMax(myPlayer.getDuration());
                            gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                            AudioPlayActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(myPlayer != null){
                                        int mCurrentPosition = myPlayer.getCurrentPosition();
                                        seekBar.setProgress(mCurrentPosition);
                                        if(mCurrentPosition>=seekBar.getMax()){
                                            play.setImageResource(R.drawable.audioplayicon);
                                            seekBar.setVisibility(View.INVISIBLE);
                                            gifView.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    mHandler.postDelayed(this, 1000);
                                }
                            });

                            showToast("Resuming audio");

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audiostopicon);
                    }else {
                        gifView.setVisibility(View.INVISIBLE);
                        try {
                            if (myPlayer != null) {
                                myPlayer.pause();
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audioplayicon);
                    }
                }
            });

            audioRestartButton = (ImageButton)findViewById(R.id.startButton);
            audioRestartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(myPlayer.isPlaying()) myPlayer.stop();
                    myPlayer.release();
                    myPlayer = null;
                    initPlay();
                }
            });

            initPlay();
        }
    }

    private void initPlay(){
        if(myPlayer == null){
            try {
                myPlayer = new MediaPlayer();
                myPlayer.setDataSource(outputFile);
                myPlayer.prepare();
                myPlayer.start();
                seekBar.setVisibility(View.VISIBLE);
                seekBar.setMax(myPlayer.getDuration());
                gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                AudioPlayActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(myPlayer != null){
                            int mCurrentPosition = myPlayer.getCurrentPosition();
                            seekBar.setProgress(mCurrentPosition);
                            if(mCurrentPosition>=seekBar.getMax()){
                                play.setImageResource(R.drawable.audioplayicon);
                                seekBar.setVisibility(View.INVISIBLE);
                                gifView.setVisibility(View.INVISIBLE);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(myPlayer.isPlaying()) myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
    }
}




























