package com.github.itsempa.nautilus.data.core

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.test.command.ErrorManager.CachedError
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import kotlin.time.Duration.Companion.minutes

/**
 * Taken from SkyHanni
 */
object NautilusErrorManager {

    // random id -> error message
    private val errorMessages = mutableMapOf<String, String>()
    private val fullErrorMessages = mutableMapOf<String, String>()
    private val cache = TimeLimitedSet<CachedError>(10.minutes)

    private val breakAfter = listOf(
        "at at.hannibal2.skyhanni.config.commands.Commands\$createCommand",
        "at net.minecraftforge.fml.common.eventhandler.EventBus.post",
        "at at.hannibal2.skyhanni.mixins.hooks.NetHandlerPlayClientHookKt.onSendPacket",
        "at net.minecraft.client.main.Main.main",
        "at.hannibal2.skyhanni.api.event.EventListeners.createZeroParameterConsumer",
        "at.hannibal2.skyhanni.api.event.EventListeners.createSingleParameterConsumer",
    )

    private val replace = mapOf(
        "at.hannibal2.skyhanni." to "SH.",
        "io.moulberry.notenoughupdates." to "NEU.",
        "net.minecraft." to "MC.",
        "net.minecraftforge.fml." to "FML.",
        "com.github.itsempa.nautilus." to "NT.",
    )


    private val replaceEntirely = mapOf(
        "at.hannibal2.skyhanni.api.event.EventListeners.createZeroParameterConsumer" to "<Skyhanni event post>",
        "at.hannibal2.skyhanni.api.event.EventListeners.createSingleParameterConsumer" to "<Skyhanni event post>",
    )

    private val ignored = listOf(
        "at java.lang.Thread.run",
        "at java.util.concurrent.",
        "at java.lang.reflect.",
        "at net.minecraft.network.",
        "at net.minecraft.client.Minecraft.addScheduledTask(",
        "at net.minecraftforge.fml.common.network.handshake.",
        "at net.minecraftforge.fml.common.eventhandler.",
        "at net.fabricmc.devlaunchinjector.",
        "at io.netty.",
        "at com.google.gson.internal.",
        "at sun.reflect.",

        "at at.hannibal2.skyhanni.config.commands.SimpleCommand.",
        "at at.hannibal2.skyhanni.config.commands.Commands\$createCommand\$1.processCommand",
        "at at.hannibal2.skyhanni.test.command.ErrorManager.logError",
        "at at.hannibal2.skyhanni.test.command.ErrorManager.skyHanniError",
        "at at.hannibal2.skyhanni.api.event.SkyHanniEvent.post",
        "at at.hannibal2.skyhanni.api.event.EventHandler.post",
        "at net.minecraft.launchwrapper.",

        "at com.github.itsempa.nautilus.commands.brigadier.BrigadierCommand",
        "at com.github.itsempa.nautilus.data.core.NautilusErrorManager.logError",
        "at com.github.itsempa.nautilus.data.core.NautilusErrorManager.nautilusError",
    )

    private val skipErrorEntry = emptyMap<String, List<String>>()

    @HandleEvent
    fun onCommand(event: BrigadierRegisterEvent) {
        event.register("ntreseterrorcache") {
            description = "Resets the cache of errors."
            category = CommandCategory.DEVELOPER_TEST
            callback {
                cache.clear()
                NautilusChat.chat("Error cache reset.")
            }
        }
    }

    private var cachedExtraData: String? = null

    fun nautilusError(message: String, vararg extraData: Pair<String, Any?>): Nothing {
        buildExtraDataString(extraData)?.let {
            cachedExtraData = it
        }
        throw IllegalStateException(message.removeColor())
    }

    private fun copyError(errorId: String) {
        val fullErrorMessage = KeyboardManager.isModifierKeyDown()
        val errorMessage = if (fullErrorMessage) {
            fullErrorMessages[errorId]
        } else {
            errorMessages[errorId]
        }
        val name = if (fullErrorMessage) "Full error" else "Error"
        NautilusChat.chat(
            errorMessage?.let {
                OSUtils.copyToClipboard(it)
                "$name copied into the clipboard, please report it on the Nautilus discord!"
            } ?: "Error id not found!",
        )
    }

    // just log for debug cases
    fun logErrorStateWithData(
        userMessage: String,
        internalMessage: String,
        vararg extraData: Pair<String, Any?>,
        ignoreErrorCache: Boolean = false,
        noStackTrace: Boolean = false,
        condition: () -> Boolean = { true },
    ): Boolean {
        if (extraData.isNotEmpty()) {
            cachedExtraData = null
        }
        return logError(
            IllegalStateException(internalMessage),
            userMessage,
            ignoreErrorCache,
            noStackTrace,
            *extraData,
            condition = condition,
        )
    }

    fun logErrorWithData(
        throwable: Throwable,
        message: String,
        vararg extraData: Pair<String, Any?>,
        ignoreErrorCache: Boolean = false,
        noStackTrace: Boolean = false,
    ): Boolean = logError(throwable, message, ignoreErrorCache, noStackTrace, *extraData)

