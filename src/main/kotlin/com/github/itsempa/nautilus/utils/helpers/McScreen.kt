package com.github.itsempa.nautilus.utils.helpers

import com.github.itsempa.nautilus.utils.NautilusNullableUtils.instanceOfAny
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.orFalse
import com.github.itsempa.nautilus.utils.NautilusNullableUtils.safeCast
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import kotlin.reflect.KClass

@Suppress("unused")
object McScreen {

    val self: GuiScreen? get() = McClient.self.currentScreen

    fun close() = McPlayer.self.closeScreen()

    val chest: GuiChest? get() = self as? GuiChest

    val containerChest: ContainerChest? get() = chest?.inventorySlots?.safeCast<ContainerChest>()

    val inventoryName: String get() = containerChest?.lowerChestInventory?.displayName?.unformattedText.orEmpty()

    fun display(screen: GuiScreen?) = McClient.self.displayGuiScreen(screen)

    inline fun <reified T> instanceOf(): Boolean = self is T

    fun instanceOfAny(vararg classes: KClass<out GuiScreen>): Boolean = self?.instanceOfAny(*classes).orFalse()
}
