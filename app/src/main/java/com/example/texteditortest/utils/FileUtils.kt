package com.example.texteditortest.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    const val TAG = "FileUtils"
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme
        if (scheme != null && scheme == ContentResolver.SCHEME_CONTENT) {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    LogMgr.d(TAG, "DISPLAY_NAME : " + OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName
    }

    fun getFromPathUri(mContext: Context, uri: Uri) : String? {
        if (uri == null || mContext == null) return null
        return if ("content".equals(uri.scheme, ignoreCase = true)) {

            if (isGooglePhotosUri(uri)) uri.lastPathSegment // 구글포토에서 가져온 이미지인 경우
            else fromHeicToJpg(mContext, uri)

        } else {
            null
        }
    }

    fun fromHeicToJpg(mContext: Context, uri: Uri) : String? {
        val fileName = getFileNameFromUri(mContext, uri)
        if (fileName != null) {
            val file = convertToJpg(mContext, uri)
            return file?.absolutePath
        }
        return null
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri
            .authority
    }

    fun convertToJpg(mContext: Context, uri: Uri?): File? {
        // 임시 파일 생성
        val tempFile: File? = createTempFile(mContext, uri)
        try {
            if (tempFile == null) return null
            // heic 파일을 jpg로 변환하여 임시 파일에 저장
            val out = FileOutputStream(tempFile)
            var source: ImageDecoder.Source? = null
            var bitmap: Bitmap? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(
                    mContext.contentResolver,
                    uri!!
                )
                bitmap = ImageDecoder.decodeBitmap(source)
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(mContext.contentResolver, uri)
            }
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 변환된 jpg 파일의 Uri 반환
        return tempFile
    }

    fun createTempFile(mContext: Context, uri: Uri?): File? {
        try {
            if (uri == null) return null

            val tempFileName = "temp.jpg"
            val storageDir = mContext.cacheDir
            val tempFile = File.createTempFile(tempFileName, null, storageDir)
            val fileName: String? = getFileNameFromUri(mContext, uri)
            if (fileName != null) {
                val renamedFile: File
                renamedFile = if (fileName.contains(".heic")) {
                    File(tempFile.parent, fileName.replace(".heic", ".jpg"))
                } else {
                    File(tempFile.parent, fileName)
                }
                if (tempFile.renameTo(renamedFile)) {
                    return renamedFile
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }
}