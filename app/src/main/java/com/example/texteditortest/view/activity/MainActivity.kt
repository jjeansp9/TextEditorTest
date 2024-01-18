package com.example.texteditortest.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.texteditortest.R
import com.example.texteditortest.common.MyWebViewClient
import com.example.texteditortest.databinding.ActivityMainBinding
import com.example.texteditortest.network.ResponseCode
import com.example.texteditortest.utils.LogMgr
import com.example.texteditortest.view.adapter.BoardAdapter
import com.example.texteditortest.viewmodel.MainViewModel
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    companion object { private const val TAG = "MainActivity" }
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mAdapter : BoardAdapter

    private var mCM: String? = null
    private var mUM: ValueCallback<Uri?>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private val FCR = 1

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {

            }
        } else {
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vmMain = viewModel
        binding.lifecycleOwner = this
        initData()
        initView()
        onClick()
        observe()
        checkPermissions()
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
        //binding.wv.addJavascriptInterface(getEditData(::onReceiveJavascript), "Android") // HTML 문서에도 동일하게 작성 window.Android....

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

        binding.wv.loadUrl("http://192.168.2.55:1212/avad/test/richTexts/ui")

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
                if (takePictureIntent!!.resolveActivity(this@MainActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
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
    private fun initData() {
        lifecycleScope.launch{ binding.vmMain?.getListData() }
    }
    private fun initView() {
        mAdapter = BoardAdapter(this, onItemClick = {
            if (it.content != null) {
                Toast.makeText(this, it.content, Toast.LENGTH_SHORT).show()
            }
        })
        binding.recycler.adapter = mAdapter
        binding.recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun onClick() {
        binding.boardAdd.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            resultLauncher.launch(intent)
            //startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            if (binding.wv.canGoBack()) binding.wv.goBack()
            else finish()
        }
    }

    private fun observe() {
        binding.vmMain?.boardItem?.observe(this) {
            if (it != null && it.isNotEmpty()) mAdapter.submitList(it)
            LogMgr.e(TAG, "data: " + it?.size)
        }
        binding.vmMain?.resultCode?.observe(this) {
            when(it) {
                ResponseCode.SUCCESS -> {

                }
                ResponseCode.BINDING_ERROR -> {
                    Toast.makeText(this, "데이터를 불러오는 방법이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                }
                ResponseCode.NOT_FOUND -> {
                    Toast.makeText(this, "데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                ResponseCode.FAIL -> {
                    Toast.makeText(this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissions() {
        var requiredPermissions: Array<String?>? = null
        requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }
            })
            .setPermissions(*requiredPermissions)
            .setDeniedMessage(getString(R.string.msg_intro_denied_permission))
            .setDeniedCloseButtonText(getString(R.string.title_permission_close))
            .setGotoSettingButton(true)
            .setGotoSettingButtonText(getString(R.string.title_permission_setting))
            .check()

//        String packageName = getPackageName();
//        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//        intent.setData(Uri.parse("package:"+packageName));
//        resultLauncher.launch(intent);
    }

    override fun onKeyDown(keyCode: Int, @NonNull event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (binding.wv.canGoBack()) binding.wv.goBack()
                    else finish()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}