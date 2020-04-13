package cn.com.quick.pdf.source

import android.content.Context
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.IOException

/**
 * Date :2020/4/11 12:56
 * Description:
 * History
 */
interface DocumentSource {

    @Throws(IOException::class)
    fun createDocument(context: Context, core: PdfiumCore, password: String?): PdfDocument
}