package cn.com.quick.kit.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Date :2020/4/11 13:07
 * Description:
 * History
 */

private const val DEFAULT_BUFFER_SIZE = 1024 * 4

@Throws(IOException::class)
fun streamToByteArray(inputStream: InputStream): ByteArray {
    val baos = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var n: Int
    while (-1 != inputStream.read(buffer).also { n = it }) {
        baos.write(buffer, 0, n)
    }
    return baos.toByteArray()
}