package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.fishing.SeaCreature
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.events.SeaCreatureEvent
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactBoundingBoxExtraEntities
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.exactLocation
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getBoundingBoxExtraEntities
import com.github.itsempa.nautilus.utils.NautilusEntityUtils.getLorenzVec
import com.github.itsempa.nautilus.utils.NautilusUtils.getCenter
import com.github.itsempa.nautilus.utils.NautilusUtils.isInPastOrAlmost
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

data class SeaCreatureExtraData<T : Any>(
    val key: String,
    private val type: KClass<T>,
) {
    companion object {
        inline fun <reified T : Any> of(key: String): SeaCreatureExtraData<T> = SeaCreatureExtraData(key, T::class)
    }
}

data class SeaCreatureData(
    val isOwn: Boolean,
    val seaCreature: SeaCreature,
    var entityId: Int,
    val spawnTime: SimpleTimeMark,
    var mob: Mob?,
) {

    private val extraData = mutableMapOf<SeaCreatureExtraData<*>, Any>()

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

    inline val despawnTime: SimpleTimeMark get() = spawnTime + SeaCreatureDetectionApi.DESPAWN_TIME

    fun isLoaded(): Boolean = entity != null

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

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getExtraData(key: SeaCreatureExtraData<T>): T? = extraData[key] as T?

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getExtraDataOrPut(key: SeaCreatureExtraData<T>, defaultValue: () -> T): T {
        return extraData.getOrPut(key) { defaultValue() } as T
    }

    fun <T : Any> setExtraData(key: SeaCreatureExtraData<T>, value: T) = extraData.put(key, value)

    fun <T : Any> removeExtraData(key: SeaCreatureExtraData<T>) = extraData.remove(key)

    fun canBeSeen(): Boolean {
        val mob = mob ?: return false // TODO: create canBeSeen function that takes into account F5
        return mob.getBoundingBoxExtraEntities().getCenter().canBeSeen()
    }

    @Suppress("HandleEventInspection")
    fun update(renderWorld: SkyHanniRenderWorldEvent) {
        val mob = mob ?: return
        if (!canBeSeen()) return
        aabb = renderWorld.exactBoundingBoxExtraEntities(mob)
        pos = renderWorld.exactLocation(mob)
    }
}
