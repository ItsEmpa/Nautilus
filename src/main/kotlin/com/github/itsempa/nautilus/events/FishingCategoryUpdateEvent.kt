package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.categories.FishingCategory

@Suppress("CanBeParameter")
class FishingCategoryUpdateEvent(
    val active: Set<FishingCategory>,
    oldActive: Set<FishingCategory>,
) : SkyHanniEvent() {
    val added: Set<FishingCategory> = active - oldActive
    val removed: Set<FishingCategory> = oldActive - active
}
