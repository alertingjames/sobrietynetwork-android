package com.sobriety.sobriety.main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.iceteck.silicompressorr.SiliCompressor;
import com.sobriety.sobriety.R;
import com.sobriety.sobriety.SobrietyApplication;
import com.sobriety.sobriety.classes.AndroidMultiPartEntity;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.commons.Constants;
import com.sobriety.sobriety.commons.ReqConst;
import com.sobriety.sobriety.models.User;
import com.sobriety.sobriety.utils.MultiPartRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddVideoStoryActivity extends AppCompatActivity {

    private static final String TAG ="File===>" ;
    VideoView videoView;
    RelativeLayout ui_lytvideo;
    String selectedVideoPath="",takenVideoPath="";
    private Uri fileUri;
    LinearLayout compressionMsg, lytProgress;
    Uri videoUri = null;
    Bitmap thumb = null;
    String compressedvideoUrl = "";
    ProgressBar progressBar, vProgressBar;
    EditText inputBox;
    FrameLayout nameBox;
    TextView cancelBoxButton, sendButton, txtPercentage;
    View boxBackground;
    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_add_video_story);

        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        inputBox = (EditText)findViewById(R.id.inputBox);
        nameBox = (FrameLayout)findViewById(R.id.nameBox);
        cancelBoxButton = (TextView)findViewById(R.id.cancel_button);
        sendButton = (TextView)findViewById(R.id.send_button);
        boxBackground = (View)findViewById(R.id.layout);
        lytProgress=(LinearLayout)findViewById(R.id.lyt_progress);
        vProgressBar = (ProgressBar)findViewById(R.id.progressBar3);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);

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
                    try {
                        Submit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "Comfortaa_Bold.ttf");
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        ((TextView)findViewById(R.id.title)).setTypeface(font);
        ((TextView)findViewById(R.id.boxTitle)).setTypeface(font);

        compressionMsg = (LinearLayout) findViewById(R.id.compressionMsg);

        ui_lytvideo=(RelativeLayout) findViewById(R.id.lytvideo);
        videoView=(VideoView) findViewById(R.id.videoView);
        dispatchTakeVideoIntent();
    }

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int SELECT_VIDEO_REQUEST=2;

    private void dispatchTakeVideoIntent() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {

            takenVideoPath = getPath(data.getData());

            videoUri = data.getData();
            thumb = ThumbnailUtils.createVideoThumbnail(takenVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
            videoView.setBackground(new BitmapDrawable(getResources(), thumb));
            videoView.setVideoURI(videoUri);
        }

        else if(requestCode == SELECT_VIDEO_REQUEST && resultCode == RESULT_OK)
        {
            if(data.getData()!=null)
            {
                selectedVideoPath = getPath(data.getData());
                try{
                    videoUri = data.getData();
                    thumb = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                    videoView.setBackground(new BitmapDrawable(getResources(),thumb));//MediaStore.Video.Thumbnails.MINI_KIND
                    videoView.setVideoURI(videoUri);

                }catch (NullPointerException e){

                }

            }
            else
            {
                Toast.makeText(getApplicationContext(), "Failed to select video" , Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
    public void selectVideoFromGallery()
    {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_VIDEO_REQUEST);
    }

    public void previewVideo(View view){
        videoView.setBackground(null);
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
    }

    public void submitVideo(View view){
        if(videoUri != null){
            nameBox.setVisibility(View.VISIBLE);
            boxBackground.setVisibility(View.VISIBLE);
        }else showToast("Please record a video.");
    }

    File thumbnailFile = null;

    private void Submit() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        thumbnailFile = new File(Environment.getExternalStorageDirectory()+"/Pictures",
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            thumbnailFile.createNewFile();
            fo = new FileOutputStream(thumbnailFile);
            fo.write(byteArrayOutputStream.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create destination directory
        File f2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/media/videos");
        if (f2.mkdirs() || f2.isDirectory())
            //compress and output new video specs
            new VideoCompressAsyncTask(this).execute(videoUri.toString(), f2.getPath());
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            compressionMsg.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                filePath = SiliCompressor.with(mContext).compressVideo(Uri.parse(paths[0]), paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return  filePath;

        }

        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;
            if(length >= 1024)
                value = length/1024f+" MB";
            else
                value = length+" KB";
            String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video compression complete", imageFile.getName(), value);
            compressionMsg.setVisibility(View.GONE);
            Log.i("Silicompressor", "Path: "+compressedFilePath);
            Log.d("Video info===>", text);
            compressedvideoUrl = compressedFilePath;
            uploadVideoInfo();
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

    public void uploadVideoInfo() {
        String url = ReqConst.SERVER_URL + "uploadStoryInfo";
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
                params.put("member_name", Commons.thisUser.getName());
                params.put("title", inputBox.getText().toString().trim());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SobrietyApplication.getInstance().addToRequestQueue(post, url);

    }

    int videoId = 0;

    public void parseRestUrlsResponse(String json) {

        try {

            JSONObject response = new JSONObject(json);   Log.d("response=====> :",response.toString());

            String success = response.getString("result_code");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {
                videoId = response.getInt("story_id");
                uploadVideoThumbnail(videoId);
            } else if(success.equals("101")){
                progressBar.setVisibility(View.GONE);
                showToast("Someone is using the same title. Try again with another title.");
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

    public void uploadVideoThumbnail(int id) {
        try {

            final Map<String, String> params = new HashMap<>();
            params.put("video_id", String.valueOf(id));

            String url = ReqConst.SERVER_URL + "uploadVideoThumbnail";

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
                }
            }, thumbnailFile, "file", params);

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
                lytProgress.setVisibility(View.VISIBLE);
                new UploadFileToServer().execute();
            }
            else {
                showToast("Server issue");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Server issue");
        }
    }

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            vProgressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            vProgressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            vProgressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            String url = ReqConst.SERVER_URL + "uploadVideoFile";
            HttpPost httppost = new HttpPost(url);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(compressedvideoUrl);

                // Adding file data to http body
                entity.addPart("video_id", new StringBody(String.valueOf(videoId)));
                entity.addPart("file", new FileBody(sourceFile));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);    Log.d("Response===>",response.toString());
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();  Log.d("VideoStatus===>",String.valueOf(statusCode));
                if (statusCode == 100) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("AddVideoStoryActivity", "Response from server: " + result);

            // showing the server response in an alert dialog
            Toast.makeText(getApplicationContext(), "Uploaded.", Toast.LENGTH_SHORT).show();
            lytProgress.setVisibility(View.GONE);
            compressedvideoUrl="";
            finish();
            super.onPostExecute(result);
        }

    }
}






































