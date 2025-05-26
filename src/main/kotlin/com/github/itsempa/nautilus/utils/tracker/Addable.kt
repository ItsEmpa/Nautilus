package com.github.itsempa.nautilus.utils.tracker

/** Interface used for adding tracker data objects. */
fun interface Addable<T : Addable<T>> {
    operator fun plus(other: T): T
}
