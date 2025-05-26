package com.github.itsempa.nautilus.utils.tracker

import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName

enum class TrackerDisplayMode(displayName: String? = null) {
    TOTAL,
    SESSION("This Session"),
    CUSTOM_SESSION,
    ;

    val displayName: String = displayName ?: toFormattedName()
}