    @JvmStatic
    fun logError(
        originalThrowable: Throwable,
        message: String,
        ignoreErrorCache: Boolean,
        noStackTrace: Boolean,
        vararg extraData: Pair<String, Any?>,
        condition: () -> Boolean = { true },
    ): Boolean {
        val throwable = originalThrowable.maybeSkipError()
        if (!ignoreErrorCache) {
            val cachedError = throwable.stackTrace.getOrNull(0)?.let {
                CachedError(it.fileName ?: "<unknown>", it.lineNumber, message)
            } ?: CachedError("<empty stack trace>", 0, message)
            if (cachedError in cache) return false
            cache.add(cachedError)
        }
        if (!condition()) return false

        Error(message, throwable).printStackTrace()

        val fullStackTrace: String
        val stackTrace: String

        if (noStackTrace) {
            fullStackTrace = "<no stack trace>"
            stackTrace = "<no stack trace>"
        } else {
            fullStackTrace = throwable.getCustomStackTrace(true).joinToString("\n")
            stackTrace = throwable.getCustomStackTrace(false).joinToString("\n")
        }
        val randomId = StringUtils.generateRandomId()

        val extraDataString = getExtraDataOrCached(extraData)
        val rawMessage = message.removeColor()
        errorMessages[randomId] = "```\n${Nautilus.MOD_NAME} ${Nautilus.VERSION}: $rawMessage\n \n$stackTrace\n$extraDataString```"
        fullErrorMessages[randomId] =
            "```\n${Nautilus.MOD_NAME} ${Nautilus.VERSION}: $rawMessage\n(full stack trace)\n \n$fullStackTrace\n$extraDataString```"

        val finalMessage = buildFinalMessage(message)
        fun send() {
            NautilusChat.clickableChat(
                "§c[${Nautilus.MOD_NAME}-${Nautilus.VERSION}]: $finalMessage Click here to copy the error into the clipboard.",
                "§eClick to copy!",
                prefix = false,
            ) {
                copyError(randomId)
            }
        }
        if (McPlayer.exists) send()
        else DelayedRun.runNextTick(::send)
        return true
    }

    private fun getExtraDataOrCached(extraData: Array<out Pair<String, Any?>>): String {
        cachedExtraData?.let {
            cachedExtraData = null
            if (extraData.isEmpty()) {
                return it
            }
        }
        return buildExtraDataString(extraData).orEmpty()
    }

    private fun buildFinalMessage(message: String): String {
        var finalMessage = message

        if (finalMessage.last() !in ".?!") {
            finalMessage += "§c."
        }
        return finalMessage
    }

    private fun buildExtraDataString(extraData: Array<out Pair<String, Any?>>): String? {
        val extraDataString = if (extraData.isNotEmpty()) {
            val builder = StringBuilder()
            for ((key, value) in extraData) {
                builder.append(key)
                builder.append(": ")
                if (value is Iterable<*>) {
                    builder.append("\n")
                    for (line in value) {
                        builder.append(" - '$line'")
                        builder.append("\n")
                    }
                } else {
                    builder.append("'$value'")
                }
                builder.append("\n")
            }
            "\nExtra data:\n$builder"
        } else null
        return extraDataString
    }

    private fun Throwable.getCustomStackTrace(
        fullStackTrace: Boolean,
        parent: List<String> = emptyList(),
    ): List<String> = buildList {
        add("Caused by ${this@getCustomStackTrace.javaClass.name}: $message")

        for (traceElement in stackTrace) {
            val text = "\tat $traceElement"
            if (!fullStackTrace && text in parent) {
                break
            }
            var visualText = text
            if (!fullStackTrace) {
                for ((from, to) in replaceEntirely) {
                    if (visualText.contains(from)) {
                        visualText = to
                        break
                    }
                }
                for ((from, to) in replace) {
                    visualText = visualText.replace(from, to)
                }
            }
            if (!fullStackTrace && breakAfter.any { text.contains(it) }) {
                add(visualText)
                break
            }
            if (ignored.any { text.contains(it) }) continue
            add(visualText)
        }

        if (this === cause) {
            add("<Infinite recurring causes>")
            return@buildList
        }

        cause?.let {
            addAll(it.getCustomStackTrace(fullStackTrace, this))
        }
    }

    private fun Throwable.maybeSkipError(): Throwable {
        val cause = cause ?: return this
        val causeClassName = this@maybeSkipError.javaClass.name
        val breakOnFirstLine = skipErrorEntry[causeClassName]

        for (traceElement in stackTrace) {
            val line = traceElement.toString()
            breakOnFirstLine?.let { list ->
                if (list.any { line.contains(it) }) return cause
            }
        }

        return this
    }

    @JvmStatic
    fun isNautilusStackTrace(throwable: Throwable): Boolean {
        return containsInStackTrace(throwable, Nautilus.CLASS_PATH)
    }

    private fun containsInStackTrace(throwable: Throwable, target: String): Boolean {
        if (throwable.message?.contains(target) == true) return true
        if (throwable.stackTrace.any { it.toString().contains(target) }) return true
        var cause = throwable.cause
        while (cause != null) {
            if (cause.message?.contains(target) == true) return true
            if (cause.stackTrace.any { it.toString().contains(target) }) return true
            cause = cause.cause
        }

        return false
    }

}
