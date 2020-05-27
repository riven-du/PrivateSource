package cn.com.quick.utils

import android.os.Build

/**
 * 判断是否为 Android Q 版本
 */
fun checkedAndroid_Q(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q