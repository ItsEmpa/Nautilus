package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.features.fishing.FishingApi
import com.github.itsempa.nautilus.modules.Module

@Module
object FeeshApi {

    // TODO: make own handling of fishing detection
    val isFishing: Boolean get() = FishingApi.isFishing(false)

}
