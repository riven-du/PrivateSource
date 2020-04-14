package com.shockwave.pdfium

import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.ArrayMap


/**
 * Date :2020/4/10 22:34
 * Description:
 * History
 */
class PdfDocument {

    class Meta(
        var title: String,
        var author: String,
        var subject: String,
        var keywords: String,
        var creator: String,
        var producer: String,
        var creationDate: String,
        var modDate: String)

    data class Bookmark(
        val children: MutableList<Bookmark> = mutableListOf(),
        var title: String? = null,
        var pageIdx: Long = 0,
        var mNativePtr: Long = 0) {

        fun hasChildren(): Boolean {
            return children.isNotEmpty()
        }

    }

    class Link(val bounds: RectF, val destPageIdx: Int?, val uri: String?)

    var mNativeDocPtr: Long = 0

    var parcelFileDescriptor: ParcelFileDescriptor? = null

    val mNativePagesPtr: ArrayMap<Int, Long> = ArrayMap()

    fun hasPage(index: Int): Boolean {
        return mNativePagesPtr.containsKey(index)
    }
}