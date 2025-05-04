package com.github.itsempa.nautilus.config.storage

import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.features.gui.RareDropsTracker
import com.google.gson.annotations.Expose

class ProfileStorage {

    @Expose
    val seaCreaturesSinceDrops: MutableMap<NeuInternalName, RareDropsTracker.RareDropEntry> = mutableMapOf()

    @Expose
    var bestCombo: Int = 0

}
