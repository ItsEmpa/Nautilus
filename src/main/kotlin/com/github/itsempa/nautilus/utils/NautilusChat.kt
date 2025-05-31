package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.chat.TextHelper.asComponent
import at.hannibal2.skyhanni.utils.chat.TextHelper.onClick
import at.hannibal2.skyhanni.utils.chat.TextHelper.prefix
import at.hannibal2.skyhanni.utils.chat.TextHelper.send
import at.hannibal2.skyhanni.utils.compat.command
import at.hannibal2.skyhanni.utils.compat.hover
import at.hannibal2.skyhanni.utils.compat.url
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.helpers.McClient
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

@Suppress("unused")
object NautilusChat {
    private const val DEBUG_PREFIX_NO_COLOR = "[${Nautilus.MOD_NAME} Debug] "
    private const val DEBUG_PREFIX = "$DEBUG_PREFIX_NO_COLOR§7"
    private const val CHAT_PREFIX = "[${Nautilus.MOD_NAME}] "
    private const val USER_ERROR_PREFIX = "§c$CHAT_PREFIX"
    const val COLOR = "§3"

    fun prefixComponent(message: String): IChatComponent = TextHelper.text(COLOR + CHAT_PREFIX + message)

    fun debug(
        message: String,
        replaceSameMessage: Boolean = false,
    ) {
        if (Nautilus.feature.about.debug) {
            internalChat(DEBUG_PREFIX + message, replaceSameMessage)
            Nautilus.consoleLog(DEBUG_PREFIX_NO_COLOR + message)
        }
    }

    fun userError(
        message: String,
        replaceSameMessage: Boolean = false,
    ) = internalChat(USER_ERROR_PREFIX + message, replaceSameMessage)

    fun chat(
        message: String,
        prefix: Boolean = true,
        prefixColor: String = COLOR,
        replaceSameMessage: Boolean = false,
        onlySendOnce: Boolean = false,
        messageId: Int? = null,
    ) {
        val text = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        internalChat(text, replaceSameMessage, onlySendOnce, messageId)
    }

    fun clickableChat(
        message: String,
        hover: String = "§eClick here!",
        expireAt: SimpleTimeMark = SimpleTimeMark.farFuture(),
        prefix: Boolean = true,
        prefixColor: String = COLOR,
        oneTimeClick: Boolean = false,
        replaceSameMessage: Boolean = false,
        onClick: () -> Unit,
    ) {
        val rawText = (if (prefix) prefixColor + CHAT_PREFIX else "") + message

        val text = TextHelper.text(rawText) {
            this.onClick(expireAt, oneTimeClick, onClick)
            this.hover = hover.asComponent()
        }
        if (replaceSameMessage) {
            send(text, getUniqueMessageIdForString(rawText))
        } else send(text)
    }

    fun clickToClipboard(message: String, lines: List<String>) {
        val text = lines.joinToString("\n") { "§7$it" }
        clickableChat(
            "$message §7(hover for info)",
            hover = "$text\n \n§eClick to copy to clipboard!"
        ) { ClipboardUtils.copyToClipboard(text.removeColor()) }
    }

    fun hoverableChat(
        message: String,
        hover: List<String>,
        command: String? = null,
        prefix: Boolean = true,
        prefixColor: String = COLOR,
    ) {
        val rawText = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        send(TextHelper.text(rawText) {
            this.hover = TextHelper.multiline(hover)
            if (command != null) {
                this.command = command
            }
        })
    }

    fun clickableLinkChat(
        message: String,
        url: String,
        hover: String = "§eOpen $url",
        autoOpen: Boolean = false,
        prefix: Boolean = true,
        prefixColor: String = COLOR,
    ) {
        val rawText = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        send(TextHelper.text(rawText) {
            this.url = url
            this.hover = "$prefixColor$hover".asComponent()
        })
        if (autoOpen) OSUtils.openBrowser(url)
    }

    fun multiComponentMessage(
        components: List<ChatComponentText>,
        prefix: Boolean = true,
        prefixColor: String = COLOR,
    ) {
        val msgPrefix = if (prefix) prefixColor + CHAT_PREFIX else ""
        send(TextHelper.join(components).prefix(msgPrefix))
    }

    private val messagesThatAreOnlySentOnce = mutableSetOf<String>()

    private fun internalChat(
        message: String,
        replaceSameMessage: Boolean,
        onlySendOnce: Boolean = false,
        messageId: Int? = null,
    ) {
        val text = message.asComponent()
        if (onlySendOnce) {
            if (message in messagesThatAreOnlySentOnce) return
            messagesThatAreOnlySentOnce.add(message)
        }
        if (replaceSameMessage || messageId != null) {
            send(text, messageId ?: getUniqueMessageIdForString(message))
            Nautilus.consoleLog(message)
        } else send(text)
    }

    private fun send(text: IChatComponent, id: Int = 0) {
        McClient.runOnWorld { text.send(id) }
    }

    private val uniqueMessageIdStorage = mutableMapOf<String, Int>()

    private fun getUniqueMessageIdForString(string: String): Int =
        uniqueMessageIdStorage.getOrPut(string, ::getUniqueMessageId)

    // TODO: find a reason to use number?
    private var lastUniqueMessageId = 223242

    fun getUniqueMessageId() = lastUniqueMessageId++


}
