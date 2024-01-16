package com.example.texteditortest.common;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.texteditortest.utils.LogMgr;

import java.net.URISyntaxException;


public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";

    private WebView wv;

    private AppCompatActivity activity;
    private AlertDialog mProgressDialog = null;

    private final String ADJUST_SCREEN_SIZE_JS_CODE = "javascript:var meta = document.createElement('meta'); meta.name = 'viewport'; meta.content = 'width=device-width, user-scalable = yes'; var header = document.getElementsByTagName('head')[0]; header.appendChild(meta)";
    public OnTextEditResult mResult;
    public interface OnTextEditResult {
        void onConfirm(String address);
    }

    public MyWebViewClient(AppCompatActivity mActivity, WebView webView) {
        this.activity = mActivity;
        this.wv = webView;
    }

    public void setUrl(WebView wv, String url, OnTextEditResult result) {
        this.mResult = result;
        wv.loadUrl(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://")) {
            if (request.getUrl().toString().startsWith("intent") && activity != null) {
                Intent schemeIntent;
                try {
                    schemeIntent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    activity.startActivity(schemeIntent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    String pkgName = schemeIntent.getPackage();
                    if (pkgName != null) {
                        activity.startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + pkgName)
                                )
                        );
                        return true;
                    }
                }
            } else {
                try {
                    if (activity != null) activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString())));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            view.loadUrl(request.getUrl().toString());
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogMgr.e(TAG, "onPageStarted() : " + url);
        //showProgressDialog();
        view.loadUrl(ADJUST_SCREEN_SIZE_JS_CODE);
        wv.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogMgr.e(TAG, "onLoadResource() : " + url);
        view.loadUrl(ADJUST_SCREEN_SIZE_JS_CODE);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        LogMgr.e(TAG, "onPageCommitVisible() : " + url);
        //if (url.contains(BUS_ROUTE_URL)) view.loadUrl(BUS_ROUTE_JS_CODE_MENU_CLEAR);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogMgr.e(TAG, "onPageFinished() : " + url);
        //hideProgressDialog();
        wv.setVisibility(View.VISIBLE);
        //view.loadUrl(url);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (error != null) {
            int errorCode = error.getErrorCode();
            if (error.getDescription() != null) {
                CharSequence description = error.getDescription();

                if(!description.toString().contains("ERR_CLEARTEXT_NOT_PERMITTED")) {
                    // onLoad할 때 network_config_xml에 선언하지 않은 http url 주소가 load될 때가 있음
                    LogMgr.e(TAG, "onReceivedError() Code: " + errorCode + ", Description: " + description);

                    if (activity != null && !activity.isFinishing()) {
                        //Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        // 오류가 났을 때 대체 페이지 로드
        //wv.loadUrl("");
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
//        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
//        builder.setMessage(activity.getString(R.string.received_ssl_error_msg));
//        builder.setPositiveButton(activity.getString(R.string.msg_continue), (dialog, which) -> handler.proceed());
//        builder.setNegativeButton(activity.getString(R.string.cancel), (dialog, whitch) -> handler.cancel());
//        final androidx.appcompat.app.AlertDialog dialog = builder.create();
//        dialog.show();
    }

    public class EditJavaScriptInterface {

        public OnTextEditResult mResult;

        public EditJavaScriptInterface(OnTextEditResult result) {
            mResult = result;
        }

        @JavascriptInterface
        public void processDATA(String str) {
            mResult.onConfirm(str);
        }
    }

//    private void showProgressDialog() {
//        if (mProgressDialog == null && activity != null && !activity.isFinishing()){
//            View view = activity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
//            TextView txt = view.findViewById(R.id.text);
//            txt.setText(activity.getString(R.string.requesting));
//
//            mProgressDialog = new AlertDialog.Builder(activity)
//                    .setCancelable(false)
//                    .setView(view)
//                    .create();
//            mProgressDialog.show();
//        }
//    }
//
//    private void hideProgressDialog() {
//        try {
//            if (mProgressDialog != null && activity != null && !activity.isFinishing()) {
//                mProgressDialog.dismiss();
//                mProgressDialog = null;
//            }
//        }catch (Exception e){
//            LogMgr.e("hideProgressDialog()", e.getMessage());
//        }
//    }
}
