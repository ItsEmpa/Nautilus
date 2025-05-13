package com.github.itsempa.nautilus.commands.brigadier

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

object BrigadierUtils {

    fun Collection<String>.toSuggestionProvider() = SuggestionProvider<Any?> { _, builder ->
        for (s in this) {
            if (s.startsWith(builder.remainingLowerCase)) {
                builder.suggest(s)
            }
        }
        builder.buildFuture()
    }

    enum class ItemParsingFail {
        UNKNOWN_ITEM,
        DISALLOWED_ITEM,
        EMPTY,
    }

    /** Parses an item (both internal name and item name) into an NeuInternalName. If it fails, it returns an ItemParsingFail instead */
    fun parseItem(
        input: String,
        aliases: Map<String, NeuInternalName> = NeuItems.commonItemAliases.global,
        isValidItem: (NeuInternalName) -> Boolean = { true },
    ): Any {
        if (input.isBlank()) return ItemParsingFail.EMPTY
        val formatted = input.filterNot { StringReader.isQuotedStringStart(it) }
        val withSpaces = formatted.replace("_", " ")

        fun NeuInternalName.handleItem(): Any = when {
            !isKnownItem() -> ItemParsingFail.UNKNOWN_ITEM
            !isValidItem(this) -> ItemParsingFail.DISALLOWED_ITEM
            else -> this
        }

        return aliases[withSpaces]?.handleItem()
            ?: NeuInternalName.fromItemNameOrInternalName(withSpaces).handleItem()
    }

    fun parseItemNameTabComplete(
        input: String,
        builder: SuggestionsBuilder,
        startsWithQuote: Boolean = false, // TODO: figure out if this is needed
        limit: Int = 200,
        isValidItem: (NeuInternalName) -> Boolean = { true },
    ): CompletableFuture<Suggestions> {
        if (input.isBlank()) return builder.buildFuture()

        val lowercaseStart = input.replace("_", " ")
        val items = NeuItems.findItemNameStartingWithWithoutNPCs(lowercaseStart, isValidItem)
        if (items.isEmpty()) return builder.buildFuture()


        for ((i, item) in items.withIndex()) {
            if (i >= limit) break
            val suffix = if (startsWithQuote) "\"" else ""
            builder.suggest(item + suffix)
        }
        return builder.buildFuture()
    }
}
