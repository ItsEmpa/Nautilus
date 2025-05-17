package com.github.itsempa.nautilus.utils

import java.util.EnumSet

inline fun <reified E : Enum<E>> emptyEnumSet(): EnumSet<E> = EnumSet.noneOf(E::class.java)

inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = emptyEnumSet<E>()

inline fun <reified E : Enum<E>> enumSetOf(element: E) = emptyEnumSet<E>().apply { add(element) }

inline fun <reified E : Enum<E>> enumSetOf(vararg elements: E): EnumSet<E> =
    if (elements.isEmpty()) emptyEnumSet<E>()
    else elements.toCollection(enumSetOf<E>())


inline fun <reified E : Enum<E>> fullEnumSetOf(): EnumSet<E> = EnumSet.allOf(E::class.java)

inline fun <reified E : Enum<E>> Set<E>.toEnumSet(): EnumSet<E> =
    if (isEmpty()) emptyEnumSet<E>() else EnumSet.copyOf(this)
