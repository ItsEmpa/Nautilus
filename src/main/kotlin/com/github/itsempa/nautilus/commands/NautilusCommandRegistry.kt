package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.NautilusPreInitFinishedEvent
import com.github.itsempa.nautilus.utils.enumMapOf
import com.mojang.brigadier.CommandDispatcher
import me.owdding.ktmodules.Module
import net.minecraftforge.client.ClientCommandHandler

@Module
object NautilusCommandRegistry {
    private val builders = mutableListOf<CommandData>()
    val commands: List<CommandData> get() = builders

    private val dispatcher: CommandDispatcher<Any?> = CommandDispatcher()

    @HandleEvent
    fun onPreInitFinished(event: NautilusPreInitFinishedEvent) {
        BrigadierRegisterEvent(builders, dispatcher).post()

        // Reorder the commands by category
        val map = enumMapOf<CommandCategory, MutableList<CommandData>>()
        for (command in builders) map.getOrPut(command.category, ::ArrayList).add(command)
        builders.clear()
        map.values.forEach { list -> list.forEach { command -> builders.add(command) } }
    }


    private fun String.isUnique() {
        if (builders.any { it.name == this || it.aliases.contains(this) }) {
            error("The command '$this is already registered!'")
        }
    }

    fun CommandData.hasUniqueName() {
        name.isUnique()
        aliases.forEach { it.isUnique() }
    }

    fun CommandData.addToRegister(dispatcher: CommandDispatcher<Any?>) {
        val command = toCommand(dispatcher)
        ClientCommandHandler.instance.registerCommand(command)
        builders.add(this)
    }
}
