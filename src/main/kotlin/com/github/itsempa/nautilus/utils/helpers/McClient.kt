package com.github.itsempa.nautilus.utils.helpers

import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.Nautilus
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.fml.common.FMLCommonHandler

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

    fun shutdown(reason: String? = null) {
        val reasonLine = reason?.let { " Reason: ($it)" }.orEmpty()
        System.err.println("${Nautilus.MOD_NAME}-${Nautilus.VERSION} forced the game to shutdown.$reasonLine")
        FMLCommonHandler.instance().handleExit(-1)
    }

}
