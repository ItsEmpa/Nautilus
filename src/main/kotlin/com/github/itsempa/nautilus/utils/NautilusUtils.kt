package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.deps.moulconfig.observer.Property

object NautilusUtils {
    fun <T : Enum<T>> Enum<T>.toFormattedName(): String =
        name.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }

    fun <T : Any> T.asProperty(): Property<T> = Property.of(this)

    inline fun <reified T> Any.cast(): T = this as T
    inline fun <reified T> Any.castOrNull(): T? = this as? T

    inline val Int.thousands get(): Int = this * 1_000
    inline val Int.millions get(): Int = this * 1_000_000

    fun Int?.orZero(): Int = this ?: 0
    fun Double?.orZero(): Double = this ?: 0.0
    fun Float?.orZero(): Float = this ?: 0f
    fun Long?.orZero(): Long = this ?: 0L
}
