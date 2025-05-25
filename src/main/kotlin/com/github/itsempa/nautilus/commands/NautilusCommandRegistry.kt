package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addOrInsert
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.events.NautilusPreInitFinishedEvent
import com.github.itsempa.nautilus.utils.anyIntersects
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
    }

    fun CommandData.hasUniqueName() {
        val names = getAllNames().toSet()
        require(builders.none { it.getAllNames().anyIntersects(names) }) {
            "The command '$name' is already registered!"
        }
    }

    fun CommandData.checkDescriptionAndCategory() {
        require(descriptor.isNotEmpty() || category in CommandCategory.developmentCategories) {
            "The command '$name' has no required description"
        }
    }

    fun CommandData.addToRegister(dispatcher: CommandDispatcher<Any?>) {
        val command = toCommand(dispatcher)
        ClientCommandHandler.instance.registerCommand(command)
        addBuilder()
    }

    private fun CommandData.addBuilder() {
        val comparator = compareBy(CommandData::category, CommandData::name)
        for ((i, command) in builders.withIndex()) {
            val comparison = comparator.compare(this, command)
            if (comparison < 0) {
                builders.addOrInsert(i, this)
                return
            }
        }
        builders.add(this)
    }
}
