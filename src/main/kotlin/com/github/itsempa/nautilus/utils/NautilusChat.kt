package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.chat.TextHelper
import at.hannibal2.skyhanni.utils.chat.TextHelper.asComponent
import at.hannibal2.skyhanni.utils.chat.TextHelper.prefix
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat
import at.hannibal2.skyhanni.utils.compat.addChatMessageToChat
import com.github.itsempa.nautilus.Nautilus
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

@Suppress("unused")
object NautilusChat {
    private const val DEBUG_PREFIX_NO_COLOR = "[${Nautilus.MOD_NAME} Debug] "
    private const val DEBUG_PREFIX = "$DEBUG_PREFIX_NO_COLOR§7"
    private const val USER_ERROR_PREFIX = "§c[${Nautilus.MOD_NAME}] "
    private const val CHAT_PREFIX = "[${Nautilus.MOD_NAME}] "
    const val PREFIX_COLOR = "§3"

    fun prefixComponent(message: String): IChatComponent = TextHelper.text(PREFIX_COLOR + CHAT_PREFIX + message)

    fun debug(message: String) {
        if (Nautilus.feature.about.debug && internalChat(DEBUG_PREFIX + message)) {
            Nautilus.consoleLog(DEBUG_PREFIX_NO_COLOR + message)
        }
    }

    fun userError(message: String) = chat(message, prefixColor = "§c")

    fun chat(message: String, prefix: Boolean = true, prefixColor: String = PREFIX_COLOR) {
        val text = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        ChatUtils.chat(text, false)
    }

    private fun internalChat(message: String): Boolean {
        if (!MinecraftCompat.localPlayerExists) {
            Nautilus.consoleLog(message.removeColor())
            return false
        }

        addChatMessageToChat(message.asComponent())
        return true
    }

    fun clickableChat(
        message: String,
        hover: String = "§eClick here!",
        expireAt: SimpleTimeMark = SimpleTimeMark.farFuture(),
        prefix: Boolean = true,
        prefixColor: String = PREFIX_COLOR,
        oneTimeClick: Boolean = false,
        onClick: () -> Unit,
    ) {
        val text = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        ChatUtils.clickableChat(text, onClick, hover, expireAt, false, oneTimeClick = oneTimeClick)
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
        prefixColor: String = PREFIX_COLOR,
    ) {
        val text = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        ChatUtils.hoverableChat(text, hover, command, false)
    }

    fun clickableLinkChat(
        message: String,
        url: String,
        hover: String = "§eOpen $url",
        autoOpen: Boolean = false,
        prefix: Boolean = true,
        prefixColor: String = PREFIX_COLOR,
    ) {
        val text = (if (prefix) prefixColor + CHAT_PREFIX else "") + message
        ChatUtils.clickableLinkChat(text, url, hover, autoOpen, false)
    }

    fun multiComponentMessage(
        components: List<ChatComponentText>,
        prefix: Boolean = true,
        prefixColor: String = PREFIX_COLOR,
    ) {
        val msgPrefix = if (prefix) prefixColor + CHAT_PREFIX else ""
        chat(TextHelper.join(components).prefix(msgPrefix))
    }

    fun chat(message: IChatComponent, send: Boolean = true): Boolean {
        val formattedMessage = message.formattedText

        if (!MinecraftCompat.localPlayerExists) {
            Nautilus.consoleLog(formattedMessage.removeColor())
            return false
        }

        if (send) addChatMessageToChat(message)
        return true
    }


}
