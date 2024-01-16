package com.example.texteditortest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.texteditortest.model.BoardData

class MainViewModel : ViewModel() {
    val _boardItem = MutableLiveData<ArrayList<BoardData>?>()
    val boardItem : LiveData<ArrayList<BoardData>?> = _boardItem

    fun getListData() {
        val data : ArrayList<BoardData> = arrayListOf()
        for (i in 0 .. 10) data.add(BoardData("제목 테스트", "내용 테스트"))

        _boardItem.postValue(data)
    }
}