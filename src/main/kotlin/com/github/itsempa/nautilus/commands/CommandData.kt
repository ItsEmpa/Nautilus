package com.github.itsempa.nautilus.commands

import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.ICommand

interface CommandData {
    val name: String
    var aliases: List<String>
    var category: CommandCategory
    val descriptor: String

    fun toCommand(dispatcher: CommandDispatcher<Any?>): ICommand
}
