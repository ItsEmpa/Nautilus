package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.RenderUtils.getViewerPos
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import java.awt.Color

object NautilusRenderUtils {

    /**
     * Method for rendering bounding boxes.
     * [lineWidth] is only used when [wireframe] is `true`. If not specified, uses default (idk what default value is)
     * [alphaMultiplier] is only used when [wireframe] is `false`.
     */
    fun SkyHanniRenderWorldEvent.drawBoundingBox(
        aabb: AxisAlignedBB,
        color: Color,
        alphaMultiplier: Float = 0.2f,
        lineWidth: Int? = null,
        wireframe: Boolean = false,
        throughBlocks: Boolean = false,
    ) {
        if (throughBlocks) {
            GlStateManager.disableDepth()
        }
        GlStateManager.disableCull()

        val vp = getViewerPos(partialTicks)
        val effectiveAABB = AxisAlignedBB(
            aabb.minX - vp.x, aabb.minY - vp.y, aabb.minZ - vp.z,
            aabb.maxX - vp.x, aabb.maxY - vp.y, aabb.maxZ - vp.z,
        )

        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.disableTexture2D()

        if (wireframe) effectiveAABB.drawWireframeBoundingBox(color, lineWidth)
        else effectiveAABB.drawFilledBoundingBox(color, alphaMultiplier)

        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()

        if (throughBlocks) {
            GlStateManager.enableDepth()
        }
    }

    private fun AxisAlignedBB.drawWireframeBoundingBox(color: Color, lineWidth: Int? = null) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        lineWidth?.let { GL11.glLineWidth(it.toFloat()) }

        with(color) {
            GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
        }
        // Bottom face
        worldRenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        tessellator.draw()

        // Top face
        worldRenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        tessellator.draw()

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        tessellator.draw()
    }

    private fun AxisAlignedBB.drawFilledBoundingBox(color: Color, alphaMultiplier: Float = 0.2f) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        // vertical
        GlStateManager.color(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f * alphaMultiplier,
        )
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        tessellator.draw()
        GlStateManager.color(
            color.red / 255f * 0.8f,
            color.green / 255f * 0.8f,
            color.blue / 255f * 0.8f,
            color.alpha / 255f * alphaMultiplier,
        )

        // x
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(minX, minY, minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        tessellator.draw()
        GlStateManager.color(
            color.red / 255f * 0.9f,
            color.green / 255f * 0.9f,
            color.blue / 255f * 0.9f,
            color.alpha / 255f * alphaMultiplier,
        )
        // z
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(minX, minY, minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        tessellator.draw()
    }

    fun SkyHanniRenderWorldEvent.exactEntityCenter(entity: Entity): LorenzVec = exactLocation(entity).up(entity.height / 2.0)

    @JvmStatic
    fun setColor(color: Color) {
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
    }
}
