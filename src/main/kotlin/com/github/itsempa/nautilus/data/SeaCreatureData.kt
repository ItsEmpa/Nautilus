package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactBoundingBoxExtraEntities
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactLocation
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.isInPastOrAlmost
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import kotlin.time.Duration.Companion.seconds

data class SeaCreatureData(
    val isOwn: Boolean,
    val seaCreature: SeaCreature,
    var entityId: Int,
    val spawnTime: SimpleTimeMark,
    var mob: Mob?,
) {

    var pos: LorenzVec?
        private set

    var aabb: AxisAlignedBB?
        private set

    init {
        if (canBeSeen()) {
            aabb = mob?.boundingBox
            pos = mob?.getLorenzVec()
        } else {
            aabb = null
            pos = null
        }

    }

    inline val name: String get() = seaCreature.name

    inline val isRare: Boolean get() = seaCreature.rare

    inline val rarity: LorenzRarity get() = seaCreature.rarity

    inline val despawnTime: SimpleTimeMark get() = spawnTime + SeaCreatureApi.DESPAWN_TIME

    fun isLoaded(): Boolean = mob != null

    val entity: EntityLivingBase? get() = mob?.baseEntity ?: EntityUtils.getEntityByID(entityId) as? EntityLivingBase

    private var hasDespawnedTimeLimit: Boolean = false

    /** Returns true if it despawned because of time limit */
    fun despawn(forceTime: Boolean = false): Boolean {
        val isTimeLimit = forceTime || (mob?.isInRender() == true && despawnTime.isInPastOrAlmost(10.seconds))
        if (isTimeLimit && hasDespawnedTimeLimit) return false
        hasDespawnedTimeLimit = isTimeLimit
        SeaCreatureEvent.DeSpawn(this, isTimeLimit).post()
        return isTimeLimit
    }

    fun canBeSeen(): Boolean {
        val mob = mob ?: return false
        val pos = mob.baseEntity.getLorenzVec()
        val newPos = if (name == "Fire Eel") pos.up() else pos
        return newPos.canBeSeen() // TODO: create canBeSeen function that takes into account F5
    }

    @Suppress("HandleEventInspection")
    fun update(renderWorld: SkyHanniRenderWorldEvent) {
        val mob = mob ?: return
        if (!canBeSeen()) return
        aabb = renderWorld.exactBoundingBoxExtraEntities(mob)
        pos = renderWorld.exactLocation(mob)
    }
}
