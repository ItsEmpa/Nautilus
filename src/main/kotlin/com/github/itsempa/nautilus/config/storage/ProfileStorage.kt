package com.github.itsempa.nautilus.config.storage

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.features.gui.CakeBuffTimer
import com.github.itsempa.nautilus.features.gui.RareDropsTracker
import com.google.gson.annotations.Expose

class ProfileStorage {

    @Expose
    val seaCreaturesSinceDrops: MutableMap<NeuInternalName, RareDropsTracker.RareDropEntry> = mutableMapOf()

    @Expose
    var bestCombo: Int = 0

    // The time represents when the buff will end
    @Expose
    val centuryCakeBuffs: MutableMap<CakeBuffTimer.CenturyCakeBuffs, SimpleTimeMark> = mutableMapOf()

}
