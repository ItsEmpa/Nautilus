package com.github.itsempa.nautilus.utils

// TODO: get a better file name

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> min(a: T, b: T): T = if (a < b) a else b

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> max(a: T, b: T): T = if (a > b) a else b

inline fun <T, C : Comparable<C>> minBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) < comparator(b)) a else b
inline fun <T, C : Comparable<C>> maxBy(a: T, b: T, comparator: (T) -> C): T = if (comparator(a) > comparator(b)) a else b
