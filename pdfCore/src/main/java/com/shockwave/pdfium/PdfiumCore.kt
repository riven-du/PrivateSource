@file:Suppress("SpellCheckingInspection")

package com.shockwave.pdfium

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import com.shockwave.pdfium.PdfDocument.Bookmark
import com.shockwave.pdfium.PdfDocument.Meta
import com.shockwave.pdfium.util.Size
import java.io.FileDescriptor
import java.io.IOException
import java.lang.reflect.Field


/**
 * Date :2020/4/10 22:55
 * Description:
 * History
 */
class PdfiumCore(context: Context) {

    companion object {
        private val TAG = PdfiumCore::class.java.name
        private val FD_CLASS: Class<*> = FileDescriptor::class.java
        private val FD_FIELD_NAME = "descriptor"

        init {
            try {
                System.loadLibrary("c++_shared")
                System.loadLibrary("modpng")
                System.loadLibrary("modft2")
                System.loadLibrary("modpdfium")
                System.loadLibrary("jniPdfium")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native libraries failed to load - $e")
            }
        }
    }

    private external fun nativeOpenDocument(fd: Int, password: String?): Long

    private external fun nativeOpenMemDocument(data: ByteArray, password: String?): Long

    private external fun nativeCloseDocument(docPtr: Long)

    private external fun nativeGetPageCount(docPtr: Long): Int

    private external fun nativeLoadPage(docPtr: Long, pageIndex: Int): Long

    private external fun nativeLoadPages(docPtr: Long,
        fromIndex: Int,
        toIndex: Int
    ): LongArray

    private external fun nativeClosePage(pagePtr: Long)

    private external fun nativeClosePages(pagesPtr: LongArray)

    private external fun nativeGetPageWidthPixel(pagePtr: Long, dpi: Int): Int

    private external fun nativeGetPageHeightPixel(pagePtr: Long, dpi: Int): Int

    private external fun nativeGetPageWidthPoint(pagePtr: Long): Int

    private external fun nativeGetPageHeightPoint(pagePtr: Long): Int

