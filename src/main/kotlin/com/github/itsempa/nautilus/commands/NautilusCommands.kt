package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.config.core.ConfigManager
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import me.owdding.ktmodules.Module

@Module
object NautilusCommands {

    @HandleEvent
    fun onCommandRegister(event: BrigadierRegisterEvent) {
        event.register("nautilus") {
            this.aliases = listOf("nt", "nautilusconfig", "ntconfig")
            this.description = "Opens the main ${Nautilus.MOD_NAME} config"
            this.category = CommandCategory.MAIN

            literalCallback("gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            }

            argCallback("search", BrigadierArguments.greedyString()) { search ->
                openConfigGui(search)
            }

            simpleCallback(::openConfigGui)
        }
        event.register("ntsaveconfig") {
            this.aliases = listOf("nautilussaveconfig")
            this.description = "Saves the config"
            this.category = CommandCategory.DEVELOPER_TEST
            callback { ConfigManager.save() }
        }
        event.register("ntdiscord") {
            this.aliases = listOf("nautilusdiscord")
            this.description = "Opens the Nautilus discord"
            this.category = CommandCategory.USERS_BUG_FIX
            callback {
                NautilusChat.clickableLinkChat(
                    "Click this to open an invite to the Nautilus Discord",
                    Nautilus.DISCORD_INVITE,
                )
            }
        }
        event.register("ntgithub") {
            this.aliases = listOf("nautilusgithub")
            this.description = "Opens the Nautilus GitHub"
            this.category = CommandCategory.USERS_BUG_FIX
            callback {
                NautilusChat.clickableLinkChat(
                    "Click this to open the Nautilus GitHub",
                    Nautilus.GITHUB,
                )
            }
        }
    }

    private fun openConfigGui(search: String? = null) {
        val editor = ConfigManager.editor

        search?.let { editor.search(search) }
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}
