package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration

fun <T> MutableList<T>.removeFirstMatches(condition: (T) -> Boolean): T? {
    val indexOf = indexOfFirst(condition)
    if (indexOf == -1) return null
    return removeAt(indexOf)
}

fun <T> MutableList<T>.removeRange(range: IntRange) {
    val last = range.last.coerceAtMost(lastIndex)
    val first = range.first.coerceAtLeast(0)
    for (i in last downTo first) removeAt(i)
}

fun <T> MutableCollection<T>.clearAnd(predicate: (T) -> Unit) {
    val it = iterator()
    while (it.hasNext()) {
        predicate(it.next())
        it.remove()
    }
}

fun <K, V> MutableMap<K, V>.clearAnd(predicate: (Map.Entry<K, V>) -> Unit) = entries.clearAnd(predicate)

fun <K, V> MutableMap<K, V>.replaceAll(value: V) = entries.forEach { it.setValue(value) }

fun <K> MutableMap<K, SimpleTimeMark>.removeMaxTime(duration: Duration) = removeIf { it.value.passedSince() > duration }

fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) = entries.removeIf(predicate)