    private external fun nativeRenderPage(
        pagePtr: Long, surface: Surface, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeRenderPageBitmap(
        pagePtr: Long, bitmap: Bitmap, dpi: Int,
        startX: Int, startY: Int,
        drawSizeHor: Int, drawSizeVer: Int,
        renderAnnot: Boolean
    )

    private external fun nativeGetDocumentMetaText(
        docPtr: Long,
        tag: String
    ): String

    private external fun nativeGetFirstChildBookmark(
        docPtr: Long,
        bookmarkPtr: Long?
    ): Long

    private external fun nativeGetSiblingBookmark(
        docPtr: Long,
        bookmarkPtr: Long
    ): Long

    private external fun nativeGetBookmarkTitle(bookmarkPtr: Long): String?

    private external fun nativeGetBookmarkDestIndex(
        docPtr: Long,
        bookmarkPtr: Long
    ): Long

    private external fun nativeGetPageSizeByIndex(
        docPtr: Long,
        pageIndex: Int,
        dpi: Int
    ): Size

    private external fun nativeGetPageLinks(pagePtr: Long): LongArray

    private external fun nativeGetDestPageIndex(
        docPtr: Long,
        linkPtr: Long
    ): Int?

    private external fun nativeGetLinkURI(
        docPtr: Long,
        linkPtr: Long
    ): String?

    private external fun nativeGetLinkRect(linkPtr: Long): RectF?

    private external fun nativePageCoordsToDevice(
        pagePtr: Long, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point


    /* synchronize native methods */
    private val lock = Any()
    private var mFdField: Field? = null
    private var mCurrentDpi = 0

    fun getNumFd(fdObj: ParcelFileDescriptor): Int {
        return try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME)
                mFdField!!.isAccessible = true
            }
            mFdField!!.getInt(fdObj.fileDescriptor)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            -1
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            -1
        }
    }


    /** Context needed to get screen density  */
    fun PdfiumCore(ctx: Context) {
        mCurrentDpi = ctx.resources.displayMetrics.densityDpi
        Log.d(TAG, "Starting PdfiumAndroid ")
    }

    /** Create new document from file  */
    @Throws(IOException::class)
    fun newDocument(fd: ParcelFileDescriptor): PdfDocument? {
        return newDocument(fd, null)
    }

    /** Create new document from file with password  */
    @Throws(IOException::class)
    fun newDocument(
        fd: ParcelFileDescriptor,
        password: String?
    ): PdfDocument? {
        val document = PdfDocument()
        document.parcelFileDescriptor = fd
        synchronized(lock) { document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password) }
        return document
    }

    /** Create new document from bytearray  */
    @Throws(IOException::class)
    fun newDocument(data: ByteArray): PdfDocument {
        return newDocument(data, null)
    }

    /** Create new document from bytearray with password  */
    @Throws(IOException::class)
    fun newDocument(
        data: ByteArray,
        password: String?
    ): PdfDocument {
        val document = PdfDocument()
        synchronized(lock) { document.mNativeDocPtr = nativeOpenMemDocument(data, password) }
        return document
    }

    /** Get total numer of pages in document  */
    fun getPageCount(doc: PdfDocument): Int {
        synchronized(lock) { return nativeGetPageCount(doc.mNativeDocPtr) }
    }

    /** Open page and store native pointer in [PdfDocument]  */
    fun openPage(doc: PdfDocument, pageIndex: Int): Long {
        var pagePtr: Long
        synchronized(lock) {
            pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex)
            doc.mNativePagesPtr[pageIndex] = pagePtr
            return pagePtr
        }
    }

    /** Open range of pages and store native pointers in [PdfDocument]  */
    fun openPage(
        doc: PdfDocument,
        fromIndex: Int,
        toIndex: Int
    ): LongArray? {
        var pagesPtr: LongArray
        synchronized(lock) {
            pagesPtr = nativeLoadPages(doc.mNativeDocPtr, fromIndex, toIndex)
            var pageIndex = fromIndex
            for (page in pagesPtr) {
                if (pageIndex > toIndex) break
                doc.mNativePagesPtr[pageIndex] = page
                pageIndex++
            }
            return pagesPtr
        }
    }

    /**
     * Get page width in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageWidth(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            var pagePtr: Long
            return if (doc.mNativePagesPtr[index].also { pagePtr = it!! } != null) {
                nativeGetPageWidthPixel(pagePtr, mCurrentDpi)
            } else 0
        }
    }

    /**
     * Get page height in pixels. <br></br>
     * This method requires page to be opened.
     */
    fun getPageHeight(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            var pagePtr: Long
            return if (doc.mNativePagesPtr[index].also { pagePtr = it!! } != null) {
                nativeGetPageHeightPixel(pagePtr, mCurrentDpi)
            } else 0
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageWidthPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            var pagePtr: Long
            return if (doc.mNativePagesPtr[index].also { pagePtr = it!! } != null) {
                nativeGetPageWidthPoint(pagePtr)
            } else 0
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br></br>
     * This method requires page to be opened.
     */
    fun getPageHeightPoint(doc: PdfDocument, index: Int): Int {
        synchronized(lock) {
            var pagePtr: Long
            return if (doc.mNativePagesPtr[index].also { pagePtr = it!! } != null) {
                nativeGetPageHeightPoint(pagePtr)
            } else 0
        }
    }

    /**
     * Get size of page in pixels.<br></br>
     * This method does not require given page to be opened.
     */
    fun getPageSize(doc: PdfDocument, index: Int): Size? {
        synchronized(
            lock
        ) { return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, mCurrentDpi) }
    }

    /**
     * Render page fragment on [Surface].<br></br>
     * Page must be opened before rendering.
     */
    fun renderPage(
        doc: PdfDocument, surface: Surface, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int
    ) {
        renderPage(doc, surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false)
    }

    /**
     * Render page fragment on [Surface]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     */
    fun renderPage(
        doc: PdfDocument, surface: Surface, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean
    ) {
        synchronized(lock) {
            try {
                //nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(
                    doc.mNativePagesPtr[pageIndex]!!, surface, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
    }

    /**
     * Render page fragment on [Bitmap].<br></br>
     * Page must be opened before rendering.
     *
     *
     * Supported bitmap configurations:
     *
     *  * ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     *  * RGB_565 - little worse quality, twice less memory usage
     *
     */
    fun renderPageBitmap(
        doc: PdfDocument, bitmap: Bitmap, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int
    ) {
        renderPageBitmap(doc, bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false)
    }

    /**
     * Render page fragment on [Bitmap]. This method allows to render annotations.<br></br>
     * Page must be opened before rendering.
     *
     *
     * For more info see [PdfiumCore.renderPageBitmap]
     */
    fun renderPageBitmap(
        doc: PdfDocument, bitmap: Bitmap, pageIndex: Int,
        startX: Int, startY: Int, drawSizeX: Int, drawSizeY: Int,
        renderAnnot: Boolean
    ) {
        synchronized(lock) {
            try {
                nativeRenderPageBitmap(
                    doc.mNativePagesPtr[pageIndex]!!, bitmap, mCurrentDpi,
                    startX, startY, drawSizeX, drawSizeY, renderAnnot
                )
            } catch (e: NullPointerException) {
                Log.e(TAG, "mContext may be null")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e(TAG, "Exception throw from native")
                e.printStackTrace()
            }
        }
    }

    /** Release native resources and opened file  */
    fun closeDocument(doc: PdfDocument) {
        synchronized(lock) {
            for (index in doc.mNativePagesPtr.keys) {
                nativeClosePage(doc.mNativePagesPtr[index]!!)
            }
            doc.mNativePagesPtr.clear()
            nativeCloseDocument(doc.mNativeDocPtr)
            if (doc.parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    doc.parcelFileDescriptor!!.close()
                } catch (e: IOException) {
                    /* ignore */
                }
                doc.parcelFileDescriptor = null
            }
        }
    }

    /** Get metadata for given document  */
    fun getDocumentMeta(doc: PdfDocument): Meta? {
        synchronized(lock) {
           return Meta(
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Title"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Author"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Subject"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Keywords"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Creator"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "Producer"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "CreationDate"),
                nativeGetDocumentMetaText(doc.mNativeDocPtr, "ModDate")
            )
        }
    }

    /** Get table of contents (bookmarks) for given document  */
    fun getTableOfContents(doc: PdfDocument): List<Bookmark>? {
        synchronized(lock) {
            val topLevel: MutableList<Bookmark> = ArrayList()
            val first = nativeGetFirstChildBookmark(doc.mNativeDocPtr, null)
            first?.let { recursiveGetBookmark(topLevel, doc, it) }
            return topLevel
        }
    }

    private fun recursiveGetBookmark(
        tree: MutableList<Bookmark>,
        doc: PdfDocument,
        bookmarkPtr: Long
    ) {
        val bookmark = Bookmark()
        bookmark.mNativePtr = bookmarkPtr
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr)!!
        bookmark.pageIdx = nativeGetBookmarkDestIndex(doc.mNativeDocPtr, bookmarkPtr)
        tree.add(bookmark)
        val child = nativeGetFirstChildBookmark(doc.mNativeDocPtr, bookmarkPtr)
        recursiveGetBookmark(bookmark.children, doc, child)
        val sibling = nativeGetSiblingBookmark(doc.mNativeDocPtr, bookmarkPtr)
        sibling?.let { recursiveGetBookmark(tree, doc, it) }
    }

    /** Get all links from given page  */
    fun getPageLinks(
        doc: PdfDocument,
        pageIndex: Int
    ): List<PdfDocument.Link?>? {
        synchronized(lock) {
            val links: MutableList<PdfDocument.Link?> = ArrayList()
            val nativePagePtr = doc.mNativePagesPtr[pageIndex] ?: return links
            val linkPtrs = nativeGetPageLinks(nativePagePtr)
            for (linkPtr in linkPtrs) {
                val index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr)
                val uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr)
                val rect = nativeGetLinkRect(linkPtr)
                if (rect != null && (index != null || uri != null)) {
                    links.add(PdfDocument.Link(rect, index, uri))
                }
            }
            return links
        }
    }

    /**
     * Map page coordinates to device screen coordinates
     *
     * @param doc       pdf document
     * @param pageIndex index of page
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     * 2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    fun mapPageCoordsToDevice(
        doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, pageX: Double, pageY: Double
    ): Point {
        val pagePtr = doc.mNativePagesPtr[pageIndex]!!
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY)
    }

    /**
     * @return mapped coordinates
     * @see PdfiumCore.mapPageCoordsToDevice
     */
    fun mapRectToDevice(
        doc: PdfDocument, pageIndex: Int, startX: Int, startY: Int, sizeX: Int,
        sizeY: Int, rotate: Int, coords: RectF
    ): RectF? {
        val leftTop: Point = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.left.toDouble(), coords.top.toDouble()
        )
        val rightBottom: Point = mapPageCoordsToDevice(
            doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
            coords.right.toDouble(), coords.bottom.toDouble()
        )
        return RectF(leftTop.x.toFloat(), leftTop.y.toFloat(), rightBottom.x.toFloat(), rightBottom.y.toFloat())
    }
}