package com.github.itsempa.nautilus.commands.brigadier

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import com.github.itsempa.nautilus.utils.NautilusUtils.hasWhitespace
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

object BrigadierUtils {

    const val DOUBLE_QUOTE = '"'

    fun Collection<String>.toSuggestionProvider(shouldEscape: Boolean = true) = SuggestionProvider<Any?> { _, builder ->
        if (shouldEscape) builder.addOptionalEscaped(this)
        else builder.addUnescaped(this)
        builder.buildFuture()
    }

    /** The same as [StringReader.readString], except it doesn't accept escaping with `'`. */
    fun StringReader.readOptionalDoubleQuotedString(): String {
        if (!canRead()) return ""
        return if (peek() == DOUBLE_QUOTE) {
            skip()
            readStringUntil(DOUBLE_QUOTE)
        } else readUnquotedString()
    }

    /** The same as [StringReader.readQuotedString], except it doesn't accept escaping with `'`. */
    fun StringReader.readDoubleQuotedString(): String {
        if (!canRead()) return ""
        if (peek() != DOUBLE_QUOTE) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(this)
        skip()
        return readStringUntil(DOUBLE_QUOTE)
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
        val withSpaces = input.replace("_", " ")

        fun NeuInternalName.handleItem(): Any = when {
            !isKnownItem() -> ItemParsingFail.UNKNOWN_ITEM
            !isValidItem(this) -> ItemParsingFail.DISALLOWED_ITEM
            else -> this
        }

        return aliases[withSpaces]?.handleItem()
            ?: NeuInternalName.fromItemNameOrInternalName(withSpaces).handleItem()
    }

    // TODO: add support for 1.21
    fun SuggestionsBuilder.addOptionalEscaped(
        collection: Collection<String>,
    ): SuggestionsBuilder {
        if (collection.isEmpty()) return this
        val input = remainingLowerCase
        val isEscaped = input.firstOrNull() == DOUBLE_QUOTE
        val escaped = if (isEscaped) input.drop(1) else input
        val lastWhitespace = escaped.lastIndexOf(' ')
        for (string in collection) {
            if (lastWhitespace == -1) {
                if (isEscaped || string.hasWhitespace()) suggest("$DOUBLE_QUOTE$string$DOUBLE_QUOTE")
                else suggest(string)
            } else {
                val suggestion = string.substring(lastWhitespace + 1)
                if (suggestion.isBlank()) suggest("$DOUBLE_QUOTE")
                else suggest("$suggestion$DOUBLE_QUOTE")
            }
        }
        return this
    }

    // TODO: add support for 1.21
    fun SuggestionsBuilder.addEscaped(
        collection: Collection<String>,
    ): SuggestionsBuilder {
        if (collection.isEmpty()) return this
        val input = remainingLowerCase
        val escaped = input.drop(1)
        val lastWhitespace = escaped.lastIndexOf(' ')
        for (string in collection) {
            if (lastWhitespace == -1) {
                suggest("$DOUBLE_QUOTE$string$DOUBLE_QUOTE")
            } else {
                val suggestion = string.substring(lastWhitespace + 1)
                if (suggestion.isBlank()) suggest("$DOUBLE_QUOTE")
                else suggest("$suggestion$DOUBLE_QUOTE")
            }
        }
        return this
    }

    // TODO: add support for 1.21
    fun SuggestionsBuilder.addUnescaped(
        collection: Collection<String>,
    ): SuggestionsBuilder {
        if (collection.isEmpty()) return this
        val input = remainingLowerCase
        val lastWhitespace = input.lastIndexOf(' ')
        for (string in collection) {
            if (lastWhitespace == -1) suggest(string)
            else {
                val suggestion = string.substring(lastWhitespace + 1)
                if (suggestion.isNotBlank()) suggest(suggestion)
            }
        }
        return this
    }

    fun parseItemNameTabComplete(
        input: String,
        builder: SuggestionsBuilder,
        limit: Int = 200,
        isValidItem: (NeuInternalName) -> Boolean = { true },
    ): CompletableFuture<Suggestions> {
        val first = input.firstOrNull() ?: return builder.buildFuture()
        val unEscaped = if (first == DOUBLE_QUOTE) input.drop(1) else input
        if (unEscaped.isBlank()) return builder.buildFuture()

        val lowercaseStart = unEscaped.replace("_", " ")
        val items = NeuItems.findItemNameStartingWithWithoutNPCs(lowercaseStart, isValidItem).take(limit)

        return builder.addOptionalEscaped(items).buildFuture()
    }
}
