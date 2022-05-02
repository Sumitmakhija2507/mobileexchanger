package com.cretenix.mobileexchanger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private boolean doubleTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;

        mWebView = findViewById(R.id.activity_main_webview);
        assert mWebView != null;

        mWebView.setWebViewClient(new Callback());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mWebView.loadUrl("https://newme.cashmobex.com/");

        final String token = MyFirebaseMessagingService.onTokenRefresh(context);


        AndroidNetworking.get("https://newme.cashmobex.com/home/deviceid?id="+token+"&type=android")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("response", response.toString());
                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("response error", token + error.toString());
                    }
                });
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
            // webview.setVisibility(webview.INVISIBLE);
            if (Build.VERSION.SDK_INT >= 11){
                webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //view.loadUrl(url);
            if (url != null && url.contains("whatsapp://")) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            } else {
                return false;
            }
        }

        public void onPageFinished(WebView view, String url) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Actions to do after 10 seconds
                    findViewById(R.id.imageView).setVisibility(View.GONE);
                    findViewById(R.id.activity_main_webview).setVisibility(View.VISIBLE);
                }
            }, 2000);
            //hide loading image
        }
    }


    @Override
    public void onBackPressed()
    {
        if (mWebView.canGoBack())
        {
            mWebView.goBack();
        }
        else if (doubleTap)
        {
            super.onBackPressed();
        }
        else
        {
            Toast toast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            doubleTap = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    doubleTap = false;
                }
            }, 2000);
        }
    }
}
