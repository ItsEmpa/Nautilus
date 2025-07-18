package com.github.itsempa.nautilus.utils

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> min(a: T, b: T): T = if (a < b) a else b

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> max(a: T, b: T): T = if (a > b) a else b

inline fun <T, C : Comparable<C>> minBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) < comparator(b)) a else b
inline fun <T, C : Comparable<C>> maxBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) > comparator(b)) a else b

/** Returns a list of all object instances that are immediate subclasses of the specified sealed class. */
inline fun <reified T : Any> getSealedObjects(): List<T> {
    val kClass = T::class
    require(kClass.isSealed) { "${kClass.simpleName} is not a sealed class" }
    return kClass.sealedSubclasses.mapNotNull { it.objectInstance }
}
