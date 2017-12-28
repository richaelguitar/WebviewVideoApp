package com.richaelguitar.webviewvideoapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.richaelguitar.webviewvideoapp.service.WebViewService;


public class WebViewActivity extends AppCompatActivity {

    public static final String URL = "https://my.tv.sohu.com/pl/9058124/96333096.shtml";

    private WebView webView;
    private Toolbar toolbar;
    private IWebviewService webviewService;//todo 用于同步用户cookie
    private boolean isBind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webview);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        initWebViewSetting();
        webView.loadUrl(URL);
    }

    @Override
    protected void onStart() {
        super.onStart();
       if(!isBind){
           bindService(new Intent(this, WebViewService.class),serviceConnection,BIND_AUTO_CREATE);
       }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            webviewService = IWebviewService.Stub.asInterface(iBinder);
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBind = false;
            webviewService = null;
        }
    };

    private void initWebViewSetting() {

        WebSettings settings = webView.getSettings();
        // 缓存(cache)
        settings.setAppCacheEnabled(true);      // 默认值 false
        settings.setAppCachePath(getCacheDir().getAbsolutePath());

        // 存储(storage)
        settings.setDomStorageEnabled(true);    // 默认值 false
        settings.setDatabaseEnabled(true);      // 默认值 false

        // 是否支持viewport属性，默认值 false
        // 页面通过`<meta name="viewport" ... />`自适应手机屏幕
        settings.setUseWideViewPort(true);
        // 是否使用overview mode加载页面，默认值 false
        // 当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
        settings.setLoadWithOverviewMode(true);

        // 是否支持Javascript，默认值false
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // 5.0以上允许加载http和https混合的页面(5.0以下默认允许，5.0+默认禁止)
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());

    }

    private class  MyWebViewClient extends WebViewClient {
        public MyWebViewClient() {
            super();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view != null&&webviewService!=null) {
                try {
                    toolbar.setTitle(webviewService.getUserInfo().getUserName()+":"+view.getTitle());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            super.onPageFinished(view, url);
        }
    }


    private class  MyWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            FrameLayout frameLayout = (FrameLayout) getWindow().getDecorView();
            frameLayout.addView(mCustomView);
            mCustomViewCallback = callback;
            webView.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            mCustomView.setVisibility(View.GONE);
            FrameLayout frameLayout = (FrameLayout) getWindow().getDecorView();
            frameLayout.removeView(mCustomView);
            mCustomViewCallback.onCustomViewHidden();
            mCustomView = null;
            toolbar.setVisibility(View.VISIBLE);
            webView.setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(webView!=null){
            webView.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBind&&serviceConnection!=null){
            unbindService(serviceConnection);
            isBind =false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(webView!=null){
            webView.onPause();
        }
    }

    @Override
    public void onBackPressed() {

        if(webView!=null&&webView.canGoBack()){
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(webView!=null){
            webView.destroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                toolbar.setVisibility(View.GONE);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                toolbar.setVisibility(View.VISIBLE);
                break;
        }
    }
}
