package com.shockwave.pdfium.util

/**
 * Date :2020/4/13 17:47
 * Description:
 * History
 */
data class Size(private val width: Int, private val height: Int) {

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (this === obj) {
            return true
        }
        if (obj is Size) {
            val (width1, height1) = obj
            return width == width1 && height == height1
        }
        return false
    }

    override fun toString(): String {
        return "$width x $height"
    }

    override fun hashCode(): Int {
        return height xor (width shl Integer.SIZE / 2 or (width ushr Integer.SIZE / 2))
    }
}