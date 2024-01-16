package com.example.texteditortest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.texteditortest.model.BoardData
import com.example.texteditortest.network.ApiUrl
import com.example.texteditortest.network.RetrofitApi
import com.example.texteditortest.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _boardItem = MutableLiveData<List<BoardData>?>()
    val boardItem : LiveData<List<BoardData>?> = _boardItem

    private val _resultCode = MutableLiveData<Int>()
    val resultCode : LiveData<Int> = _resultCode

    suspend fun getListData() {
        withContext(Dispatchers.IO) {
            val retrofit: RetrofitApi? = RetrofitClient.getRetrofitInstance(ApiUrl.BASE_URL).create(
                RetrofitApi::class.java)
            var getData : List<BoardData>? = null
            val result = retrofit?.getBoardList()
            if (result != null && result.isSuccessful) {
                getData = result.body()?.data
            }

            withContext(Dispatchers.Main) {
                if (getData != null) {
                    _boardItem.postValue(getData)
                }
            }
        }
    }
}