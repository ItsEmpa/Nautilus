package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandBuilder
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.fullEnumMapOf

@Module
object NautilusCommands {

    private fun getOpenMainMenu(args: Array<String>) {
        if (args.isNotEmpty()) {
            if (args.first().lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            } else openConfigGui(args.joinToString(" "))
        } else openConfigGui()
    }

    val commandsList = mutableListOf<CommandBuilder>()

    // Priority is set to the lowest so that all the commands have already been registered when this gets called
    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onFinishCommandRegistration(event: NautilusCommandRegistrationEvent) {
        val map = fullEnumMapOf<CommandCategory, MutableList<CommandBuilder>> { mutableListOf() }
        for (command in commandsList) map[command.category]!!.add(command)
        commandsList.clear()
        map.values.forEach { list -> list.forEach { command -> commandsList.add(command) } }
    }

    // Priority is set to the highest so that these commands always appear at the top
    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("nautilus") {
            this.aliases = listOf("nt", "nautilusconfig", "ntconfig")
            this.description = "Opens the main ${Nautilus.MOD_NAME} config"
            this.category = CommandCategory.MAIN
            callback(::getOpenMainMenu)
        }
        event.register("ntcommands") {
            this.aliases = listOf("nautiluscommands", "nautilushelp", "nthelp")
            this.description = "Shows this list"
            this.category = CommandCategory.MAIN
            callback(NautilusHelpCommand::onCommand)
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
        val editor = ConfigManager.getEditor()

        search?.let { editor.search(search) }
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}
