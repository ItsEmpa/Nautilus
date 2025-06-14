package com.github.itsempa.nautilus.features.misc.update

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.utils.ApiUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.events.BrigadierRegisterEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.helpers.McPlayer
import me.owdding.ktmodules.Module
import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateTarget
import moe.nea.libautoupdate.UpdateUtils
import java.util.concurrent.CompletableFuture
import javax.net.ssl.HttpsURLConnection

@Module
object UpdateManager {

    private val config get() = Nautilus.feature.about

    private var activePromise: CompletableFuture<*>? = null
        set(value) {
            field?.cancel(true)
            field = value
        }

    var updateState: UpdateState = UpdateState.NONE
        private set

    fun getNextVersion(): String? = potentialUpdate?.update?.versionName

    private var potentialUpdate: PotentialUpdate? = null

    private val context = UpdateContext(
        GithubReleaseUpdateSource("ItsEmpa", "Nautilus"),
        UpdateTarget.deleteAndSaveInTheSameFolder(this::class.java),
        Nautilus.SEM_VER,
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
    fun onTick() {
        if (checked) return
        if (!config.notifyUpdates) return
        if (!McPlayer.exists) return
        checked = true
        checkUpdate()
    }

    fun checkUpdate(forceUpdate: Boolean = false) {
        NautilusChat.chat("Checking for updates...")
        activePromise = context.checkUpdate("pre")
            .thenAcceptAsync(
                {
                    potentialUpdate = it
                    if (!it.isUpdateAvailable) return@thenAcceptAsync NautilusChat.chat("No updates found.")
                    updateState = UpdateState.AVAILABLE
                    val text = "Found a new update! (${Nautilus.VERSION} -> ${it.update.versionName})"
                    if (config.autoUpdates || forceUpdate) {
                        NautilusChat.chat("$text Starting to download...")
                        queueUpdate()
                    } else {
                        NautilusChat.clickableChat("$text Click here to download.", onClick = ::queueUpdate)
                    }
                },
                DelayedRun.onThread,
            )
    }

    @HandleEvent
    fun onCommandRegistration(event: BrigadierRegisterEvent) {
        event.register("ntupdate") {
            this.aliases = listOf("nautilusupdate")
            this.description = "Checks for updates"
            this.category = CommandCategory.MAIN
            callback { checkUpdate(true) }
        }
    }

    fun queueUpdate() {
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync(
            {
                updateState = UpdateState.DOWNLOADED
                potentialUpdate!!.executePreparedUpdate()
                NautilusChat.chat("Update complete! Restart your game to apply changes.")
            },
            DelayedRun.onThread,
        )
    }

}
