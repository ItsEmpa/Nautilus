package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.modules.DevModule
import com.github.itsempa.nautilus.utils.NautilusChat

@DevModule
object GeneralTesting {

    @HandleEvent
    fun onKill(event: KillEvent) {
        NautilusChat.debug("Kill event: ${event.kills} kills")
    }

}
