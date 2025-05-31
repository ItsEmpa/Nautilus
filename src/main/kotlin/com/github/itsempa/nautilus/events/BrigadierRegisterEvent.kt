package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import com.github.itsempa.nautilus.commands.CommandData
import com.github.itsempa.nautilus.commands.NautilusCommandRegistry.addToRegister
import com.github.itsempa.nautilus.commands.NautilusCommandRegistry.checkDescriptionAndCategory
import com.github.itsempa.nautilus.commands.NautilusCommandRegistry.hasUniqueName
import com.github.itsempa.nautilus.commands.brigadier.BaseBrigadierBuilder
import com.mojang.brigadier.CommandDispatcher

class BrigadierRegisterEvent(
    private val builders: MutableList<CommandData>,
    val dispatcher: CommandDispatcher<Any?>,
) : SkyHanniEvent() {

    val commands: List<CommandData> get() = builders

    fun register(name: String, builder: BaseBrigadierBuilder.() -> Unit) {
        val command = BaseBrigadierBuilder(name).apply(builder)
        command.hasUniqueName()
        command.checkDescriptionAndCategory()
        command.addToRegister(dispatcher)
    }
}

