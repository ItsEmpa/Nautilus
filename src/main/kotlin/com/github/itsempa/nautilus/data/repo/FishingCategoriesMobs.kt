package com.github.itsempa.nautilus.data.repo

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.repo.json.FishingCategoriesMobsJson
import com.github.itsempa.nautilus.data.repo.json.FishingCategoryMobsDataJson
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.events.NautilusRepositoryReloadEvent
import me.owdding.ktmodules.Module

@Module
object FishingCategoriesMobs {

    var categoriesMobData: Map<FishingCategory, FishingCategoryMobsDataJson> = emptyMap()
        private set

    @HandleEvent
    fun onRepoLoad(event: NautilusRepositoryReloadEvent) {
        val data = event.getConstant<FishingCategoriesMobsJson>("FishingCategoriesMobs")
        categoriesMobData = data.categories
    }

    fun FishingCategory.getMobs(): List<String> = categoriesMobData[this]?.mobs.orEmpty()

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("FishingCategoriesMobs")
        event.addIrrelevant(
            "categoriesMobData" to categoriesMobData.entries,
        )
    }

}
