package com.github.itsempa.nautilus.utils.helpers

import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.cast
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

@Suppress("unused")
object McScreen {

    val self: GuiScreen? get() = McClient.self.currentScreen

    fun close() = McPlayer.self.closeScreen()

    val chest: GuiChest? get() = self as? GuiChest

    val containerChest: ContainerChest? get() = chest?.inventorySlots?.cast<ContainerChest>()

    val inventoryName: String get() = containerChest?.getInventoryName().orEmpty()

    fun display(screen: GuiScreen?) = McClient.self.displayGuiScreen(screen)

    inline fun <reified T> instanceOf(): Boolean = self is T
}
