package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.github.itsempa.nautilus.mixins.transformers.AccessorRenderManager
import com.github.itsempa.nautilus.utils.helpers.McClient
import com.github.itsempa.nautilus.utils.removeIf
import com.github.itsempa.nautilus.utils.tryOrDefault
import me.owdding.ktmodules.Module
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GLContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Module
object EntityViewApi {

    private val renderManagerAccessor = McClient.self.renderManager as AccessorRenderManager
    private val queries = ConcurrentHashMap<UUID, OcclusionQuery>()
    private val QUERY_MODE = if (GLContext.getCapabilities().OpenGL33) GL33.GL_ANY_SAMPLES_PASSED else GL15.GL_SAMPLES_PASSED
    private val DELAY = 10.milliseconds

    @HandleEvent
    fun onTick() {
        val now = SimpleTimeMark.now()

        for (query in queries.values) {
            if (query.nextQuery != 0) {
                val available = GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT_AVAILABLE) != 0
                if (available) {
                    query.occluded = GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT) == 0
                    GL15.glDeleteQueries(query.nextQuery)
                    query.nextQuery = 0
                }
            }

            if (query.nextQuery == 0 && (now - query.executionTime) > DELAY) {
                query.executionTime = now
                query.refresh = true
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(5)) cleanupUnusedQueries()
    }

    /**
     * Contrary to [at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen], this takes into account the entire bounding box of an entity,
     * and also takes into account the fov and frustum.
     */
    fun Entity.canActuallyBeSeen(): Boolean {
        val query = queries.computeIfAbsent(uniqueID) { OcclusionQuery() }

        if (query.refresh) {
            query.nextQuery = tryOrDefault(0) { GL15.glGenQueries() }
            query.refresh = false

            GL15.glBeginQuery(QUERY_MODE, query.nextQuery)
            drawSelectionBoundingBox(
                entityBoundingBox
                    .expand(0.2, 0.2, 0.2)
                    .offset(
                        -renderManagerAccessor.getRenderPosX(),
                        -renderManagerAccessor.getRenderPosY(),
                        -renderManagerAccessor.getRenderPosZ(),
                    ),
            )
            GL15.glEndQuery(QUERY_MODE)
        }

        return !query.occluded
    }

    private fun drawSelectionBoundingBox(b: AxisAlignedBB) {
        GlStateManager.disableAlpha()
        GlStateManager.disableCull()
        GlStateManager.depthMask(false)
        GlStateManager.colorMask(false, false, false, false)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.worldRenderer
        buffer.apply {
            begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION)
            pos(b.maxX, b.maxY, b.maxZ).endVertex()
            pos(b.maxX, b.maxY, b.minZ).endVertex()
            pos(b.minX, b.maxY, b.maxZ).endVertex()
            pos(b.minX, b.maxY, b.minZ).endVertex()

            pos(b.minX, b.minY, b.maxZ).endVertex()
            pos(b.minX, b.minY, b.minZ).endVertex()
            pos(b.minX, b.maxY, b.minZ).endVertex()
            pos(b.minX, b.minY, b.minZ).endVertex()

            pos(b.maxX, b.maxY, b.minZ).endVertex()
            pos(b.maxX, b.minY, b.minZ).endVertex()
            pos(b.maxX, b.maxY, b.maxZ).endVertex()
            pos(b.maxX, b.minY, b.maxZ).endVertex()

            pos(b.minX, b.maxY, b.maxZ).endVertex()
            pos(b.minX, b.minY, b.maxZ).endVertex()
            pos(b.minX, b.minY, b.maxZ).endVertex()
            pos(b.maxX, b.minY, b.maxZ).endVertex()

            pos(b.minX, b.minY, b.minZ).endVertex()
            pos(b.maxX, b.minY, b.minZ).endVertex()
        }
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.enableAlpha()
    }

    private fun cleanupUnusedQueries() {
        val theWorld = McClient.world
        val loaded: Set<UUID> = theWorld.loadedEntityList.mapTo(mutableSetOf()) { it.uniqueID }

        queries.removeIf { (uuid, query) ->
            if (uuid !in loaded) {
                if (query.nextQuery != 0) GL15.glDeleteQueries(query.nextQuery)
                return@removeIf true
            }
            false
        }
    }

    private data class OcclusionQuery(
        var nextQuery: Int = 0,
        var refresh: Boolean = true,
        var occluded: Boolean = false,
        var executionTime: SimpleTimeMark = SimpleTimeMark.farPast(),
    )
}
