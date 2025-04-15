package com.github.itsempa.nautilus.data.repo

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.google.gson.annotations.Expose

data class FishingDropsRepo(
    @Expose val items: Map<NeuInternalName, List<String>>,
)
