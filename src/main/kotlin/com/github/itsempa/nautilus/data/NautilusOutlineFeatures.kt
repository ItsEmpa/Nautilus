package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.utils.RecalculatingValue
import com.github.itsempa.nautilus.features.debug.SeaCreatureOwnerHighlight
import kotlin.time.Duration.Companion.seconds

object NautilusOutlineFeatures {

    @JvmStatic
    val anyEnabled by RecalculatingValue(1.seconds, ::computeAnyEnabled)

    // Add functions that use the entity outline renderer here
    private fun computeAnyEnabled(): Boolean {
        return SeaCreatureOwnerHighlight.isEnabled()
    }

}
