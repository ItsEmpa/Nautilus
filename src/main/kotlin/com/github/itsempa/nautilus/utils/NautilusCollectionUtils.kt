package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import java.util.EnumMap
import java.util.EnumSet
import kotlin.time.Duration

inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = EnumSet.noneOf(E::class.java)

fun <E : Enum<E>> Set<E>.toEnumSet(): EnumSet<E> = EnumSet.copyOf(this)

inline fun <reified K : Enum<K>, V> enumMapOf(): EnumMap<K, V> = EnumMap<K, V>(K::class.java)

fun <K : Enum<K>, V> Map<K, V>.toEnumMap(): EnumMap<K, V> = EnumMap(this)

inline fun <reified K : Enum<K>, V> fullEnumMapOf(defaultValue: V): EnumMap<K, V> {
    return buildMap {
        for (enum in enumValues<K>()) put(enum, defaultValue)
    }.toEnumMap()
}

inline fun <reified K : Enum<K>, V> fullEnumMapOf(defaultValue: () -> V): EnumMap<K, V> {
    return buildMap {
        for (enum in enumValues<K>()) put(enum, defaultValue())
    }.toEnumMap()
}

fun <T> MutableList<T>.removeFirstMatches(condition: (T) -> Boolean): T? {
    val indexOf = indexOfFirst(condition)
    if (indexOf == -1) return null
    return removeAt(indexOf)
}

fun <K, V> MutableMap<K, V>.replaceAll(value: V) = entries.forEach { it.setValue(value) }

fun <K> MutableMap<K, SimpleTimeMark>.removeMaxTime(duration: Duration) = removeIf { it.value.passedSince() > duration }

fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) = entries.removeIf(predicate)
