package com.github.itsempa.nautilus.features.dev

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.features.fishing.FishingApi.isLavaRod
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.repoItemName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalNames
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

            val lavaRods: Set<NeuInternalName> = setOf(
                "STARTER_LAVA_ROD",
                "INFERNO_ROD",
                "MAGMA_ROD",
                "HELLFIRE_ROD",
                "POLISHED_TOPAZ_ROD"
            ).toInternalNames()

            val lavaRodArgument = InternalNameArgumentType.itemName(lavaRods, showWhenEmpty = true, isGreedy = true)

            argCallback("lavaRod", lavaRodArgument) { lavaRod ->
                ChatUtils.chat("Lava rod selected: ${lavaRod.repoItemName}")
            }
        }
    }

    @HandleEvent
    fun onKill(event: KillEvent) {
        NautilusChat.debug("Kill event: ${event.kills} kills")
    }

}
