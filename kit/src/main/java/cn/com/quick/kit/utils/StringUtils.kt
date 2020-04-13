package cn.com.quick.kit.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Date :2020/4/12 17:27
 * Description:
 * History
 */

fun getHashKey(key: String): String {
    return try {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(key.toByte())
        bytesToHexString(digest.digest())
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        key.hashCode().toString()
    }
}

/**
 *
 */
private fun bytesToHexString(bytes: ByteArray): String {
    val sb = StringBuilder()
    var hex: String
    for (byte in bytes) {
        hex = Integer.toHexString(0XFF and byte.toInt())
        if (hex.length == 1) {
            sb.append('0')
        }
        sb.append(hex)
    }
    return sb.toString()
}