package com.github.itsempa.nautilus.features.gui

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.utils.ClipboardUtils
import at.hannibal2.skyhanni.utils.EnumUtils.toFormattedName
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.collection.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.container.HorizontalContainerRenderable.Companion.horizontal
import at.hannibal2.skyhanni.utils.renderables.container.VerticalContainerRenderable.Companion.vertical
import at.hannibal2.skyhanni.utils.renderables.primitives.ItemStackRenderable.Companion.item
import at.hannibal2.skyhanni.utils.renderables.primitives.placeholder
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.FeeshApi
import com.github.itsempa.nautilus.data.categories.FishingCategory
import com.github.itsempa.nautilus.data.fishingevents.SpookyFestivalEvent
import com.github.itsempa.nautilus.events.FishingEventUpdate
import com.github.itsempa.nautilus.events.NautilusDebugEvent
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.fullEnumMapOf
import com.github.itsempa.nautilus.utils.safe.SafeUtils
import me.owdding.ktmodules.Module
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

@Module
object SpookyCounter {

    private val config get() = Nautilus.feature.gui

    // TODO: repo
    private const val NIGHTMARE_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYwNTQwMjg1NDk2NCwKICAicHJvZmlsZUlkIiA6ICJmMzA1ZjA5NDI0NTg0ZjU4YmEyYjY0ZjAyZDcyNDYyYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJqcm9ja2EzMyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81NzgyMTFlMWI0ZDk5ZDFjN2JmZGE0ODM4ZTQ4ZmM4ODRjM2VhZTM3NmY1OGQ5MzJiYzJmNzhiMGE5MTlmOGU3IgogICAgfQogIH0KfQ=="
    private const val NIGHTMARE_UUID = "76e06cd3-376f-3cfa-9431-1c2b67de42e9"
    private const val WEREWOLF_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYwMzY2ODk3MjkwMywKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVyb2Ryb2dvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NlNDYwNmM2ZDk3M2E5OTlhZWMxNjg3YzdlMDc1ZjdkMzdkYjgxODVlODhiODQ0NTA3ZjE2YjNlMmIzZWI2OTAiCiAgICB9CiAgfQp9"
    private const val WEREWOLF_UUID = "c8e41e94-9c30-38af-80c6-05b2ed6d5c53"
    private const val PHANTOM_FISHER_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYwNDAyNDY0MjMyNywKICAicHJvZmlsZUlkIiA6ICI0ZmFkNjk2NTIxNGI0NGQ4YjAxYzlhZTVjZDQ0MDVjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaXBfYXNpYW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM5YmMwMWYyOTlmOThkNTY1YTI3YmExMGExMjkzOTE1YWU4YmVlZWZiOGE2Nzg0NWUyMzMxZGJlNmZkNmZkNiIKICAgIH0KICB9Cn0="
    private const val PHANTOM_FISHER_UUID = "936428d9-2f4a-3ce7-8a93-a69d81f1a28a"
    private const val GRIM_REAPER_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYwNDA1NDA5MDUwMCwKICAicHJvZmlsZUlkIiA6ICI5NGMzZGM3YTdiMmQ0NzQ1YmVlYjQzZDc2ZjRjNDVkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJEb25fVml0b0Nvcmxlb25lIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY4YTYxOTRhNWIyMTdiOWY1YTNkZmVjY2U1ZjNlZmU2OTY3NDA1MDM5YjgyZmEwYzRlODk1OTE3NWYzMmU3NWEiCiAgICB9CiAgfQp9"
    private const val GRIM_REAPER_UUID = "69e73584-c3f5-38b6-9622-3d0b4a95fb92"

