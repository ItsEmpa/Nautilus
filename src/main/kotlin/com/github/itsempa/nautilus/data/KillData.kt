package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SizeLimitedCache
import com.github.itsempa.nautilus.events.KillEvent
import com.github.itsempa.nautilus.utils.NautilusItemUtils.getBookOfStats
import com.github.itsempa.nautilus.utils.NautilusItemUtils.uuid
import me.owdding.ktmodules.Module
import java.util.UUID

@Module
object KillData {

    @Suppress("UnstableApiUsage")
    private val itemCache = SizeLimitedCache<UUID, KillData>(10)

    private data class KillData(
        var amount: Int = 0,
        var lastKill: SimpleTimeMark = SimpleTimeMark.farPast(),
    )

    var lastKill: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    @HandleEvent
    fun onInventoryUpdate(event: OwnInventoryItemUpdateEvent) {
        val stack = event.itemStack
        val uuid = stack.uuid ?: return
        val kills = stack.getBookOfStats() ?: return
        val data = itemCache.getOrNull(uuid)
        if (data == null) {
            itemCache[uuid] = KillData(kills)
            return
        }
        val diff = kills - data.amount
        if (diff <= 0) return
        data.amount = kills
        lastKill = SimpleTimeMark.now()
        KillEvent(diff).post()
    }

}
