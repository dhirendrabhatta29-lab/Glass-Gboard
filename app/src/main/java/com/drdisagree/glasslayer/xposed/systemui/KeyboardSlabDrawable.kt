package com.drdisagree.glasslayer.xposed.systemui

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

internal class KeyboardSlabDrawable(
    private val blur: () -> Drawable?,
    private val color: () -> Int,
    private val topRadius: () -> Float,
    private val extend: () -> Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val radii = FloatArray(RADII)

    override fun draw(canvas: Canvas) {
        val area = bounds
        if (area.isEmpty) return
        val bottom = area.bottom + extend()
        blur()?.let {
            it.setBounds(area.left, area.top, area.right, bottom)
            it.draw(canvas)
        }
        val radius = topRadius()
        for (index in 0 until TOP_CORNER_VALUES) radii[index] = radius
        for (index in TOP_CORNER_VALUES until RADII) radii[index] = 0f
        path.rewind()
        path.addRoundRect(area.left.toFloat(), area.top.toFloat(), area.right.toFloat(), bottom.toFloat(), radii, Path.Direction.CW)
        paint.color = color()
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

private const val RADII = 8
private const val TOP_CORNER_VALUES = 4
