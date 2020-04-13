package cn.com.quick.pdf.source

import android.content.Context
import android.os.ParcelFileDescriptor
import cn.com.quick.kit.utils.copyAssetFileToCache
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.IOException

/**
 * Date :2020/4/11 13:14
 * Description:
 * History
 */
class AssetSource(private val assetsName: String): DocumentSource {

    @Throws(IOException::class)
    override fun createDocument(
        context: Context,
        core: PdfiumCore,
        password: String?
    ): PdfDocument = core.newDocument(ParcelFileDescriptor.open(copyAssetFileToCache(context, assetsName), ParcelFileDescriptor.MODE_READ_ONLY), password)
}