package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.chat.TextHelper
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.commands.brigadier.BaseBrigadierBuilder
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments.getString
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.suggestion.SuggestionProvider

@Module
object NautilusUsageCommand {

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        val commandNameSuggestions = SuggestionProvider<Any?> { _, builder ->
            val string = builder.remainingLowerCase
            for (command in event.commands) {
                names@ for (name in command.getAllNames()) {
                    if (name.startsWith(string)) {
                        builder.suggest(name)
                        continue@names
                    }
                }
            }
            builder.buildFuture()
        }

        event.register("ntcommandusage") {
            this.aliases = listOf("ntusage", "ntcmdusage")
            this.description = "Shows the usage of a command."
            this.category = CommandCategory.MAIN

            thenCallback("command", BrigadierArguments.string(), commandNameSuggestions) {
                val commandName = getString("command") ?: return@thenCallback
                val command = event.commands.find { commandName in it.getAllNames() } ?: run {
                    NautilusChat.userError("Command '$commandName' not found.")
                    return@thenCallback
                }
                val brigadier = command as? BaseBrigadierBuilder ?: run {
                    NautilusChat.userError("Command '$commandName' doest not have usage.")
                    return@thenCallback
                }
                brigadier.sendHelpMessage(event.dispatcher)
            }
        }
    }

    private fun BaseBrigadierBuilder.sendHelpMessage(dispatcher: CommandDispatcher<Any?>) {
        val map = dispatcher.getSmartUsage(node, null)
        TextHelper.displayPaginatedList(
            "§3${Nautilus.MOD_NAME} Command Usage: §f/${node.name}",
            map.values.toList(),
            chatLineId = this.hashCode(),
            emptyMessage = "No usage found.",
            currentPage = 1,
            maxPerPage = 15,
        ) { usage ->
            TextHelper.text("§7 - §e/${node.name} $usage")
        }
    }

}
