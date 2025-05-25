package com.github.itsempa.nautilus

import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.config.Features
import com.github.itsempa.nautilus.data.core.NautilusRepoManager
import com.github.itsempa.nautilus.data.core.SkyHanniVersionCheck
import com.github.itsempa.nautilus.events.NautilusPreInitFinishedEvent
import com.github.itsempa.nautilus.features.misc.update.SemVersion
import com.github.itsempa.nautilus.mixins.transformers.skyhanni.AccessorSkyHanniEvents
import com.github.itsempa.nautilus.modules.NautilusDevModules
import com.github.itsempa.nautilus.modules.NautilusModules
import com.github.itsempa.nautilus.utils.tryError
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.owdding.ktmodules.Module
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
    modLanguageAdapter = "com.github.itsempa.nautilus.utils.KotlinLanguageAdapter",
)
object Nautilus {

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        SkyHanniVersionCheck.checkSkyHanniLoaded()
        tryError("Something went wrong while initializing the config!") {
            ConfigManager // Load ConfigManager class to initialize config
        }

        tryError("Something went wrong while initializing modules!") {
            NautilusModules.init(::loadModule)
        }
        if (PlatformUtils.isDevEnvironment) {
            tryError("Something went wrong while initializing dev modules!") {
                NautilusDevModules.init(::loadModule)
            }
        }
        // TODO: figure out why this errors on startup
        NautilusRepoManager.initRepo()

        NautilusPreInitFinishedEvent.post()
    }

    private fun loadModule(obj: Any) {
        if (obj in modules) return
        tryError(
            "Something went wrong while initializing events!",
            "module" to obj.javaClass.simpleName,
            ignoreErrorCache = true,
        ) {
            for (method in obj.javaClass.declaredMethods) {
                @Suppress("CAST_NEVER_SUCCEEDS")
                (SkyHanniEvents as AccessorSkyHanniEvents).`nautilus$registerMethod`(method, obj)
            }
            MinecraftForge.EVENT_BUS.register(obj)
            modules.add(obj)
        }
    }

    const val MOD_ID = "@MOD_ID@"
    const val VERSION = "@MOD_VER@"
    const val MOD_NAME = "@MOD_NAME@"
    const val COMPILED_SH_VERSION = "@SKYHANNI_VER@"

    const val CLASS_PATH = "com.github.itsempa.nautilus"
    const val DISCORD_INVITE = "https://discord.gg/KM3dKjbWqg"
    const val GITHUB = "https://github.com/ItsEmpa/Nautilus"

    val SEM_VER = SemVersion.fromString(VERSION)

    private val modules: MutableList<Any> = mutableListOf()

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
