package com.example.texteditortest.model.response

import com.example.texteditortest.model.BoardData

data class BoardResponse(
    val msg : String = "",
    val data : List<BoardData>? = null
)
