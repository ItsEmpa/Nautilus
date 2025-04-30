package com.github.itsempa.nautilus.utils

import java.util.EnumMap

fun <K : Enum<K>, V> Map<K, V>.toEnumMap(): EnumMap<K, V> = EnumMap(this)

inline fun <reified K : Enum<K>, V> fullEnumMapOf(defaultValue: V): EnumMap<K, V> {
    return buildMap {
        for (enum in enumValues<K>()) put(enum, defaultValue)
    }.toEnumMap()
}

fun <K, V> MutableMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) = entries.removeIf(predicate)
