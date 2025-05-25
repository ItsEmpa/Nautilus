package com.github.itsempa.nautilus.utils.helpers

import at.hannibal2.skyhanni.utils.DelayedRun
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.GameSettings

@Suppress("unused")
object McClient {

    val self: Minecraft get() = Minecraft.getMinecraft()
    val worldNull: WorldClient? get() = self.theWorld
    val world: WorldClient get() = worldNull!!

    val settings get(): GameSettings = self.gameSettings
    val textureManager: TextureManager get() = self.textureManager

    /** Runs [action] in the main thread. */
    fun run(action: () -> Unit) {
        if (self.isCallingFromMinecraftThread) {
            action()
        } else {
            self.addScheduledTask(action)
        }
    }

    fun runOnWorld(action: () -> Unit) {
        if (McPlayer.exists) action()
        else DelayedRun.runNextTick(action)
    }

}
