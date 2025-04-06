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

@Module
object NautilusCommands {

    private fun getOpenMainMenu(args: Array<String>) {
        if (args.isNotEmpty()) {
            if (args[0].lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            } else openConfigGui(args.joinToString(" "))
        } else openConfigGui()
    }

    val commandsList = mutableListOf<CommandBuilder>()

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("nautilus") {
            this.aliases = listOf("nt", "nautilusconfig", "ntconfig")
            this.category = CommandCategory.MAIN
            this.description = "Opens the main ${Nautilus.MOD_NAME} config"
            callback(::getOpenMainMenu)
        }
        event.register("nautiluscommands") {
            this.aliases = listOf("ntcommands", "nautilushelp", "nthelp")
            this.description = "Shows this list"
            this.category = CommandCategory.MAIN
            callback(NautilusHelpCommand::onCommand)
        }
        event.register("nautilussaveconfig") {
            this.aliases = listOf("ntsaveconfig")
            this.description = "Saves the config"
            this.category = CommandCategory.DEVELOPER_TEST
            callback { ConfigManager.save() }
        }
    }

    private fun openConfigGui(search: String? = null) {
        val editor = ConfigManager.getEditor()

        search?.let { editor.search(search) }
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}
