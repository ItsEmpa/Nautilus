package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.deps.libautoupdate.CurrentVersion
import at.hannibal2.skyhanni.deps.libautoupdate.GithubReleaseUpdateSource
import at.hannibal2.skyhanni.deps.libautoupdate.PotentialUpdate
import at.hannibal2.skyhanni.deps.libautoupdate.UpdateContext
import at.hannibal2.skyhanni.deps.libautoupdate.UpdateTarget
import at.hannibal2.skyhanni.deps.libautoupdate.UpdateUtils
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.utils.ApiUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChatUtils
import com.github.itsempa.nautilus.utils.helpers.McClient
import com.google.gson.JsonElement
import java.util.concurrent.CompletableFuture
import javax.net.ssl.HttpsURLConnection
import kotlin.math.pow

// TODO: dont use the libautoupdate bundled in SkyHanni, as it causes problems when both
//  SkyHanni and Nautilus want to update at the same time
@Module
object UpdateManager {

    private val config get() = Nautilus.feature.about

    private var _activePromise: CompletableFuture<*>? = null
    private var activePromise: CompletableFuture<*>?
        get() = _activePromise
        set(value) {
            _activePromise?.cancel(true)
            _activePromise = value
        }

    var updateState: UpdateState = UpdateState.NONE
        private set

    fun getNextVersion(): String? = potentialUpdate?.update?.versionName

    private var potentialUpdate: PotentialUpdate? = null

    private val context = UpdateContext(
        GithubReleaseUpdateSource("ItsEmpa", "Nautilus"),
        UpdateTarget.deleteAndSaveInTheSameFolder(this::class.java),
        object : CurrentVersion {
            val normalDelegate = CurrentVersion.ofTag(Nautilus.VERSION)
            override fun display() = normalDelegate.display()
            override fun isOlderThan(element: JsonElement?): Boolean {
                val version = element?.asString ?: return true

                fun getVersionNumber(input: String): Int { // very primitive semver parsing
                    val splits = input.split(".").asReversed()
                    var result = 0
                    for (i in splits.indices) {
                        result += splits[i].toInt() * 100.0.pow(i).toInt()
                    }
                    return result
                }

                val currentVersion = getVersionNumber(Nautilus.VERSION)
                val newVersion = getVersionNumber(version)
                return currentVersion < newVersion
            }
        },
        Nautilus.MOD_ID,
    )

    init {
        context.cleanup()
        UpdateUtils.patchConnection {
            if (it is HttpsURLConnection) {
                ApiUtils.patchHttpsRequest(it)
            }
        }
    }

    private var checked: Boolean = false

    // TODO: change this
    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (checked) return
        if (!config.notifyUpdates) return
        McClient.self.thePlayer ?: return
        checked = true
        checkUpdate()
    }

    fun checkUpdate(forceUpdate: Boolean = false) {
        NautilusChatUtils.chat("Checking for updates...")
        activePromise = context.checkUpdate("balls") // TODO: get correct updateStream
            .thenAcceptAsync({
                potentialUpdate = it
                if (!it.isUpdateAvailable) return@thenAcceptAsync NautilusChatUtils.chat("No updates found.")
                updateState = UpdateState.AVAILABLE
                val text = "Found a new update! (${Nautilus.VERSION} -> ${it.update.versionName})"
                if (config.autoUpdates || forceUpdate) {
                    NautilusChatUtils.chat("$text Starting to download...")
                    queueUpdate()
                } else {
                    NautilusChatUtils.clickableChat("$text Click here to download.", onClick = ::queueUpdate)
                }
            }, DelayedRun.onThread)
    }

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("nautilusupdate") {
            this.aliases = listOf("ntupdate")
            this.description = "Checks for updates"
            this.category = CommandCategory.MAIN
            callback { checkUpdate(true) }
        }
    }

    fun queueUpdate() {
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync({
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executePreparedUpdate()
            NautilusChatUtils.chat("Update complete! Restart your game to apply changes.")
        }, DelayedRun.onThread)
    }

}
