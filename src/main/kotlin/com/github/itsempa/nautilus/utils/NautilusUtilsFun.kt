package com.github.itsempa.nautilus.utils

// TODO: get a better file name

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> min(a: T, b: T): T = if (a < b) a else b

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Comparable<T>> max(a: T, b: T): T = if (a > b) a else b
