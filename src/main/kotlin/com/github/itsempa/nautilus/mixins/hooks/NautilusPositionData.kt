package com.github.itsempa.nautilus.mixins.hooks

import at.hannibal2.skyhanni.config.core.config.Position

interface NautilusPositionData {
    var isNautilus: Boolean
    
    companion object {
        @JvmStatic
        @Suppress("CAST_NEVER_SUCCEEDS")
        inline var Position.isNautilus: Boolean get() = (this as NautilusPositionData).isNautilus
            set(value) {
                (this as NautilusPositionData).isNautilus = value
            }

    }
}
