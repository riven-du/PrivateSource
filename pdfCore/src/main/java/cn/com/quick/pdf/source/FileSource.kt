package cn.com.quick.pdf.source

import android.content.Context
import android.os.ParcelFileDescriptor
import cn.com.quick.pdf.core.PdfDocument
import cn.com.quick.pdf.core.PdfiumCore
import java.io.File
import java.io.IOException

/**
 * Date :2020/4/11 13:44
 * Description:
 * History
 */
class FileSource(private val file: File): DocumentSource {

    @Throws(IOException::class)
    override fun createDocument(
        context: Context,
        core: PdfiumCore,
        password: String?
    ): PdfDocument = core.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), password)
}