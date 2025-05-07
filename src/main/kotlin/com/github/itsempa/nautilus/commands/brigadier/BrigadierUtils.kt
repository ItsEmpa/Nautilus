package com.github.itsempa.nautilus.commands.brigadier

import com.mojang.brigadier.suggestion.SuggestionProvider

object BrigadierUtils {

    fun Collection<String>.toSuggestionProvider() = SuggestionProvider<Any?> { _, builder ->
        for (s in this) {
            if (s.startsWith(builder.remainingLowerCase)) {
                builder.suggest(s)
            }
        }
        builder.buildFuture()
    }
}
