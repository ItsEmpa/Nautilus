@file:OptIn(ExperimentalContracts::class)

package com.github.itsempa.nautilus.utils

import com.github.itsempa.nautilus.data.NautilusErrorManager
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T> tryOrNull(func: () -> T): T? {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        func()
    } catch (_: Throwable) {
        null
    }
}

inline fun <T> tryOrDefault(default: T, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        func()
    } catch (_: Throwable) {
        default
    }
}

inline fun <T> tryOrDefault(default: () -> T, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        func()
    } catch (_: Throwable) {
        default()
    }
}

inline fun tryCatch(func: () -> Unit) {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    try {
        func()
    } catch (_: Throwable) {

    }
}

inline fun tryError(message: String, func: () -> Unit) {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    try {
        func()
    } catch (e: Throwable) {
        NautilusErrorManager.logErrorWithData(e, message)
    }
}

inline fun tryError(lazyMessage: (Throwable) -> String, func: () -> Unit) {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }
    try {
        func()
    } catch (e: Throwable) {
        NautilusErrorManager.logErrorWithData(e, lazyMessage(e))
    }
}

@Throws(Throwable::class)
inline fun <T> tryThrowError(message: String, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        func()
    } catch (e: Throwable) {
        NautilusErrorManager.logErrorWithData(e, message)
        throw e
    }
}

@Throws(Throwable::class)
inline fun <T> tryThrowError(lazyMessage: (Throwable) -> String, func: () -> T): T {
    contract {
        callsInPlace(func, InvocationKind.AT_MOST_ONCE)
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        func()
    } catch (e: Throwable) {
        NautilusErrorManager.logErrorWithData(e, lazyMessage(e))
        throw e
    }
}

@Throws(IllegalStateException::class)
inline fun <T : Any> errorIfNull(value: T?, lazyMessage: () -> Any): T {
    contract {
        returns() implies (value != null)
    }

    if (value == null) {
        val message = lazyMessage().toString()
        val e = IllegalStateException(message)
        NautilusErrorManager.logErrorWithData(e, message)
        throw e
    } else {
        return value
    }
}
