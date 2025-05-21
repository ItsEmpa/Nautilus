package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.system.ModVersion
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.utils.tryOrNull
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.FMLCommonHandler

object SkyHanniVersionCheck {

    private const val SKYHANNI_MOD_CLASS = "at.hannibal2.skyhanni.SkyHanniMod"

    val isSkyHanniLoaded: Boolean = runCatching { Class.forName(SKYHANNI_MOD_CLASS) }.isSuccess

    @JvmStatic
    fun checkSkyHanniLoaded() {
        if (!isSkyHanniLoaded) {
            System.err.print("SkyHanni is missing!")
            closeMinecraft()
            return
        }
        val compiledVersion = tryOrNull {
            ModVersion.fromString(Nautilus.COMPILED_SH_VERSION)
        }
        val installedVersion = tryOrNull(SkyHanniMod::modVersion)
        if (installedVersion == null || compiledVersion == null || installedVersion < compiledVersion) {
            DelayedRun.runNextTick {
                val text = "SkyHanni version is outdated ($installedVersion)! Please update to ${Nautilus.COMPILED_SH_VERSION} or higher."
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("§c§l[Nautilus] $text"))
            }
            return
        }
    }

    private fun closeMinecraft() {
        FMLCommonHandler.instance().handleExit(-1)
        FMLCommonHandler.instance().expectServerStopped()
    }

}
