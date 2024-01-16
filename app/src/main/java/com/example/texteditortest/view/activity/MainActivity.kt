package com.example.texteditortest.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.texteditortest.R
import com.example.texteditortest.databinding.ActivityMainBinding
import com.example.texteditortest.network.ResponseCode
import com.example.texteditortest.utils.LogMgr
import com.example.texteditortest.view.adapter.BoardAdapter
import com.example.texteditortest.viewmodel.MainViewModel
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    companion object { private const val TAG = "MainActivity" }
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mAdapter : BoardAdapter

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {

            }
        } else {
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
}