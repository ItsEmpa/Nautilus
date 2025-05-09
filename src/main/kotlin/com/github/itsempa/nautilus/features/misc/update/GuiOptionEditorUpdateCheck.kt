package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.config.core.elements.GuiElementButton
import at.hannibal2.skyhanni.deps.moulconfig.common.RenderContext
import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiOptionEditor
import at.hannibal2.skyhanni.deps.moulconfig.gui.KeyboardEvent
import at.hannibal2.skyhanni.deps.moulconfig.gui.MouseEvent
import at.hannibal2.skyhanni.deps.moulconfig.internal.TextRenderUtils
import at.hannibal2.skyhanni.deps.moulconfig.processor.ProcessedOption
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.helpers.McClient
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumChatFormatting.GREEN
import net.minecraft.util.EnumChatFormatting.RED
import org.lwjgl.input.Mouse

class GuiOptionEditorUpdateCheck(option: ProcessedOption) : GuiOptionEditor(option) {

    private val button = GuiElementButton()

    override fun render(context: RenderContext, x: Int, y: Int, width: Int) {
        val fr = McClient.self.fontRendererObj

        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat() + 10, y.toFloat(), 1F)
        val adjustedWith = width - 20
        val nextVersion = UpdateManager.getNextVersion()

        button.text = when (UpdateManager.updateState) {
            UpdateState.AVAILABLE -> "Download update"
            UpdateState.QUEUED -> "Downloading..."
            UpdateState.DOWNLOADED -> "Downloaded"
            UpdateState.NONE -> if (nextVersion == null) "Check for Updates" else "Up to date"
        }
        button.render(context, getButtonPosition(adjustedWith), 10)

        if (UpdateManager.updateState == UpdateState.DOWNLOADED) {
            TextRenderUtils.drawStringCentered(
                "${GREEN}The update will be installed after your next restart.",
                fr,
                adjustedWith / 2F,
                40F,
                true,
                -1
            )
        }

        val widthRemaining = adjustedWith - button.width - 10

        GlStateManager.scale(2F, 2F, 1F)
        val currentVersion = Nautilus.VERSION
        val sameVersion = currentVersion.equals(nextVersion, true)
        TextRenderUtils.drawStringCenteredScaledMaxWidth(
            "${if (UpdateManager.updateState == UpdateState.NONE) GREEN else RED}$currentVersion" +
                if (nextVersion != null && !sameVersion) "➜ ${GREEN}${nextVersion}" else "",
            fr,
            widthRemaining / 4F,
            10F,
            true,
            widthRemaining / 2,
            -1
        )

        GlStateManager.popMatrix()
    }

    private fun getButtonPosition(width: Int) = width - button.width
    override fun getHeight() = 55

    // TODO: try to use the MouseEvent
    override fun mouseInput(x: Int, y: Int, width: Int, mouseX: Int, mouseY: Int, mouseEvent: MouseEvent): Boolean {
        val width = width - 20
        if (Mouse.getEventButtonState() && (mouseX - getButtonPosition(width) - x) in (0..button.width) && (mouseY - 10 - y) in (0..button.height)) {
            when (UpdateManager.updateState) {
                UpdateState.AVAILABLE, UpdateState.NONE -> UpdateManager.checkUpdate()
                else -> {}
            }
            return true
        }
        return false
    }

    override fun keyboardInput(event: KeyboardEvent) = false

    override fun fulfillsSearch(word: String) = super.fulfillsSearch(word) || word in "download" || word in "update"
}
