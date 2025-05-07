package com.github.itsempa.nautilus.commands.brigadier

import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.commands.CommandData
import com.github.itsempa.nautilus.commands.brigadier.BrigadierUtils.toSuggestionProvider
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
        callback { block(emptyArray()) }
    }

    fun literal(vararg names: String, action: LiteralCommandBuilder.() -> Unit): BrigadierBuilder<B> {
        for (name in names) {
            if (name.contains(" ")) {
                val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name.substringBefore(" ")))
                builder.literal(name.substringAfter(" "), action = action)
                this.builder.then(builder.builder)
                continue
            }
            val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name))
            builder.action()
            this.builder.then(builder.builder)
        }
        return this
    }

    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        crossinline action: ArgumentCommandBuilder<T>.(BrigadierArgument<T>) -> Unit,
    ): BrigadierBuilder<B> = arg(name, argument, suggestions.toSuggestionProvider(), action)

    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        crossinline action: ArgumentCommandBuilder<T>.(BrigadierArgument<T>) -> Unit,
    ): BrigadierBuilder<B> {
        if (!name.contains("  ")) {
            return internalArg(name, argument, suggestions) { action(BrigadierArgument(name, T::class.java)) }
        }
        val split = name.split(" ")
        val beforeArg = split.subList(0, split.size - 1).joinToString(" ")
        val argName = split.last()
        return internalArg(beforeArg, argument, suggestions) { action(BrigadierArgument(argName, T::class.java)) }
    }

    fun <T> internalArg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ): BrigadierBuilder<B> {
        if (name.contains(" ")) {
            val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name.substringBefore(" ")))
            builder.internalArg(name.substringAfter(" "), argument, suggestions, action)
            this.builder.then(builder.builder)
            return this
        }
        val builder = BrigadierBuilder(
            RequiredArgumentBuilder.argument<Any?, T>(name, argument).apply {
                if (suggestions != null) suggests(suggestions)
            },
        )
        builder.action()
        this.builder.then(builder.builder)
        return this
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
