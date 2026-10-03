package com.application.requiemproject.domain.model

data class TextBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    fun width() = right - left
    fun height() = bottom - top
    fun centerX() = (left + right) / 2
    fun centerY() = (top + bottom) / 2
    fun intersects(other: TextBounds) = left < other.right && other.left < right && top < other.bottom && other.top < bottom
}

data class TextBlock(val text: String, val boundingBox: TextBounds?)
