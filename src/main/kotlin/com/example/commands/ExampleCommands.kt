package com.example.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandBuilder
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import com.example.ExampleMod
import com.example.events.ExampleCommandRegistrationEvent
import com.example.modules.Module

@Module
object ExampleCommands {

    private fun getOpenMainMenu(args: Array<String>) {
        if (args.isNotEmpty()) {
            if (args[0].lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            } else openConfigGui(args.joinToString(" "))
        } else openConfigGui()
    }

    val commandsList = mutableListOf<CommandBuilder>()

    @HandleEvent
    fun onCommandRegistration(event: ExampleCommandRegistrationEvent) {
        event.register("examplemod") {
            this.aliases = listOf("examplemodconfig")
            this.category = CommandCategory.MAIN
            this.description = "Opens the main ${ExampleMod.MOD_NAME} config"
            callback(::getOpenMainMenu)
        }
        event.register("examplemodcommands") {
            this.description = "Shows this list"
            this.category = CommandCategory.MAIN
            callback(ExampleHelpCommand::onCommand)
        }
        event.register("examplemodsaveconfig") {
            this.description = "Saves the config"
            this.category = CommandCategory.DEVELOPER_TEST
            callback { ExampleMod.managedConfig.saveToFile() }
        }
    }

    private fun openConfigGui(search: String? = null) {
        val editor = ExampleMod.managedConfig.getEditor()

        search?.let { editor.search(search) }
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}
