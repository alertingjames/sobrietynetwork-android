package com.sobriety.sobriety;

/**
 * Created by andre on 8/14/2018.
 */

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.sobriety.sobriety.utils.LruBitmapCache;

/**
 * Created by sonback123456 on 12/17/2017.
 */

public class SobrietyApplication extends MultiDexApplication {
    public static final String TAG = SobrietyApplication.class.getSimpleName();

    public RequestQueue _requestQueue;
    public ImageLoader _imageLoader;

    private String m_gsmToken = "";

    private static final String LOG_TAG = "CrashCatch";

    private static SobrietyApplication _instance;

    public static SobrietyApplication getApplication() {
        return _instance;
    }

    @Override

    public void onCreate(){

        super.onCreate();
        _instance = this;

        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(this);

        startCatcher();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public String getGcmToken() {
        return m_gsmToken;
    }

    public void setGcmToken(String p_strGsmToken) {
        m_gsmToken = p_strGsmToken;
    }



    public static synchronized SobrietyApplication getInstance(){

        return _instance;
    }

    public RequestQueue getRequestQueue(){

        if(_requestQueue == null){
            _requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return _requestQueue;
    }

    public ImageLoader getImageLoader(){

        getRequestQueue();
        if(_imageLoader == null){
            _imageLoader = new ImageLoader(this._requestQueue, new LruBitmapCache());
        }
        return this._imageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag){

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (_requestQueue != null) {
            _requestQueue.cancelAll(tag);
        }
    }

    // Crash

    private void startCatcher() {
        Thread.UncaughtExceptionHandler systemUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler();
        // the following handler is used to catch exceptions thrown in background threads
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler(new Handler()));

        while (true) {
            try {
                Log.v(LOG_TAG, "Starting crash catch Looper");
                Looper.loop();
                Thread.setDefaultUncaughtExceptionHandler(systemUncaughtHandler);
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (BackgroundException e) {
                Log.e(LOG_TAG, "Caught the exception in the background thread " + e.threadName + ", TID: " + e.tid, e.getCause());
                showCrashDisplayActivity(e.getCause());
            } catch (Throwable e) {
                Log.e(LOG_TAG, "Caught the exception in the UI thread, e:", e);
                showCrashDisplayActivity(e);
            }
        }
    }

    void showCrashDisplayActivity(Throwable e) {
//        Intent i = new Intent(this, CrashDisplayActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.putExtra("e", e);
//        startActivity(i);
    }


    /**
     * This handler catches exceptions in the background threads and propagates them to the UI thread
     */
    static class UncaughtHandler implements Thread.UncaughtExceptionHandler {

        private final Handler mHandler;

        UncaughtHandler(Handler handler) {
            mHandler = handler;
        }

        public void uncaughtException(Thread thread, final Throwable e) {
            Log.v(LOG_TAG, "Caught the exception in the background " + thread + " propagating it to the UI thread, e:", e);
            final int tid = Process.myTid();
            final String threadName = thread.getName();
            mHandler.post(new Runnable() {
                public void run() {
                    throw new BackgroundException(e, tid, threadName);
                }
            });
        }
    }

    /**
     * Wrapper class for exceptions caught in the background
     */
    static class BackgroundException extends RuntimeException {

        final int tid;
        final String threadName;

        /**
         * @param e original exception
         * @param tid id of the thread where exception occurred
         * @param threadName name of the thread where exception occurred
         */
        BackgroundException(Throwable e, int tid, String threadName) {
            super(e);
            this.tid = tid;
            this.threadName = threadName;
        }
    }
}


