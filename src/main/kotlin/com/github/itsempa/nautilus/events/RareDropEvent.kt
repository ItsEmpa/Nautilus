package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.data.RareDropStat
import com.github.itsempa.nautilus.data.RareDropType

data class RareDropEvent(
    val internalName: NeuInternalName,
    val amount: Int,
    val dropType: RareDropType,
    val magicFind: Int?,
    val dropStat: RareDropStat?,
) : SkyHanniEvent()
