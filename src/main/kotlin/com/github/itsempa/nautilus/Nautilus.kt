package com.github.itsempa.nautilus

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.deps.moulconfig.managed.GsonMapper
import at.hannibal2.skyhanni.deps.moulconfig.managed.ManagedConfig
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.events.ExampleCommandRegistrationEvent
import com.github.itsempa.nautilus.mixins.transformers.skyhanni.AccessorSkyHanniEvents
import com.github.itsempa.nautilus.modules.Nautilusules
import com.github.itsempa.nautilus.modules.Module
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

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
        Nautilusules.modules.loadModules()

        ExampleCommandRegistrationEvent.post()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(60)) {
            managedConfig.saveToFile()
        }
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

    const val HIDE_MOD_ID: Boolean = false

    @JvmField
    val logger: Logger = LogManager.getLogger(MOD_NAME)

    @JvmField
    val modules: MutableList<Any> = ArrayList()

    @JvmStatic
    val feature: Features get() = managedConfig.instance

    @JvmStatic
    val managedConfig by lazy {
        ManagedConfig.create(File("config/$MOD_ID/config.json"), Features::class.java) {
            throwOnFailure()
            val mapper = this.mapper as GsonMapper<Features>
            mapper.doNotRequireExposed = true
            mapper.gsonBuilder.setPrettyPrinting()
        }
    }

}
