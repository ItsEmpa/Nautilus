package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.commands.CommandBuilder
import com.github.itsempa.nautilus.commands.NautilusCommands
import net.minecraftforge.client.ClientCommandHandler

object NautilusCommandRegistrationEvent : SkyHanniEvent() {
    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val info = CommandBuilder(name).apply(block)
        val names = info.allNames().toSet()
        if (NautilusCommands.commandsList.any { it.allNames().intersect(names).isNotEmpty() }) {
            error("The command '$name is already registered!'")
        }
        ClientCommandHandler.instance.registerCommand(info.toCommand())
        NautilusCommands.commandsList.add(info)
    }
    private fun CommandBuilder.allNames(): List<String> = aliases + name
}
