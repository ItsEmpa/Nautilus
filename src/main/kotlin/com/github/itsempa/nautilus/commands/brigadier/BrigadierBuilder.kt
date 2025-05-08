package com.github.itsempa.nautilus.commands.brigadier

import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.commands.CommandData
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils.toSuggestionProvider
import com.github.itsempa.nautilus.utils.NautilusUtils.hasWhitespace
import com.github.itsempa.nautilus.utils.NautilusUtils.splitLastWhitespace
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.command.ICommand

typealias LiteralCommandBuilder = BrigadierBuilder<LiteralArgumentBuilder<Any?>>
typealias ArgumentCommandBuilder<T> = BrigadierBuilder<RequiredArgumentBuilder<Any?, T>>

class BaseBrigadierBuilder(override val name: String) : CommandData, BrigadierBuilder<LiteralArgumentBuilder<Any?>>(
    LiteralArgumentBuilder.literal<Any?>(name),
) {
    var description: String = ""
    override var aliases: List<String> = emptyList()
    override var category: CommandCategory = CommandCategory.MAIN

    override val descriptor: String
        get() = description

    lateinit var node: CommandNode<Any?>

    override fun toCommand(dispatcher: CommandDispatcher<Any?>): ICommand = BrigadierCommand(this, dispatcher)
}

open class BrigadierBuilder<B : ArgumentBuilder<Any?, B>>(
    val builder: ArgumentBuilder<Any?, B>,
) {

    fun callback(block: ArgContext.() -> Unit) {
        this.builder.executes {
            block(ArgContext(it))
            1
        }
    }

    fun simpleCallback(block: () -> Unit) {
        this.builder.executes {
            block()
            1
        }
    }

    fun legacyCallbackArgs(block: (Array<String>) -> Unit) {
        argCallback("allArgs", BrigadierArguments.greedyString()) { allArgs ->
            block(allArgs.split(" ").toTypedArray())
        }
        simpleCallback { block(emptyArray()) }
    }

    fun literal(vararg names: String, action: LiteralCommandBuilder.() -> Unit) {
        for (name in names) {
            if (name.hasWhitespace()) {
                val (prevLiteral, nextLiteral) = name.splitLastWhitespace()
                literal(prevLiteral) {
                    literal(nextLiteral) {
                        action(this)
                    }
                }
                continue
            }
            val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name))
            builder.action()
            this.builder.then(builder.builder)
        }
    }

    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        crossinline action: ArgumentCommandBuilder<T>.(BrigadierArgument<T>) -> Unit,
    ) = arg(name, argument, suggestions.toSuggestionProvider(), action)

    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        crossinline action: ArgumentCommandBuilder<T>.(BrigadierArgument<T>) -> Unit,
    ) {
        if (!name.hasWhitespace()) {
            internalArg(name, argument, suggestions) { action(BrigadierArgument.of(name)) }
            return
        }
        val (literalNames, argName) = name.splitLastWhitespace()
        literal(literalNames) {
            internalArg(argName, argument, suggestions) { action(BrigadierArgument.of<T>(argName)) }
        }
    }

    fun <T> internalArg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ) {
        if (name.hasWhitespace()) {
            val (prevLiteral, nextLiteral) = name.splitLastWhitespace()
            literal(prevLiteral) {
                internalArg(nextLiteral, argument, suggestions, action)
            }
            return
        }
        val builder = BrigadierBuilder(
            RequiredArgumentBuilder.argument<Any?, T>(name, argument).apply {
                if (suggestions != null) suggests(suggestions)
            },
        )
        builder.action()
        this.builder.then(builder.builder)
    }

    fun literalCallback(
        vararg names: String,
        block: ArgContext.() -> Unit,
    ) = literal(*names) { callback(block) }

    inline fun <reified T> argCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        crossinline block: ArgContext.(T) -> Unit,
    ) = arg(name, argument, suggestions) { callback { block(getArg(it)) } }

    inline fun <reified T> argCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        crossinline callback: ArgContext.(T) -> Unit,
    ) = arg(name, argument, suggestions) { callback { callback(getArg(it)) } }

}
