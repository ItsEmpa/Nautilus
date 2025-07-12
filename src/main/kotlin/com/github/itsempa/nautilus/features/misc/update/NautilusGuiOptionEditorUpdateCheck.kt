package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.config.core.elements.GuiElementButton
import at.hannibal2.skyhanni.deps.moulconfig.common.RenderContext
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiOptionEditor
import at.hannibal2.skyhanni.deps.moulconfig.gui.KeyboardEvent
import at.hannibal2.skyhanni.deps.moulconfig.gui.MouseEvent
import at.hannibal2.skyhanni.deps.moulconfig.processor.ProcessedOption
import com.github.itsempa.nautilus.Nautilus
import org.lwjgl.input.Mouse

class NautilusGuiOptionEditorUpdateCheck(option: ProcessedOption) : GuiOptionEditor(option) {

    private val button = GuiElementButton()

    override fun render(context: RenderContext, x: Int, y: Int, width: Int) {
        val fr = context.minecraft.defaultFontRenderer

        context.pushMatrix()
        context.translate(x.toFloat() + 10, y.toFloat())
        val adjustedWidth = width - 20
        val nextVersion = UpdateManager.getNextVersion()

        val updateState = UpdateManager.updateState

        button.text = when (updateState) {
            UpdateState.AVAILABLE -> "Download update"
            UpdateState.QUEUED -> "Downloading..."
            UpdateState.DOWNLOADED -> "Downloaded"
            UpdateState.NONE -> if (nextVersion == null) "Check for Updates" else "Up to date"
        }
        button.width = button.getWidth(context)
        button.render(context, getButtonPosition(adjustedWidth), 10)

        if (updateState == UpdateState.DOWNLOADED) {
            val updateText = "§aThe update will be installed after your next restart."
            context.drawStringCenteredScaledMaxWidth(
                updateText,
                fr,
                adjustedWidth / 2F,
                40f,
                true,
                x - fr.getStringWidth(updateText) / 2,
                -1,
            )
        }

        val widthRemaining = adjustedWidth - button.width - 10

        context.scale(2F, 2F)
        val currentVersion = Nautilus.VERSION
        val sameVersion = currentVersion.equals(nextVersion, true)

        val prefix = if (updateState == UpdateState.NONE) "§a" else "§c"
        context.drawStringCenteredScaledMaxWidth(
            "$prefix$currentVersion" +
                if (nextVersion != null && !sameVersion) "➜ §a$nextVersion" else "",
            fr,
            widthRemaining / 4F,
            10F,
            true,
            widthRemaining / 2,
            -1,
        )

        context.popMatrix()
    }

    private fun getButtonPosition(width: Int) = width - button.width
    override fun getHeight() = 55

    // TODO: try to use the MouseEvent
    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int, mouseEvent: MouseEvent): Boolean {
        val adjustedWidth = width - 20
        if (
            Mouse.getEventButtonState() &&
            (mouseX - getButtonPosition(adjustedWidth) - x) in (0..button.width) &&
            (mouseY - 10 - y) in (0..button.height)
        ) {
            when (UpdateManager.updateState) {
                UpdateState.AVAILABLE -> UpdateManager.queueUpdate()
                UpdateState.NONE -> UpdateManager.checkUpdate()
                else -> { /* Empty */ }
            }
            return true
        }
        return false
    }

    override fun keyboardInput(event: KeyboardEvent) = false

    override fun fulfillsSearch(word: String) = super.fulfillsSearch(word) || word in "download" || word in "update"
}
