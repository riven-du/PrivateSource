package cn.com.quick.pdf.source

import android.content.Context
import android.net.Uri
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.IOException

/**
 * Date :2020/4/11 13:46
 * Description:
 * History
 */
class UriSource(private val uri: Uri): DocumentSource {

    @Throws(IOException::class)
    override fun createDocument(
        context: Context,
        core: PdfiumCore,
        password: String?
    ): PdfDocument = core.newDocument(context.contentResolver.openFileDescriptor(uri, "r")!!, password)
}