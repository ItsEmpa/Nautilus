package com.github.itsempa.nautilus.commands.brigadier

import com.github.itsempa.nautilus.data.NautilusErrorManager
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.tryOrDefault
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class BrigadierCommand(
    root: BaseBrigadierBuilder,
    private val dispatcher: CommandDispatcher<Any?>
) : CommandBase() {
    private val aliases: List<String> = root.aliases
    private val node: CommandNode<Any?>

    init {
        val builder = root.builder
        node = builder.build()
        root.node = node
        dispatcher.register(builder as LiteralArgumentBuilder<Any?>)
    }

    override fun getCommandName(): String = node.name
    override fun getCommandUsage(sender: ICommandSender): String = "/${node.name}"

    override fun getCommandAliases() = aliases
    override fun canCommandSenderUseCommand(sender: ICommandSender) = true

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val input = if (args.isEmpty()) node.name else "${node.name} ${args.joinToString(" ")}"
        try {
            dispatcher.execute(input, sender)
        } catch (e: CommandSyntaxException) {
            NautilusChat.userError(e.message ?: "Error when parsing command.")
        } catch (e: Exception) {
            NautilusErrorManager.logErrorWithData(e, "Failed to execute command")
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos
    ): List<String> {
        val input = if (args.isEmpty()) node.name else "${node.name} ${args.joinToString(" ")}"
        return tryOrDefault(emptyList()) {
            dispatcher.getCompletionSuggestions(dispatcher.parse(input, sender)).get().list.map { it.text }
        }
    }

}
