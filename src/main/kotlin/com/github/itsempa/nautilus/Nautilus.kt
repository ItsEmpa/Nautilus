package com.github.itsempa.nautilus


import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.data.NautilusRepoManager
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.github.itsempa.nautilus.mixins.transformers.skyhanni.AccessorSkyHanniEvents
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.modules.NautilusModules
import com.github.itsempa.nautilus.utils.NautilusUtils
import com.github.itsempa.nautilus.utils.tryError
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        NautilusRepoManager.initRepo()
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
                NautilusUtils.logErrorWithData(
                    it,
                    "Something went wrong while initializing events.",
                    "module" to obj.javaClass.simpleName,
                    ignoreErrorCache = true,
                )
            }
        }
    }

    const val MOD_ID = "@MOD_ID@"
    const val VERSION = "@MOD_VER@"
    const val MOD_NAME = "@MOD_NAME@"

    val SEM_VER = SemVersion.fromString(VERSION)

    @JvmField
    val modules: MutableList<Any> = mutableListOf()

    @JvmStatic
    val feature: Features get() = ConfigManager.managedConfig.instance

    @JvmField
    val logger: Logger = LogManager.getLogger(MOD_NAME)

    fun consoleLog(message: String) = logger.info(message)

    private val globalJob: Job = Job(null)
    private val coroutineScope = CoroutineScope(CoroutineName(MOD_NAME) + SupervisorJob(globalJob))

    fun launchCoroutine(function: suspend () -> Unit) {
        coroutineScope.launch {
            tryError({ it.message ?: "Asynchronous exception caught" }) {
                function()
            }
        }
    }

    fun launchIOCoroutine(block: suspend CoroutineScope.() -> Unit) {
        launchCoroutine {
            withContext(Dispatchers.IO) {
                block()
            }
        }
    }

}
