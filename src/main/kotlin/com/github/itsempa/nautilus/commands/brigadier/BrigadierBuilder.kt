package com.github.itsempa.nautilus.commands.brigadier

import at.hannibal2.skyhanni.config.commands.CommandCategory
import com.github.itsempa.nautilus.commands.CommandData
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments.getString
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.command.ICommand

typealias LiteralCommandBuilder = BrigadierBuilder<LiteralArgumentBuilder<Any?>>
typealias ArgumentCommandBuilder<T> = BrigadierBuilder<RequiredArgumentBuilder<Any?, T>>
typealias ArgContext = CommandContext<*>

class BaseBrigadierBuilder(override val name: String) : CommandData, BrigadierBuilder<LiteralArgumentBuilder<Any?>>(
    LiteralArgumentBuilder.literal<Any?>(name)
) {
    var description: String = ""
    override var aliases: List<String> = emptyList()
    override var category: CommandCategory = CommandCategory.MAIN

    override val descriptor: String
        get() = description

    override fun toCommand(dispatcher: CommandDispatcher<Any?>): ICommand = BrigadierCommand(this, dispatcher)
}

open class BrigadierBuilder<B : ArgumentBuilder<Any?, B>>(
    val builder: ArgumentBuilder<Any?, B>,
) {
    fun callback(callback: ArgContext.() -> Unit) {
        this.builder.executes {
            callback(it)
            1
        }
    }

    fun simpleCallback(callback: () -> Unit) {
        this.builder.executes {
            callback()
            1
        }
    }

    @Deprecated("Use callback function instead")
    fun callbackArgs(callback: (Array<String>) -> Unit) {
        then("allArgs", BrigadierArguments.greedyString()) {
            callback {
                val string = getString("allArgs") ?: return@callback
                val args = string.split(" ").toTypedArray()
                callback(args)
            }
        }
        callback { callback(emptyArray<String>()) }
    }

    fun then(vararg names: String, action: LiteralCommandBuilder.() -> Unit): BrigadierBuilder<B> {
        for (name in names) {
            if (name.contains(" ")) {
                val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name.substringBefore(" ")))
                builder.then(name.substringAfter(" "), action = action)
                this.builder.then(builder.builder)
                continue
            }
            val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name))
            builder.action()
            this.builder.then(builder.builder)
        }
        return this
    }

    fun thenCallback(vararg names: String, callback: ArgContext.() -> Unit) {
        then(*names) { callback(callback) }
    }

    fun <T> then(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ): BrigadierBuilder<B> = then(
        name,
        argument,
        { _, builder ->
            val string = builder.remainingLowerCase
            for (s in suggestions) {
                if (s.startsWith(string)) builder.suggest(s)
            }
            builder.buildFuture()
        },
        action,
    )

    fun <T> thenCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        callback: ArgContext.() -> Unit,
    ) {
        then(name, argument, suggestions) { callback(callback) }
    }

    fun <T> then(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        action: ArgumentCommandBuilder<T>.() -> Unit,
    ): BrigadierBuilder<B> {
        if (name.contains(" ")) {
            val builder = BrigadierBuilder(LiteralArgumentBuilder.literal(name.substringBefore(" ")))
            builder.then(name.substringAfter(" "), argument, suggestions, action)
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

    fun <T> thenCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<Any?>? = null,
        callback: ArgContext.() -> Unit,
    ) {
        then(name, argument, suggestions) { callback(callback) }
    }
}
