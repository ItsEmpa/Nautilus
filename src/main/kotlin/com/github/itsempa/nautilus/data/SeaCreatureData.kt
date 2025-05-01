package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.data.EntityViewApi.canActuallyBeSeen
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactBoundingBoxExtraEntities
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactLocation
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getLorenzVec
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB

data class SeaCreatureData(
    val isOwn: Boolean,
    val seaCreature: SeaCreature,
    var entityId: Int,
    val spawnTime: SimpleTimeMark,
    var mob: Mob?,
) {

    /** This tracks the last position of the sea creature that the user was able to see. */
    var pos: LorenzVec?
        private set

    /** This tracks the last bounding box of the sea creature that the user was able to see. */
    var aabb: AxisAlignedBB?
        private set

    /** This tracks the real last position of the sea creature. Don't display this to the user */
    internal var actualLastPos: LorenzVec
        private set

    init {
        if (canBeSeen()) {
            aabb = mob?.boundingBox
            pos = mob?.getLorenzVec()
        } else {
            aabb = null
            pos = null
        }
        updateCanBeSeen()
        actualLastPos = mob?.getLorenzVec() ?: LorenzVec(0.0, 0.0, 0.0)
    }

    inline val name: String get() = seaCreature.name

    inline val isRare: Boolean get() = seaCreature.rare

    inline val rarity: LorenzRarity get() = seaCreature.rarity

    inline val health: Int? get() = mob?.health?.toInt()

    inline val despawnTime: SimpleTimeMark get() = spawnTime + SeaCreatureDetectionApi.DESPAWN_TIME

    fun isLoaded(): Boolean = entity != null

    val entity: EntityLivingBase? get() = mob?.baseEntity ?: EntityUtils.getEntityByID(entityId) as? EntityLivingBase

    private var hasDied: Boolean = false

    fun despawn() {
        SeaCreatureEvent.DeSpawn(this).post()
    }

    fun sendDeath(seenDeath: Boolean = true) {
        if (hasDied) return
        hasDied = true
        SeaCreatureEvent.Death(this, seenDeath).post()
    }

    private var hasRemoved: Boolean = false

    fun forceRemove() {
        if (hasRemoved) return
        hasRemoved = true
        SeaCreatureEvent.Remove(this).post()
    }

    private var canBeSeenCache = false

    fun canBeSeen(): Boolean = canBeSeenCache

    private fun updateCanBeSeen(): Boolean {
        val mob = mob ?: return false
        mob.baseEntity.canBeSeen()
        canBeSeenCache = mob.baseEntity.canActuallyBeSeen() || mob.extraEntities.any { it.canActuallyBeSeen() }
        return canBeSeenCache
    }

    @Suppress("HandleEventInspection")
    fun update(renderWorld: SkyHanniRenderWorldEvent) {
        val mob = mob ?: return
        actualLastPos = mob.getLorenzVec()
        if (!updateCanBeSeen()) return
        aabb = renderWorld.exactBoundingBoxExtraEntities(mob)
        pos = renderWorld.exactLocation(mob)
    }
}
