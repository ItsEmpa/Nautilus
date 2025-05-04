package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.commands.CommandData
import com.github.itsempa.nautilus.commands.NautilusCommandRegistry.addToRegister
import com.github.itsempa.nautilus.commands.NautilusCommandRegistry.hasUniqueName
import com.github.itsempa.nautilus.commands.brigadier.BaseBrigadierBuilder
import com.mojang.brigadier.CommandDispatcher

class BrigadierRegisterEvent(
    private val builders: MutableList<CommandData>,
    private val dispatcher: CommandDispatcher<Any?>,
) : SkyHanniEvent() {

    val commands: List<CommandData> get() = builders

    fun register(name: String, builder: BaseBrigadierBuilder.() -> Unit) {
        val command = BaseBrigadierBuilder(name).apply(builder)
        command.hasUniqueName()
        if (command.description.isEmpty() && command.category !in CommandCategory.developmentCategories) {
            error("The command '$name' has no description!")
        }
        command.addToRegister(dispatcher)
    }
}

