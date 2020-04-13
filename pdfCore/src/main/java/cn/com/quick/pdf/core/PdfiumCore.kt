@file:Suppress("SpellCheckingInspection")

package cn.com.quick.pdf.core

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.view.Surface
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field

/**
 * Date :2020/4/10 22:55
 * Description:
 * History
 */
class PdfiumCore(context: Context) {

    private val FD_CLASS: Class<*> = FileDescriptor::class.java
    private val FD_FIELD_NAME = "descriptor"
    private var mFdField: Field? = null

    private var mCurrentDpi = 0

    /* synchronize native methods */
    private val lock = Any()

    init {
        mCurrentDpi = context.resources.displayMetrics.densityDpi
    }

    private fun getNumFd(fdObj: ParcelFileDescriptor): Int {
        return try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                mFdField!!.isAccessible = true
            }
            mFdField!!.getInt(fdObj.fileDescriptor)
        } catch (e: Exception) {
            -1
        }
    }

    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocument {
        return newDocument(fd, null)
    }

    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor, password: String?): PdfDocument {
        return PdfDocument().apply {
            parcelFileDescriptor = fd
            synchronized(lock) {
                mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password)
            }
        }
    }

    @Throws(IOException::class)
    fun newDocument(data: ByteArray): PdfDocument {
        return newDocument(data, null)
    }

    @Throws(IOException::class)
    fun newDocument(data: ByteArray, password: String?): PdfDocument {
        return PdfDocument().apply {
            synchronized(lock) {
                mNativeDocPtr = nativeOpenMemDocument(data, password)
            }
        }
    }

    fun getPageCount(document: PdfDocument): Int {
        synchronized(lock) {
            return nativeGetPageCount(document.mNativeDocPtr)
        }
    }

    fun openPage(document: PdfDocument, pageIndex: Int): Long {
        synchronized(lock) {
            return nativeLoadPage(document.mNativeDocPtr, pageIndex).also {
                document.mNativePagesPtr[pageIndex] = it
            }
        }
    }

    fun openPage(document: PdfDocument, fromIndex: Int, toIndex: Int): LongArray {
        synchronized(lock) {
            if (fromIndex < toIndex) {
                return longArrayOf()
            }
            return nativeLoadPages(document.mNativeDocPtr, fromIndex, toIndex)!!.also {
                var pageIndex = fromIndex
                for (page in it) {
                    if (pageIndex > toIndex) break
                    document.mNativePagesPtr[pageIndex] = page
                    pageIndex++
                }
            }
        }
    }

    fun getPageWidth(document: PdfDocument, pageIndex: Int): Int {
        synchronized(lock) {
            document.mNativePagesPtr[pageIndex]?.let {
                return nativeGetPageWidthPixel(it, mCurrentDpi)
            }
            return 0
        }
    }

    fun getPageHeight(document: PdfDocument, pageIndex: Int): Int {
        synchronized(lock) {
            document.mNativePagesPtr[pageIndex]?.let {
                return nativeGetPageHeightPixel(it, mCurrentDpi)
            }
            return 0
        }
    }

    fun getPageWidthPoint(document: PdfDocument, pageIndex: Int): Int {
        synchronized(lock) {
            document.mNativePagesPtr[pageIndex]?.let {
                return nativeGetPageWidthPoint(it)
            }
            return 0
        }
    }

    fun getPageHeightPoint(document: PdfDocument, pageIndex: Int): Int {
        synchronized(lock) {
            document.mNativePagesPtr[pageIndex]?.let {
                return nativeGetPageHeightPoint(it)
            }
            return 0
        }
    }

    fun renderPage(document: PdfDocument, surface: Surface, pageIndex: Int, startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int) {
        renderPage(document, surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false)
    }

    fun renderPage(document: PdfDocument, surface: Surface, pageIndex: Int, startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, renderAnnot: Boolean) {
        synchronized(lock) {
            try {
                nativeRenderPage(document.mNativePagesPtr[pageIndex]!!, surface, mCurrentDpi, startX, startY, drawSizeX, drawSizeY, renderAnnot)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun renderPageBitmap(document: PdfDocument, bitmap: Bitmap, pageIndex: Int, startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int) {
        renderPageBitmap(document, bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false)
    }

    fun renderPageBitmap(document: PdfDocument, bitmap: Bitmap, pageIndex: Int, startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int, renderAnnot: Boolean) {
        synchronized(lock) {
            try {
                nativeRenderPageBitmap(document.mNativePagesPtr[pageIndex]!!, bitmap, mCurrentDpi, startX, startY,drawSizeX,drawSizeY, renderAnnot)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun closeDocument(document: PdfDocument) {
        synchronized(lock) {
            val nativePagesPtr = document.mNativePagesPtr
            for (entry in nativePagesPtr) {
                nativeClosePage(entry.value)
            }
            nativePagesPtr.clear()
            nativeCloseDocument(document.mNativeDocPtr)
            try {
                document.parcelFileDescriptor?.close()
            } catch (ignore: IOException) {}
        }
    }

    fun getDocumentMeta(document: PdfDocument): PdfDocument.Meta {
        synchronized(lock) {
            return PdfDocument.Meta().apply {
                val docPtr = document.mNativeDocPtr
                title = nativeGetDocumentMetaText(docPtr, "Title")
                author = nativeGetDocumentMetaText(docPtr, "Author")
                subject = nativeGetDocumentMetaText(docPtr, "Subject")
                keywords = nativeGetDocumentMetaText(docPtr, "Keywords")
                creator = nativeGetDocumentMetaText(docPtr, "Creator")
                producer = nativeGetDocumentMetaText(docPtr, "Producer")
                creationDate = nativeGetDocumentMetaText(docPtr, "CreationDate")
                modDate = nativeGetDocumentMetaText(docPtr, "ModDate")
            }
        }
    }

    fun getTableOfContents(document: PdfDocument): MutableList<PdfDocument.Bookmark> {
        synchronized(lock) {
            return mutableListOf<PdfDocument.Bookmark>().apply {
                val first = nativeGetFirstChildBookmark(document.mNativeDocPtr, null)
                if (first != null) {
                    recursiveGetBookmark(this, document, first)
                }
            }
        }
    }

    private fun recursiveGetBookmark(tree: MutableList<PdfDocument.Bookmark>, document: PdfDocument, bookmarkPtr: Long) {
        PdfDocument.Bookmark().apply {
            mNativePtr = bookmarkPtr
            title = nativeGetBookmarkTitle(bookmarkPtr)
            pageIdx = nativeGetBookmarkDestIndex(document.mNativeDocPtr,bookmarkPtr)
            tree.add(this)
            val child = nativeGetFirstChildBookmark(document.mNativeDocPtr, bookmarkPtr)
            if (child != null) {
                recursiveGetBookmark(children, document, child)
            }
            val sibling = nativeGetSiblingBookmark(document.mNativeDocPtr, bookmarkPtr)
            if (sibling != null) {
                recursiveGetBookmark(tree, document, sibling)
            }
        }
    }

    companion object {
        init {
            System.loadLibrary("pdfium-lib")
        }
    }
    private external fun nativeOpenDocument(fd: Int, password: String?): Long
    private external fun nativeOpenMemDocument(data: ByteArray, password: String?): Long
    private external fun nativeCloseDocument(docPtr: Long)
    private external fun nativeGetPageCount(docPtr: Long): Int
    private external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long
    private external fun nativeLoadPages(docPtr: Long, fromIndex: Int, toIndex: Int): LongArray?
    private external fun nativeClosePage(pagePtr: Long)
    private external fun nativeClosePages(pagesPtr: LongArray)
    private external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int
    private external fun nativeGetPageWidthPoint(pagePtr: Long): Int
    private external fun nativeGetPageHeightPoint(pagePtr: Long): Int
    private external fun nativeRenderPage(pagePtr: Long, surface: Surface, dpi: Int, startX: Int, startY: Int, drawSizeHor: Int, drawSizeVer: Int, renderAnnot: Boolean)
    private external fun nativeRenderPageBitmap(pagePtr: Long, bitmap: Bitmap, dpi: Int, startX: Int, startY: Int, drawSizeHor: Int, drawSizeVer: Int, renderAnnot: Boolean)
    private external fun nativeGetDocumentMetaText(docPtr: Long, tag: String): String
    private external fun nativeGetFirstChildBookmark(docPtr: Long, bookmarkPtr: Long?): Long?
    private external fun nativeGetSiblingBookmark(docPtr: Long, bookmarkPtr: Long): Long?
    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String
    private external fun nativeGetBookmarkDestIndex(docPtr: Long, bookmarkPtr: Long): Long

}