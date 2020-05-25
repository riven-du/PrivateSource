package cn.com.quick.media

import android.content.Context
import android.provider.MediaStore
import java.lang.Exception

/**
 * Date :2020/5/20 11:30
 * Description:
 * History
 */
object MediaController {

    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val QUERY_BY = "${MediaStore.Files.FileColumns._ID} DESC"
    private val NOT_GIF_UNKNOWN = "!='image/*'"
    private val NOT_GIF = "!='image/gif' AND ${MediaStore.MediaColumns.MIME_TYPE}${NOT_GIF_UNKNOWN}"
    private val GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id"
    private val COLUMN_COUNT = "count"
    private val COLUMN_BUCKET_ID = "bucket_id"
    private val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"



    fun queryAllImage(context: Context) {
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        try {
            val cursor = context.contentResolver.query(QUERY_URI, projection, null, null, null)
            cursor?.moveToNext()
            cursor?.close()
        } catch (e: Exception) {

        }

    }
}