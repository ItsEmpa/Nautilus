package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.TimeUtils
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.modules.DevModule
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusTimeUtils.customFormat

@DevModule
object GeneralTesting {

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        event.register("nttimetest") {
            description = "Test time formatting."
            category = CommandCategory.DEVELOPER_TEST

            argCallback("time", BrigadierArguments.greedyString()) { string ->
                val duration = TimeUtils.getDuration(string)
                NautilusChat.chat("Duration: ${duration.customFormat(showDeciseconds = false, maxUnits = 3)}")
            }
        }
    }

    @HandleEvent
    fun onKill(event: KillEvent) {
        NautilusChat.debug("Kill event: ${event.kills} kills")
    }

}
