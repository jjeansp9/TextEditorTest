package com.example.texteditortest.network

import com.example.texteditortest.model.BoardData
import com.example.texteditortest.model.response.BaseResponse
import com.example.texteditortest.model.response.BoardResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RetrofitApi {
    // Editor 글 등록
    @POST("${ApiUrl.PREFIX}richText")
    suspend fun insertBoard(
        @Body info : BoardData
    ) : Response<BaseResponse>

    // Editor 글 조회
    @GET("${ApiUrl.PREFIX}richTexts")
    suspend fun getBoardList(
//        @Query("seq") seq : Long
    ) : Response<BoardResponse>
}