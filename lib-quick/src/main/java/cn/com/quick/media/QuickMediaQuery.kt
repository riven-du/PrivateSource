package cn.com.quick.media

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import cn.com.quick.utils.checkedAndroid_Q
import cn.com.quick.utils.quickLogD
import cn.com.quick.utils.quickLogE
import cn.com.quick.utils.quickLogW
import java.lang.ref.WeakReference


class QuickMediaQuery(target: Any): Handler(), LifecycleObserver {


    companion object {

        private val QUERY_URI = MediaStore.Files.getContentUri("external")
        private const val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
        private const val NOT_GIF_UNKNOWN = "!='image/*'"
        private const val NOT_GIF = "!='image/gif' AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF_UNKNOWN
        private const val GROUP_BY_BUCKET_Id = " GROUP BY (bucket_id"
        private const val COLUMN_COUNT = "count"
        private const val COLUMN_BUCKET_ID = "bucket_id"
        private const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"

        /**
         * Filter out recordings that are less than 500 milliseconds long
         */
        private const val AUDIO_DURATION = 500

        /**
         * unit
         */
        private const val FILE_SIZE_UNIT = 1024 * 1024L

        /**
         * Image
         */
        private const val SELECTION =
            ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? )"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0)" + GROUP_BY_BUCKET_Id)
        private const val SELECTION_29 = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? "
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")
        private const val SELECTION_NOT_GIF =
            ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF + ") AND " + MediaStore.MediaColumns.SIZE + ">0)" + GROUP_BY_BUCKET_Id)
        private const val SELECTION_NOT_GIF_29 =
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        /**
         * Queries for images with the specified suffix
         */
        private const val SELECTION_SPECIFIED_FORMAT =
            ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE)

        /**
         * Queries for images with the specified suffix targetSdk>=29
         */
        private const val SELECTION_SPECIFIED_FORMAT_29 =
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE)

        /**
         * Query criteria (audio and video)
         *
         * @param timeCondition
         * @return
         */
        private fun getSelectionArgsForSingleMediaCondition(timeCondition: String): String {
            return if (checkedAndroid_Q()) {
                (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                        + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                        + " AND " + timeCondition)
            } else "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"+ ") AND " + MediaStore.MediaColumns.SIZE + ">0" + " AND " + timeCondition + ")" + GROUP_BY_BUCKET_Id
        }

        /**
         * All mode conditions
         *
         * @param timeCondition
         * @param isGif
         * @return
         */
        private fun getSelectionArgsForAllMediaCondition(
            timeCondition: String,
            isGif: Boolean
        ): String {
            return if (checkedAndroid_Q()) {
                ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                        + (if (isGif) "" else " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)
                        + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + timeCondition + ") AND " + MediaStore.MediaColumns.SIZE + ">0")
            } else "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"+ (if (isGif) "" else " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)+ " OR " + (MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + timeCondition) + ")" + " AND " + MediaStore.MediaColumns.SIZE + ">0)" + GROUP_BY_BUCKET_Id
        }

        /**
         * Get pictures or videos
         */
        private val SELECTION_ALL_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        /**
         * Gets a file of the specified type
         *
         * @param mediaType
         * @return
         */
        private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString())
        }

        /**
         * Gets a file of the specified type
         *
         * @param mediaType
         * @return
         */
        private fun getSelectionArgsForPageSingleMediaType(
            mediaType: Int,
            bucketId: Long
        ): Array<String> {
            return if (bucketId == -1L) {
                arrayOf(mediaType.toString())
            } else {
                arrayOf(
                    mediaType.toString(),
                    bucketId.toString()
                )
            }
        }

        private val PROJECTION_29 = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )
        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS $COLUMN_COUNT"
        )

        /**
         * Media file database field
         */
        private val PROJECTION_PAGE = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID
        )

        /**
         * Get cover uri
         *
         * @param cursor
         * @return
         */
        private fun getFirstUri(cursor: Cursor): String {
            val id =
                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
            return getRealPathAndroid_Q(id)
        }

        /**
         * Get cover url
         *
         * @param cursor
         * @return
         */
        private fun getFirstUrl(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
        }

        /**
         * Android Q
         *
         * @param id
         * @return
         */
        private fun getRealPathAndroid_Q(id: Long): String {
            return QUERY_URI.buildUpon()
                .appendPath(id.toString()).build().toString()
        }
    }



    private val EVENT_QUERY_IMAGE = 1
    private val EVENT_QUERY_VIDEO = 2
    private val EVENT_QUERY_AUDIO = 3



    private var mResolver: WeakReference<ContentResolver>
    private var sLooper: Looper
    private var mWorkerThreadHandler: Handler

    init {
        val context: Context
        when (target) {
            is FragmentActivity -> {
                context = target
                target.lifecycle.addObserver(this)
            }
            is Fragment -> {
                context = target.requireContext()
                target.lifecycle.addObserver(this)
            }
            else -> {
                throw IllegalArgumentException("target is not androidx.fragment.app.FragmentActivity or androidx.fragment.app.Fragment")
            }
        }

        mResolver = WeakReference<ContentResolver>(context.contentResolver)

        synchronized(QuickMediaQuery::class) {
            val thread = HandlerThread("QuickMediaQuery")
            thread.start()
            sLooper = thread.looper
        }
        mWorkerThreadHandler = createHandler(sLooper)
    }
    
    private fun createHandler(looper: Looper): Handler {
        return WorkHandler(looper)
    }

    private class WorkerArgs(
        val uri:Uri,
        val handler: Handler,
        val projection: Array<String>,
        val selection: String?,
        val selectionArgs: Array<String>?,
        var orderBy: String?,
        var result: Cursor?
    )

    private inner class WorkHandler(looper: Looper): Handler(looper) {

        override fun handleMessage(msg: Message) {
            val contentResolver = mResolver.get() ?: return
            val args = msg.obj as WorkerArgs

            val token = msg.what

            var data: Cursor?
            try {
                data = contentResolver.query(args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy)
                data?.apply {
                    val count = count
                    quickLogD("query count: $count")
                }
            } catch (e: Exception) {
                quickLogW("Exception thrown during handling EVENT_ARG_QUERY")
                e.printStackTrace()
                data = null
            }
            args.result = data

            val reply = args.handler.obtainMessage(token)
            reply.obj = args
            reply.arg1 = msg.arg1
            quickLogD("WorkerHandler.handleMsg: msg.arg1=${msg.arg1}, reply.what=${reply.what}")
            reply.sendToTarget()
        }
    }

    private val FILE_COLUMNS_MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE
    private val FILE_COLUMNS_MEDIA_TYPE_IMAGE = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
    private val FILE_COLUMNS_MEDIA_TYPE_VIDEO = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
    private val FILE_COLUMNS_MEDIA_TYPE_AUDIO = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO


    private val MEDIA_COLUMNS_MEDIA_TYPE = MediaStore.MediaColumns.MIME_TYPE
    private val MEDIA_COLUMNS_SIZE = MediaStore.MediaColumns.SIZE
    private val MEDIA_COLUMNS_DURATION = MediaStore.MediaColumns.DURATION



    fun queryAllImage(isGif: Boolean) {

        val gifSelection = if (isGif) "" else " AND $MEDIA_COLUMNS_MEDIA_TYPE$NOT_GIF"
        val selection = "($FILE_COLUMNS_MEDIA_TYPE=?$gifSelection) AND $MEDIA_COLUMNS_SIZE>0"

        val msg = mWorkerThreadHandler.obtainMessage(EVENT_QUERY_IMAGE)
        msg.arg1 = EVENT_QUERY_IMAGE
        val args = WorkerArgs(
            QUERY_URI,
            this,
            PROJECTION_PAGE,
            selection,
            arrayOf(FILE_COLUMNS_MEDIA_TYPE_IMAGE.toString()),
            ORDER_BY,
            null
        )
        msg.obj = args
        mWorkerThreadHandler.sendMessage(msg)
    }

    fun queryAllVideo() {

        val durationCondition = "0 < $MEDIA_COLUMNS_DURATION AND $MEDIA_COLUMNS_DURATION <= ${Long.MAX_VALUE}"

        val selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + durationCondition + ") AND " + MediaStore.MediaColumns.SIZE + ">0"

        val msg = mWorkerThreadHandler.obtainMessage(EVENT_QUERY_VIDEO)
        msg.arg1 = EVENT_QUERY_VIDEO
        val args = WorkerArgs(
            QUERY_URI,
            this,
            PROJECTION_PAGE,
            selection,
            arrayOf(FILE_COLUMNS_MEDIA_TYPE_VIDEO.toString()),
            ORDER_BY,
            null
        )
        msg.obj = args
        mWorkerThreadHandler.sendMessage(msg)
    }

    fun queryAllAudio() {
        val durationCondition = "0 < $MEDIA_COLUMNS_DURATION AND $MEDIA_COLUMNS_DURATION <= ${Long.MAX_VALUE}"
        val selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? AND " + durationCondition + ") AND " + MediaStore.MediaColumns.SIZE + ">0"
        val msg = mWorkerThreadHandler.obtainMessage(EVENT_QUERY_AUDIO)
        msg.arg1 = EVENT_QUERY_AUDIO
        val args = WorkerArgs(
            QUERY_URI,
            this,
            PROJECTION,
            selection,
            arrayOf(FILE_COLUMNS_MEDIA_TYPE_AUDIO.toString()),
            null,
            null
        )
        msg.obj = args
        mWorkerThreadHandler.sendMessage(msg)
    }

    fun queryAllDoc(context: Context) {
        //MEDIA_TYPE_NONE

        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? AND ${MediaStore.MediaColumns.MIME_TYPE}=? OR ${MediaStore.MediaColumns.MIME_TYPE}=? OR ${MediaStore.MediaColumns.MIME_TYPE}=?"

        try {
            val data = context.contentResolver.query(
                QUERY_URI,
                arrayOf(MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DATA),
                selection,
                arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(), "application/pdf","application/msword", "audio/x-mpeg"),
                null
            )

            data?.apply {
                count

                val mimeTypeColumn = getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dataColumn = getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

                while (moveToNext()) {
                    val mimeType = getString(mimeTypeColumn)
                    if (TextUtils.isEmpty(mimeType)) {
                        continue
                    }
                    val data = getString(dataColumn)
                    quickLogE("mime_type:  $mimeType               data:   $data")
                }

                close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun handleMessage(msg: Message) {
        val args = msg.obj as WorkerArgs
        quickLogD("QuickMediaQuery.handleMsg: msg.arg1=${msg.arg1}, reply.what=${msg.what}")
        when (msg.arg1) {
            EVENT_QUERY_IMAGE -> {
                args.result?.apply {
                    try {
                        val dataColumn: Int = getColumnIndex(MediaStore.Images.Media.DATA)
                        var number:Int = 0

                        while (moveToNext()) {
                            val path = getString(dataColumn)
                            if (TextUtils.isEmpty(path)) {
                                continue
                            }

                            number ++

                            quickLogE("path:  $path")
                        }
                        quickLogE("count:  $number")

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            EVENT_QUERY_VIDEO -> {
                args.result?.apply {
                    try {

                        val dataColumn: Int = getColumnIndex(MediaStore.Images.Media.DATA)
                        var number:Int = 0



                        while (moveToNext()) {
                            val path = getString(dataColumn)
                            if (TextUtils.isEmpty(path)) {
                                continue
                            }

                            number ++

                            quickLogE("path:  $path")
                        }
                        quickLogE("count:  $number")

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            EVENT_QUERY_AUDIO -> {
                args.result?.apply {
                    try {

                        val dataColumn: Int = getColumnIndex(MediaStore.Images.Media.DATA)


                        var number:Int = 0

                        while (moveToNext()) {
                            val path = getString(dataColumn)
                            if (TextUtils.isEmpty(path)) {
                                continue
                            }

                            number ++

                            quickLogE("path:  $path")
                        }
                        quickLogE("count:  $count")

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun closeQuery() {
        mWorkerThreadHandler.removeMessages(0)
    }
}