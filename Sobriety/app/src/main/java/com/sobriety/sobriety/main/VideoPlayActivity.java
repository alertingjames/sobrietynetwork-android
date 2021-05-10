package com.sobriety.sobriety.main;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.sobriety.sobriety.R;

import java.io.File;

public class VideoPlayActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        String title = getIntent().getStringExtra("title");
        String videoUrl = getIntent().getStringExtra("videoUrl");

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.title)).setText(title);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        WebView video=(WebView) findViewById(R.id.video);

        video.setWebViewClient(new WebViewClient());
        video.getSettings().setJavaScriptEnabled(true);
        video.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        video.getSettings().setPluginState(WebSettings.PluginState.ON);
        video.getSettings().setMediaPlaybackRequiresUserGesture(true);
        video.setWebChromeClient(new WebChromeClient());
        video.loadUrl(videoUrl);
    }
}
























