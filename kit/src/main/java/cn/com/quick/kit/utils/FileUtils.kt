package cn.com.quick.kit.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Date :2020/4/11 13:18
 * Description:
 * History
 */
@Throws(IOException::class)
fun copyAssetFileToCache(context: Context, assetName: String): File {
    return File(context.cacheDir, assetName).apply {
        copyFile(context.assets.open(assetName), this)
    }
}

@Throws(IOException::class)
fun copyFile(inputStream: InputStream, outFile: File) {
    FileOutputStream(outFile).apply {
        var read:Int
        val bytes = ByteArray(1024)
        while (inputStream.read(bytes).also { read = it } != -1) {
            write(bytes, 0, read)
        }
        inputStream.close()
        close()
    }
}