package com.github.itsempa.nautilus.events.combo

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.data.ComboData

data class ComboUpdateEvent(val combo: Int, val colorCode: Char, val buffs: Map<ComboData.ComboBuff, Int>) : SkyHanniEvent()
