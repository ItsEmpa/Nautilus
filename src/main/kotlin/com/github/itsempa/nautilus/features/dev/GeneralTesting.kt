package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.features.fishing.FishingApi.isLavaRod
import at.hannibal2.skyhanni.utils.NeuInternalName
import com.github.itsempa.nautilus.commands.brigadier.arguments.InternalNameArgumentType
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.modules.DevModule
import com.github.itsempa.nautilus.utils.NautilusChat

@DevModule
object GeneralTesting {

    private fun isValid(internalName: NeuInternalName): Boolean {
        return internalName.isLavaRod()
    }

    @HandleEvent
    fun onBrigadier(event: BrigadierRegisterEvent) {
        event.register("ntbrigadier") {
            aliases = listOf("ntbd")
            category = CommandCategory.DEVELOPER_TEST

            argCallback("item", InternalNameArgumentType.itemName(::isValid)) { item ->
                NautilusChat.debug("Item: $item")
            }
        }
    }

    @HandleEvent
    fun onKill(event: KillEvent) {
        NautilusChat.debug("Kill event: ${event.kills} kills")
    }

}
