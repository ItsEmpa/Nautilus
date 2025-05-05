package com.github.itsempa.nautilus.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat.isLocalPlayer
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments
import com.github.itsempa.nautilus.commands.brigadier.BrigadierArguments.getInteger
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase

@Module
object Spinny {

    private val config get() = Nautilus.feature.misc.spin

    @HandleEvent
    fun onCommandRegister(event: BrigadierRegisterEvent) {
        event.register("nautilusspin") {
            this.aliases = listOf("ntspin", "spin")
            this.description = "Spin the player!"
            this.category = CommandCategory.USERS_ACTIVE

            thenCallback("toggle") {
                toggle()
            }
            thenCallback("speed", BrigadierArguments.integer(-500, 500)) {
                val number = getInteger("speed")
                NautilusChat.chat("Set spin speed to $number!")
                if (!config.enabled) config.enabled = true
                config.spinSpeed = number
            }
            callback {
                toggle()
            }
        }
    }

    private fun toggle() {
        val newValue = !config.enabled
        config.enabled = newValue
        val text = if (newValue) "§aenabled" else "§cdisabled"
        NautilusChat.chat("Set spin to $text!")
    }

    @JvmStatic
    fun rotatePlayer(player: EntityLivingBase, partialTicks: Float) {
        if (!config.enabled) return
        if (!player.isLocalPlayer) return
        val spinsPerMinute = config.spinSpeed
        val spinsPerSecond = spinsPerMinute / 60.0
        val degreesPerSecond = spinsPerSecond * 360.0
        val degreesPerTick = degreesPerSecond / 20.0

        val ticksExisted = player.ticksExisted + partialTicks

        val rotation = (ticksExisted * degreesPerTick).toFloat() % 360 + 180
        GlStateManager.rotate(rotation, 0f, 1f, 0f)
    }

}
