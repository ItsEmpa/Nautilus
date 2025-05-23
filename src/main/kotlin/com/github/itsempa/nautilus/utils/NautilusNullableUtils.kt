package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark

@Suppress("NOTHING_TO_INLINE")
object NautilusNullableUtils {
    inline fun <reified T> Any.cast(): T = this as T
    inline fun <reified T> Any.castOrNull(): T? = this as? T

    inline fun Int?.orZero(): Int = this ?: 0
    inline fun Double?.orZero(): Double = this ?: 0.0
    inline fun Float?.orZero(): Float = this ?: 0f
    inline fun Long?.orZero(): Long = this ?: 0L

    inline fun Boolean?.orFalse(): Boolean = this ?: false
    inline fun Boolean?.orTrue(): Boolean = this ?: true
    inline fun SimpleTimeMark?.orFarPast(): SimpleTimeMark = this ?: SimpleTimeMark.farPast()
}