    // TODO: repo maybe?
    private enum class SpookyMob(val rarity: LorenzRarity, itemSupplier: () -> ItemStack) {
        SCARECROW(LorenzRarity.COMMON, { ItemStack(Blocks.pumpkin) }),
        NIGHTMARE(LorenzRarity.RARE, NIGHTMARE_SKULL, NIGHTMARE_UUID),
        WEREWOLF(LorenzRarity.EPIC, WEREWOLF_SKULL, WEREWOLF_UUID),
        PHANTOM_FISHER(LorenzRarity.LEGENDARY, PHANTOM_FISHER_SKULL, PHANTOM_FISHER_UUID),
        GRIM_REAPER(LorenzRarity.LEGENDARY, GRIM_REAPER_SKULL, GRIM_REAPER_UUID),
        ;

        constructor(rarity: LorenzRarity, texture: String, uuid: String) :
            this(rarity, { ItemUtils.createSkull("Head", uuid, texture) })

        val item: ItemStack by lazy(itemSupplier)
        private val chatName: String = toFormattedName()
        override fun toString(): String = chatName

        companion object {
            fun fromChatName(name: String): SpookyMob? = entries.find { it.chatName == name }
        }
    }

    private val catchAmount = fullEnumMapOf<SpookyMob, Int>(0)
    private var isActive = false
    private var startTime = SimpleTimeMark.farPast()
    private var renderable: Renderable? = null

    @HandleEvent
    fun onCatch(event: SeaCreatureFishEvent) {
        if (!isActive) return
        val spookyMob = SpookyMob.fromChatName(event.seaCreature.name) ?: return
        catchAmount.addOrPut(spookyMob, if (event.doubleHook) 2 else 1)
        if (startTime.isFarPast()) startTime = SimpleTimeMark.now()
        updateDisplay()
    }

    @HandleEvent
    fun onSpookyStart(event: FishingEventUpdate.Start<SpookyFestivalEvent>) {
        resetCatchCount()
        isActive = true
        updateDisplay()
    }

    @HandleEvent
    fun onSpookyEnd(event: FishingEventUpdate.End<SpookyFestivalEvent>) {
        sendMessage()
        resetCatchCount()
        isActive = false
        startTime = SimpleTimeMark.farPast()
        renderable = null
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isActive || !FeeshApi.isFishing || !config.spookyCounter) return
        if (!FishingCategory.Water.Events.Spooky.isActive) return
        config.spookyCounterPos.renderRenderable(renderable, "Spooky Counter")
    }

    private fun sendMessage() {
        var total = 0
        val list = catchAmount.mapNotNull { (mob, amount) ->
            if (amount == 0) return@mapNotNull null
            total += amount
            "§7 - ${mob.rarity.chatColorCode + mob.toString()}§7: §b$amount"
        }
        if (list.isEmpty()) return
        val totalString = total.addSeparators()
        val message = list.joinToString("\n")
        val compactMobs = catchAmount.entries.joinToString { (mob, amount) -> "$mob: $amount" }
        val compact = "You caught $totalString sea creatures ($compactMobs) during this Spooky Festival!"
        NautilusChat.clickableChat(
            "You caught §b$totalString §3spooky sea creatures during this Spooky Festival!\n$message",
            hover = "§eClick to copy compact message to clipboard!",
        ) {
            ClipboardUtils.copyToClipboard(compact)
            NautilusChat.chat("Copied spooky festival message to clipboard!")
        }
    }

    private fun updateDisplay() {
        renderable = Renderable.vertical(
            listOf(
                SafeUtils.stringRenderable("§5§lSpooky Festival Counter"),
                Renderable.horizontal(
                    buildList {
                        for ((mob, amount) in catchAmount) {
                            add(Renderable.item(mob.item, 1.0))
                            add(SafeUtils.stringRenderable("§e$amount"))
                            add(Renderable.placeholder(5, 0))
                        }
                    },
                ),
            ),
            spacing = 2,
        )
    }

    @HandleEvent
    fun onDebug(event: NautilusDebugEvent) {
        event.title("SpookyCounter")
        event.addIrrelevant(
            "isActive" to isActive,
            "startTime" to startTime,
            "catchAmount" to catchAmount.entries,
        )
    }

    private fun resetCatchCount() = catchAmount.replaceAll { _, _ -> 0 }

}
