package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.LorenzVec

class FishCatchEvent(val sinceMove: Int, val bobberPos: LorenzVec) : SkyHanniEvent()
