package com.example.texteditortest.network

import com.example.texteditortest.model.BoardData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitApi {
    // Editor 글 등록
    @POST("${ApiUrl.PREFIX}/richText")
    fun insertBoard(
        @Part("body") info : BoardData
    ) : Response<BoardData>
}