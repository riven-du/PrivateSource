package cn.com.quick.media

import android.app.Activity
import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import cn.com.quick.utils.quickLogE
import java.lang.Exception
import kotlin.concurrent.thread

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

    // 过滤掉长度小于500毫秒的录音
    private val AUDIO_DURATION = 500

    // 单位
    private val FILE_SIZE_UNIT = 1024 * 1024L

    // image
    private val SELECTION = "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? ) AND ${MediaStore.MediaColumns.SIZE} >0) GROUP BY (bucket_id"



    fun queryAllImage(context: Context) {


        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA)
        try {

            QueryHandler(context)
                .startQuery(0, null, QUERY_URI, projection, null, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class QueryHandler(context: Context) : AsyncQueryHandler(context.contentResolver) {


        override fun startQuery(
            token: Int,
            cookie: Any?,
            uri: Uri?,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            orderBy: String?
        ) {
            super.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy)
        }


        override fun onInsertComplete(token: Int, cookie: Any?, uri: Uri?) {
            super.onInsertComplete(token, cookie, uri)
        }

        override fun onDeleteComplete(token: Int, cookie: Any?, result: Int) {
            super.onDeleteComplete(token, cookie, result)
        }

        override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
               cursor?.apply {
                   val displayNameColumns =
                       cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)

                   val pathColumns =
                       cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                   while (moveToNext()) {
                       val displayName = cursor.getString(displayNameColumns)
                       val data = cursor.getString(pathColumns)
                       quickLogE("名字：  $displayName    地址: $data")
                   }
                   close()
               }
        }

        override fun onUpdateComplete(token: Int, cookie: Any?, result: Int) {
            super.onUpdateComplete(token, cookie, result)
        }



    }
}