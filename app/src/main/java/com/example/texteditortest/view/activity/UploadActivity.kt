package com.example.texteditortest.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.NonNull
import com.example.texteditortest.R
import com.example.texteditortest.common.MyWebViewClient
import com.example.texteditortest.databinding.ActivityUploadBinding
import com.example.texteditortest.utils.LogMgr
import com.example.texteditortest.viewmodel.UploadViewModel
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date


class UploadActivity : BaseActivity<ActivityUploadBinding>(R.layout.activity_upload) {

    companion object { private const val TAG = "UploadActivity" }
    private val viewModel: UploadViewModel by viewModels()
    var mResult: OnEditorResult? = null

    private var mCM: String? = null
    private var mUM: ValueCallback<Uri?>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val FCR = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA!!.onReceiveValue(results)
            mUMA = null
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return
                val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(result)
                mUM = null
            }
        }
    }

    private val imgLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                var results: Array<Uri>? = null
                if (null != mUMA) {
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
                mUMA!!.onReceiveValue(results)
                mUMA = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vmUpload = viewModel
        binding.lifecycleOwner = this

        onClick()
        setWeb()
    }

    @SuppressLint("SetJavaScriptEnabled", "WrongViewCast")
    private fun setWeb() {
        val webSettings = binding.wv.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.domStorageEnabled = true // DOM 스토리지 활성화
        webSettings.loadWithOverviewMode = true //html 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정되도록 설정
        webSettings.useWideViewPort = true //html viewport 메타태그 지원
        webSettings.setSupportMultipleWindows(true)
        webSettings.domStorageEnabled = true

        binding.wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.wv.addJavascriptInterface(getEditData(::onReceiveJavascript), "Android") // HTML 문서에도 동일하게 작성 window.Android....

        // 파일 허용
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess= true
        webSettings.mixedContentMode= WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.loadsImagesAutomatically= true

        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        val wvClient = MyWebViewClient(this, binding.wv)
        binding.wv.webViewClient = wvClient
        //binding.wv.webViewClient = Callback()

        binding.wv.loadUrl("http://192.168.2.55:1212/avad/test/richText/ui")

        binding.wv.webChromeClient = object : WebChromeClient() {
            //For Android 5.0+
            @SuppressLint("QueryPermissionsNeeded")
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@UploadActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e(TAG, "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?> = if (takePictureIntent != null) {
                    arrayOf(takePictureIntent)
                } else {
                    arrayOfNulls(0)
                }
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }
        }
    }

    private fun onClick() {
        binding.boardAdd.setOnClickListener {

            finish()
        }
        binding.btnBack.setOnClickListener { finish() }
    }

    class Callback : WebViewClient() {
        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            LogMgr.e(TAG+ "onReceivedError", description + ", " + view.url + ", " + view.title)
        }
    }

    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onKeyDown(keyCode: Int, @NonNull event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.wv.canGoBack()) {
                        binding.wv.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onReceiveJavascript(intValue: Int, stringValue: String) {
        Toast.makeText(this, "int: $intValue, string: $stringValue", Toast.LENGTH_SHORT).show()
    }

//    var editor = new FroalaEditor('#editor');

//    function fn_callBack_iOS() {
//        return editor.html.get();
//    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    interface OnEditorResult {
        fun onConfirm(address: String?)
    }

    private class getEditData(val onReceiveJavascript: (Int, String) -> Unit) {
        @JavascriptInterface
        fun messageFromWebToMobile(json: String?) {
            var jsonDecoded = ""
            if (json == null) {
                return
            } else {
                try {
                    jsonDecoded = URLDecoder.decode(json, "UTF-8")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onReceiveJavascript(data.intValue, data.stringValue)
        }
    }
}