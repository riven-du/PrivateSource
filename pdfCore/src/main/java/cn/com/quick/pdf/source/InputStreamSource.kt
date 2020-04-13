package cn.com.quick.pdf.source

import android.content.Context
import cn.com.quick.kit.utils.streamToByteArray
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.IOException
import java.io.InputStream

/**
 * Date :2020/4/11 13:04
 * Description:
 * History
 */
class InputStreamSource(private val inputStream: InputStream): DocumentSource {

    @Throws(IOException::class)
    override fun createDocument(
        context: Context,
        core: PdfiumCore,
        password: String?
    ): PdfDocument = core.newDocument(streamToByteArray(inputStream), password)

}