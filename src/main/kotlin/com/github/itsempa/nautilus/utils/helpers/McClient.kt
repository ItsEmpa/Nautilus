package com.github.itsempa.nautilus.utils.helpers

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.GameSettings

@Suppress("unused")
object McClient {

    val self: Minecraft get() = Minecraft.getMinecraft()
    val world: WorldClient? get() = self.theWorld

    val settings get(): GameSettings = self.gameSettings
    val textureManager: TextureManager get() = self.textureManager

}
