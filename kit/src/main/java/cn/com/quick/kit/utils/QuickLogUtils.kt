package cn.com.quick.kit.utils

import android.text.TextUtils
import android.util.Log

/**
 * Author: duqi
 * Date :2020/3/16 23:54
 * Description:
 * History
 */

private const val DEFAULT_TAG = "Quick"
private val LINE_SEPARATOR = System.getProperty("line.separator")

var quickLogSwitch = false

var tag = DEFAULT_TAG

fun quickLogI(msg: String) {
    printLog(Log.INFO, msg)
}

fun quickLogE(msg: String) {
    printLog(Log.ERROR, msg)
}

fun quickLogD(msg: String) {
    printLog(Log.DEBUG, msg)
}

fun quickLogW(msg: String) {
    printLog(Log.WARN, msg)
}

/**
 * 打印
 * @param priority 优先级
 * @param logs 日志信息
 */
private fun printLog(priority: Int, msg: String) {
    if (quickLogSwitch) {
        return
    }
    try { // 获取方法入栈信息
        val stackTrace = Throwable().stackTrace
        if (stackTrace.isEmpty()) {
            return
        }
        val stackTraceElement = stackTrace[2] ?: return
        val sb = StringBuilder()
        sb.append(stackTraceElement)
        sb.append(LINE_SEPARATOR)
        sb.append(msg)
        val info = sb.toString()
        Log.println(priority, tag, if (TextUtils.isEmpty(info)) "log is empty" else info)
    } catch (ignore: Exception) {}
}