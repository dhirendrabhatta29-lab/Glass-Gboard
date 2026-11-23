package com.drdisagree.glasslayer.xposed.glass

import android.view.View
import kotlin.math.sqrt

internal object GlassLight {

    private const val SCREEN_X = -0.5f
    private const val SCREEN_Y = -0.85f
    private const val EPSILON = 1e-4f

    private val vectors = FloatArray(4)

    val defaultX: Float
    val defaultY: Float

    init {
        val length = sqrt(SCREEN_X * SCREEN_X + SCREEN_Y * SCREEN_Y)
        defaultX = SCREEN_X / length
        defaultY = SCREEN_Y / length
    }

    fun fromAxes(axisXx: Float, axisXy: Float, axisYx: Float, axisYy: Float, out: FloatArray) {
        val lengthX = sqrt(axisXx * axisXx + axisXy * axisXy)
        val lengthY = sqrt(axisYx * axisYx + axisYy * axisYy)
        if (lengthX < EPSILON || lengthY < EPSILON) {
            out[0] = defaultX
            out[1] = defaultY
            return
        }
        val x = (axisXx * defaultX + axisXy * defaultY) / lengthX
        val y = (axisYx * defaultX + axisYy * defaultY) / lengthY
        val length = sqrt(x * x + y * y)
        if (length < EPSILON) {
            out[0] = defaultX
            out[1] = defaultY
            return
        }
        out[0] = x / length
        out[1] = y / length
    }

    fun forView(view: View, out: FloatArray) {
        vectors[0] = 1f
        vectors[1] = 0f
        vectors[2] = 0f
        vectors[3] = 1f
        var current: View? = view
        while (current != null) {
            val matrix = current.matrix
            if (!matrix.isIdentity) matrix.mapVectors(vectors)
            current = current.parent as? View
        }
        fromAxes(vectors[0], vectors[1], vectors[2], vectors[3], out)
    }
}
