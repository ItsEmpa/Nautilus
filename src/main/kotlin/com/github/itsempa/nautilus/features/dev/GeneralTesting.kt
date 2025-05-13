package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.commands.brigadier.arguments.ItemNameArgumentType
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat

@Module(devOnly = true)
object GeneralTesting {

    @HandleEvent
    fun onBrigadier(event: BrigadierRegisterEvent) {
        event.register("ntbrigadier") {
            aliases = listOf("ntbd")
            category = CommandCategory.DEVELOPER_TEST

            argCallback("item", ItemNameArgumentType.itemName()) { item ->
                NautilusChat.debug("Item: $item")
            }
        }
    }

    @HandleEvent
    fun onKill(event: KillEvent) {
        NautilusChat.debug("Kill event: ${event.kills} kills")
    }

}
