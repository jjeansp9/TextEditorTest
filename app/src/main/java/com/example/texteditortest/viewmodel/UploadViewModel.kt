package com.example.texteditortest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.texteditortest.model.BoardData
import com.example.texteditortest.network.ApiUrl
import com.example.texteditortest.network.ResponseCode
import com.example.texteditortest.network.RetrofitApi
import com.example.texteditortest.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadViewModel : ViewModel() {
    private val _resultCode = MutableLiveData<Int>()
    val resultCode : LiveData<Int> = _resultCode
    suspend fun insertBoard(str: String) {

        val board = BoardData()

        board.content = str

        withContext(Dispatchers.IO) {
            val retrofit: RetrofitApi? = RetrofitClient.getRetrofitInstance(ApiUrl.BASE_URL).create(RetrofitApi::class.java)
            val result = retrofit?.insertBoard(board)

            withContext(Dispatchers.Main) {
                if (result != null && result.isSuccessful) {
                    _resultCode.value = result.code()
                } else {
                    _resultCode.value = ResponseCode.FAIL
                }
            }
        }
    }
}