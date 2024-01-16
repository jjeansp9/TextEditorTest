package com.example.texteditortest.common;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.texteditortest.utils.LogMgr;
import com.uniquext.android.rxlifecycle.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = "MyWebChromeClient";

    private Dialog dialog;
    private WebView newWebView;
    private AppCompatActivity activity;


    private String mCM = null;
    private ValueCallback<Uri> mUM = null;
    private ValueCallback<Uri[]> mUMA = null;
    private int FCR = 1000;

    public MyWebChromeClient(AppCompatActivity mActivity) {
        this.activity = mActivity;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        LogMgr.e("WebView", "Event!! create");
        newWebView = new WebView(view.getContext());
        WebSettings webSettings = newWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);

        // 파일 허용
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBlockNetworkImage(true);
        webSettings.setBlockNetworkLoads(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setLoadsImagesAutomatically(true);

        newWebView.setWebViewClient(new MyWebViewClient(activity, newWebView));
        newWebView.setWebChromeClient(new MyWebChromeClient(activity) {
            @Override
            public void onCloseWindow(WebView window) {
                LogMgr.e("WebView", "Event!! close2");

                if (dialog != null) dialog.dismiss();
                if (newWebView != null) newWebView.destroy();
                if (window != null) window.destroy();

                super.onCloseWindow(window);
            }
        });
        activity.setContentView(newWebView);
        //dialog = new Dialog(view.getContext());

//            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//
//            dialog.getWindow().setAttributes(params);
//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialogInterface) {
//                    if (newWebView != null) newWebView.destroy();
//                }
//            });
//            dialog.show();

        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();

        return true;
    }

    //ssl 인증이 없는 경우 해결을 위한 부분
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        super.onPermissionRequest(request);
        request.grant(request.getResources());
    }

    //        @Override
//        public void onCloseWindow(WebView window) {
//            LogMgr.e("testWebView", "Event!! close");
//
//            if (dialog != null) dialog.dismiss();
//            if (newWebView != null) newWebView.destroy();
//            if (window != null) window.destroy();
//        }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        LogMgr.e(TAG, "onJsAlert() url ["+url+"], msg ="+message);
        new AlertDialog.Builder(view.getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();

        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        super.onCloseWindow(window);
    }


}
