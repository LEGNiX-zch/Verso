package com.verso.dict.util

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.DimenRes

/**
 * Persists the user's font-size preference and applies it to any [TextView] based on a
 * dimension resource base size. Used by both the results list and the word detail dialog so
 * the whole UI follows the same setting after restart.
 */
object FontSizeManager {

    private const val PREFS = "verso_prefs"
    private const val KEY_LEVEL = "font_level"

    /** Scale factors indexed by seekbar progress (0..6). Index 3 is the default (1.0). */
    private val SCALES = floatArrayOf(0.75f, 0.85f, 0.92f, 1.0f, 1.12f, 1.25f, 1.5f)
    const val DEFAULT_LEVEL = 3

    fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getLevel(context: Context): Int =
        prefs(context).getInt(KEY_LEVEL, DEFAULT_LEVEL).coerceIn(0, SCALES.size - 1)

    fun setLevel(context: Context, level: Int) =
        prefs(context).edit().putInt(KEY_LEVEL, level.coerceIn(0, SCALES.size - 1)).apply()

    fun getScale(context: Context): Float = SCALES[getLevel(context)]

    /**
     * Apply the scaled font size to [view]. [dimenRes] is the base size from dimens.xml (so watch
     * and phone each start from their own base), multiplied by the user's font scale.
     */
    fun applyScaled(context: Context, view: TextView, @DimenRes dimenRes: Int) {
        val basePx = context.resources.getDimension(dimenRes)
        val density = context.resources.displayMetrics.density
        val baseSp = if (density > 0f) basePx / density else basePx
        val scaled = baseSp * getScale(context)
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaled)
    }
}
