package cn.com.quick.pdf.core

/**
 * Date :2020/4/10 22:51
 * Description:
 * History
 */
class PdfPasswordException: Exception {
    constructor(): super()
    constructor(detailMessage: String): super(detailMessage)
}