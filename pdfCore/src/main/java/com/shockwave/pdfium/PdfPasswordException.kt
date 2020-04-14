package com.shockwave.pdfium

/**
 * Date :2020/4/10 22:51
 * Description:
 * History
 */
class PdfPasswordException: Exception {
    constructor(): super()
    constructor(detailMessage: String): super(detailMessage)
}