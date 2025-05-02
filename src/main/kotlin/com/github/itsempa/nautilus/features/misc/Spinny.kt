package com.github.itsempa.nautilus.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrNull
import at.hannibal2.skyhanni.utils.compat.MinecraftCompat.isLocalPlayer
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase

@Module
object Spinny {

    private val config get() = Nautilus.feature.misc.spin

    @HandleEvent
    fun onCommandRegister(event: NautilusCommandRegistrationEvent) {
        event.register("nautilusspin") {
            this.aliases = listOf("ntspin", "spin")
            this.description = "Spin the player!"
            this.category = CommandCategory.USERS_ACTIVE
            callback(::command)
        }
    }

    private fun command(args: Array<String>) {
        if (args.isEmpty()) {
            val newValue = !config.enabled
            config.enabled = newValue
            val text = if (newValue) "§aenabled" else "§cdisabled"
            NautilusChat.chat("Set spin to $text!")
            return
        } else {
            val number = args.first().formatIntOrNull()?.coerceIn(-500..500)
            if (number == null) {
                NautilusChat.userError("Invalid number!")
                return
            }
            NautilusChat.chat("Set spin speed to $number!")
            if (!config.enabled) config.enabled = true
            config.spinSpeed = number
        }
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
