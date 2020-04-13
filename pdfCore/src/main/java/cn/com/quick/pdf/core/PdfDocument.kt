package cn.com.quick.pdf.core

import android.os.ParcelFileDescriptor
import android.util.ArrayMap
import java.util.*

/**
 * Date :2020/4/10 22:34
 * Description:
 * History
 */
class PdfDocument constructor(){

    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var mNativeDocPtr: Long = 0
    var mNativePagesPtr: ArrayMap<Int, Long> = ArrayMap()

    fun hasPage(index: Int): Boolean = mNativePagesPtr.containsKey(index)

    data class Meta(var title:String = "",
                    var author: String = "",
                    var subject: String = "",
                    var keywords: String = "",
                    var creator: String = "",
                    var producer: String = "",
                    var creationDate: String = "",
                    var modDate: String = "")

    data class Bookmark(var children: MutableList<Bookmark> = mutableListOf(),
                        var title: String = "",
                        var pageIdx: Long = -1,
                        var mNativePtr: Long = -1) {

        fun hasChildren(): Boolean {
            return children.isNotEmpty()
        }
    }
}