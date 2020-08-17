package com.kiatech.kia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebAccessActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_access);

        webView = (WebView)findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setScrollbarFadingEnabled(false);

        webView.getSettings().setUserAgentString("Chrome/41.0.2228.0");

        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "ButtonRecogniser");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                loadEvent(clickListener());
            }

            void loadEvent(String javascript){
                webView.loadUrl("javascript:"+javascript);
            }

            String clickListener(){
                return getButtons()+"for(var i=0 ; i<buttons.length ; i++){ buttons[i].onclick = function(){ console.log('click worked!'); ButtonRecogniser.boundMethod('button clicked'); }; }";
            }

            String getButtons(){
                return "var buttons = document.getElementsByClassName('gNO89b'); console.log(buttons);";
            }
        });

        webView.loadUrl("https://www.google.com/");
    }

    class MyJavaScriptInterface{
        Context context;

        MyJavaScriptInterface(Context context){
            this.context = context;
        }

        @JavascriptInterface
        public void boundMethod(String html){
            new AlertDialog.Builder(context).setTitle("HTML").setMessage("It Worked!")
                    .setPositiveButton("OK", null).setCancelable(false).create().show();
        }
    }

}