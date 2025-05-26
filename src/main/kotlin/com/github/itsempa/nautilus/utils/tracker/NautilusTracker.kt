package com.github.itsempa.nautilus.utils.tracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.RenderData
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.github.itsempa.nautilus.config.storage.ProfileStorage
import com.github.itsempa.nautilus.data.core.NautilusErrorManager
import com.github.itsempa.nautilus.data.core.NautilusStorage
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.helpers.McScreen
import com.google.gson.annotations.Expose
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory

@Suppress("unused")
open class NautilusTracker<Data : NautilusTrackerData>(
    val name: String,
    val internalName: String,
    private val createNewSession: () -> Data,
    private val getStorage: (ProfileStorage) -> Data,
    private val customSessionStorage: (ProfileStorage) -> Data, // TODO: change this to be a map of tracker display mode to data?
    private val extraDisplayModes: Map<TrackerDisplayMode, (ProfileStorage) -> Data> = emptyMap(),
    private val drawDisplay: (Data) -> List<Renderable>,
) {
    var inventoryOpen = false
        private set
    var displayMode: TrackerDisplayMode = TrackerDisplayMode.TOTAL
        private set
    private val currentSessions = mutableMapOf<ProfileStorage, Data>()
    private var display = emptyList<Renderable>()
    private var sessionResetTime = SimpleTimeMark.farPast()
    private var dirty = false


    companion object {
        // TODO: replace with own config
        private val shConfig get() = SkyHanniMod.feature.misc.tracker
        fun getPricePer(name: NeuInternalName) = name.getPrice(shConfig.priceSource)
    }

    fun resetCommand() = NautilusChat.clickableChat(
        "Are you sure you want to reset your total $name? Click here to confirm.",
        "§eClick to confirm.",
        oneTimeClick = true,
    ) {
        reset(TrackerDisplayMode.TOTAL, "Reset total $name!")
    }

    fun modify(modifyFunction: Data.(TrackerDisplayMode) -> Unit) {
        getSharedTracker().modify(modifyFunction)
        update()
    }

    fun modify(mode: TrackerDisplayMode, modifyFunction: (Data) -> Unit) {
        getSharedTracker().modify(mode, modifyFunction)
        update()
    }


    private fun tryModify(mode: TrackerDisplayMode, modifyFunction: (Data) -> Unit) {
        getSharedTracker().tryModify(mode, modifyFunction)
        update()
    }

    fun modifyEachMode(modifyFunction: (Data) -> Unit) {
        TrackerDisplayMode.entries.forEach {
            tryModify(it, modifyFunction)
        }
    }

    fun renderDisplay(position: Position) {
        if (shConfig.hideInEstimatedItemValue && EstimatedItemValue.isCurrentlyShowing()) return

        var currentlyOpen = McScreen.instanceOfAny(GuiInventory::class, GuiChest::class)
        /*if (!currentlyOpen && shConfig.hideItemTrackersOutsideInventory && this is SkyHanniItemTracker) {
            return
        }*/
        if (RenderData.outsideInventory) {
            currentlyOpen = false
        }
        if (inventoryOpen != currentlyOpen) {
            inventoryOpen = currentlyOpen
            update()
        }
        if (dirty || TrackerManager.dirty) {
            val shared = getSharedTracker()
            val data = shared.get(displayMode)
            val renderables = drawDisplay(data)
            // TODO: add some things (title, etc)
            display = renderables
            dirty = false
        }

        position.renderRenderables(display, posLabel = name)
    }

    fun update() {
        dirty = true
    }

    private fun getSharedTracker(): NautilusTracker<Data>.SharedTracker {
        val storage = NautilusStorage.profile
        return SharedTracker(
            buildMap {
                put(TrackerDisplayMode.TOTAL, storage.getTotal())
                put(TrackerDisplayMode.SESSION, storage.getCurrentSession())
                if (storage.info.isCustomSessionActive) {
                    put(TrackerDisplayMode.CUSTOM_SESSION, storage.getCustomSession())
                }
                extraDisplayModes.mapValuesTo(this) { it.value(storage) }
            }
        )
    }

    private fun reset(displayMode: TrackerDisplayMode, message: String) {
        getSharedTracker().get(displayMode).reset()
        ChatUtils.chat(message)
        update()
    }

    fun firstUpdate() {
        if (display.isEmpty()) update()
    }

    private fun ProfileStorage.getCustomSession(): Data = customSessionStorage(this)

    private fun ProfileStorage.getCurrentSession() = currentSessions.getOrPut(this, createNewSession)

    private fun ProfileStorage.getTotal(): Data = getStorage(this)

    private val ProfileStorage.info get() = trackerInfo.getOrPut(internalName, ::TrackerInfo)

    inner class SharedTracker(
        private val entries: Map<TrackerDisplayMode, Data>,
    ) {

        fun modify(mode: TrackerDisplayMode, modifyFunction: Data.() -> Unit) {
            get(mode).let(modifyFunction)
        }

        fun tryModify(mode: TrackerDisplayMode, modifyFunction: Data.() -> Unit) {
            entries[mode]?.let(modifyFunction)
        }

        fun modify(modifyFunction: Data.(TrackerDisplayMode) -> Unit) {
            entries.entries.forEach { (mode, data) ->
                modifyFunction(data, mode)
            }
        }

        fun get(displayMode: TrackerDisplayMode) = entries[displayMode] ?: NautilusErrorManager.nautilusError(
            "Unregistered display mode accessed on tracker",
            "tracker" to name,
            "displayMode" to displayMode,
            "availableModes" to entries.keys,
        )
    }

    data class TrackerInfo(
        @Expose var customSessionStart: SimpleTimeMark = SimpleTimeMark.farFuture(),
        @Expose var isCustomSessionActive: Boolean = false,
    )


}
