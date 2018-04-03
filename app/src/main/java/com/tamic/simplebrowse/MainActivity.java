package com.tamic.simplebrowse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.widget.Toast;

import com.tamic.jswebview.browse.CallBackFunction;
import com.tamic.jswebview.browse.JsWeb.CustomWebViewClient;
import com.tamic.jswebview.browse.JsWeb.JavaCallHandler;
import com.tamic.jswebview.browse.JsWeb.JsHandler;
import com.tamic.jswebview.view.ProgressBarWebView;

import java.util.ArrayList;
import java.util.Map;

/**
 *  Email: 2185134304@qq.com
 *  Create JackChen 2018/4/3 11:11
 *  Version:
 *  Params:
 *  Description:   基于JsBridge封装的高效自带加载进度条的WebView
 */
public class MainActivity extends AppCompatActivity {

    // WebView
    private ProgressBarWebView mProgressBarWebView;
    // 用于存放Js代码中的所有方法名
    private ArrayList<String> mHandlers = new ArrayList<>();


    ValueCallback<Uri> mUploadMessage;
    private static CallBackFunction mfunction;

    int RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWebView();
    }

    private void initWebView() {

        mProgressBarWebView = (ProgressBarWebView) findViewById(R.id.login_progress_webview);
        mProgressBarWebView.setWebViewClient(new CustomWebViewClient(mProgressBarWebView.getWebView()) {

            @Override
            public String onPageError(String url) {
                //指定网络加载失败时的错误页面
                return "file:///android_asset/error.html";
            }

            @Override
            public Map<String, String> onPageHeaders(String url) {

                // 可以加入header
                return null;
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }
        });

        // 打开页面，也可以支持网络url
        mProgressBarWebView.loadUrl("file:///android_asset/demo.html");



        mHandlers.add("login");
        mHandlers.add("callNative");
        mHandlers.add("callJs");
        mHandlers.add("open");

        //回调js的方法     handlerName：Js中写的方法  responseData：Js返回的数据  function.onCallBack("我在上海")：回调给Js的数据
        mProgressBarWebView.registerHandlers(mHandlers, new JsHandler() {
            @Override
            public void OnHandler(String handlerName, String responseData, CallBackFunction function) {

                // 点击WebView中 登录 （Js中定义的login()方法）
                if (handlerName.equals("login")) {
                    Toast.makeText(MainActivity.this, responseData, Toast.LENGTH_SHORT).show();

                } else if (handlerName.equals("callNative")) {

                    Toast.makeText(MainActivity.this, responseData, Toast.LENGTH_SHORT).show();
                    function.onCallBack("我在上海");


                // 点击调用native
                } else if (handlerName.equals("callJs")) {

                    Toast.makeText(MainActivity.this, responseData, Toast.LENGTH_SHORT).show();

                    // 想调用你的方法：
                    function.onCallBack("好的 这是图片地址 ：xxxxxxx");


                // 点击选择相册
                } if (handlerName.equals("open")) {

                    mfunction = function;
                    pickFile();
                }

            }
        });

        // 首先显示WebView，然后调用Js中的callNative()方法，jsResponseData是Js返回来的数据
        mProgressBarWebView.callHandler("callNative", "hello H5, 我是java", new JavaCallHandler() {
            @Override
            public void OnHandler(String handlerName, String jsResponseData) {
                Toast.makeText(MainActivity.this, "h5返回的数据：" + jsResponseData, Toast.LENGTH_SHORT).show();
            }
        });

        //发送消息给js
        mProgressBarWebView.send("哈喽", new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

            mfunction.onCallBack(intent.getData().toString());

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 防止内存泄露
        if (mProgressBarWebView.getWebView() != null) {
            mProgressBarWebView.getWebView().destroy();
        }
    }
}
