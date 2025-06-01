package com.github.itsempa.nautilus.config.core

import at.hannibal2.skyhanni.deps.moulconfig.gui.GuiScreenElementWrapper
import com.github.itsempa.nautilus.utils.helpers.McScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import org.lwjgl.input.Keyboard
import java.io.IOException

@Suppress("unused")
class ConfigGuiForgeInterop : IModGuiFactory {
    override fun initialize(minecraft: Minecraft) { /* Empty */ }

    override fun mainConfigGuiClass() = WrappedNautilusConfig::class.java

    override fun runtimeGuiCategories(): Set<IModGuiFactory.RuntimeOptionCategoryElement>? = null

    override fun getHandlerFor(element: IModGuiFactory.RuntimeOptionCategoryElement) = null

    class WrappedNautilusConfig(
        private val parent: GuiScreen
    ) : GuiScreenElementWrapper(ConfigManager.editor) {

        @Throws(IOException::class)
        override fun handleKeyboardInput() {
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                return McScreen.display(parent)
            } else super.handleKeyboardInput()
        }

    }

}
