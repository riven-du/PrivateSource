#ifndef _UTIL_HPP_
#define _UTIL_HPP_

#include <jni.h>

extern "C" {
#include <stdlib.h>
}

#include <android/log.h>
//cn.com.quick.pdf.core.PdfPasswordException
#define JIN_PASSWORD_EXCEPTION "cn/com/quick/pdf/core/PdfPasswordException"
//cn.com.quick.pdf.core.PdfiumCore
#define JNI_FUNC(retType, name)  JNIEXPORT retType JNICALL Java_cn_com_quick_pdf_core_PdfiumCore_##name
#define JNI_ARGS    JNIEnv *env, jobject thiz
#define LOG_TAG "jniPdfium"
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif // _UTIL_HPP_
