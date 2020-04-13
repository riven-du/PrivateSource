package cn.com.quick.pdf.source

import android.content.Context
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.IOException

/**
 * Date :2020/4/11 12:59
 * Description:
 * History
 */
class ByteArraySource(private val data: ByteArray): DocumentSource {

    @Throws(IOException::class)
    override fun createDocument(
        context: Context,
        core: PdfiumCore,
        password: String?
    ): PdfDocument = core.newDocument(data, password)
}