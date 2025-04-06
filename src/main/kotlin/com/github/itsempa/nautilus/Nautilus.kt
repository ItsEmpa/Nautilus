package com.github.itsempa.nautilus

import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.mixins.transformers.skyhanni.AccessorSkyHanniEvents
import com.github.itsempa.nautilus.modules.NautilusModules
import com.github.itsempa.nautilus.modules.Module
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Module
@Mod(
    modid = Nautilus.MOD_ID,
    name = Nautilus.MOD_NAME,
    clientSideOnly = true,
    useMetadata = true,
    version = Nautilus.VERSION,
    dependencies = "before:skyhanni",
    modLanguageAdapter = "at.hannibal2.skyhanni.utils.system.KotlinLanguageAdapter",
)
object Nautilus {

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        ConfigManager // Load ConfigManager class to initialize config
        NautilusModules.modules.loadModules()

        NautilusCommandRegistrationEvent.post()
    }


    private fun List<Any>.loadModules() = forEach(::loadModule)

    private fun loadModule(obj: Any) {
        if (obj in modules) return
        runCatching {
            for (method in obj.javaClass.declaredMethods) {
                @Suppress("CAST_NEVER_SUCCEEDS")
                (SkyHanniEvents as AccessorSkyHanniEvents).`nautilus$registerMethod`(method, obj)
            }
            MinecraftForge.EVENT_BUS.register(obj)
            modules.add(obj)
        }.onFailure {
            DelayedRun.runNextTick {
                ErrorManager.logErrorWithData(
                    it,
                    "§c${MOD_NAME} ERROR! Something went wrong while initializing events",
                    ignoreErrorCache = true,
                    betaOnly = false,
                )
            }
        }
    }

    const val MOD_ID = "@MOD_ID@"
    const val VERSION = "@MOD_VER@"
    const val MOD_NAME = "@MOD_NAME@"

    @JvmField
    val logger: Logger = LogManager.getLogger(MOD_NAME)

    fun consoleLog(message: String) = logger.info(message)

    @JvmField
    val modules: MutableList<Any> = ArrayList()

    @JvmStatic
    val feature: Features get() = ConfigManager.managedConfig.instance

}
