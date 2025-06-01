package com.github.itsempa.nautilus.data.repo.json

import at.hannibal2.skyhanni.utils.KSerializable
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.google.gson.annotations.Expose

@KSerializable
data class FishingCategoriesMobsJson(
    @Expose val categories: Map<FishingCategory, FishingCategoryMobsDataJson>,
)

@KSerializable
data class FishingCategoryMobsDataJson(
    @Expose val mobs: List<String>,
)
