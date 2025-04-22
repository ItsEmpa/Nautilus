package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.reflect.KProperty
import kotlin.time.Duration

/** Utility class where the stored value gets reset to what's specified in [default] if the value isn't changed in [time] duration. */
class ResettingValue<T>(private val time: Duration, private val default: () -> T) {
    constructor(time: Duration, default: T) : this(time, { default })

    private var lastSet = SimpleTimeMark.farPast()
    private var hasReset = false

    private var currentValue: Any? = UNINITIALIZED_VALUE

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    fun get(): T {
        if (!hasReset && lastSet.passedSince() > time) {
            currentValue = default()
            hasReset = true
        }
        @Suppress("UNCHECKED_CAST")
        return currentValue as T
    }

    fun set(value: T) {
        this.currentValue = value
        hasReset = false
        lastSet = SimpleTimeMark.now()
    }

    fun reset() {
        currentValue = default()
        hasReset = true
        lastSet = SimpleTimeMark.now()
    }

    companion object {
        private val UNINITIALIZED_VALUE = Any()
    }

}
