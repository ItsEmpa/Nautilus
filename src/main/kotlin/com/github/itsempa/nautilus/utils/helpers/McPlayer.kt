package com.github.itsempa.nautilus.utils.helpers

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.item.ItemStack

@Suppress("unused")
object McPlayer {

    val self: EntityPlayerSP get() = selfNull!!
    val selfNull: EntityPlayerSP? get() = McClient.self.thePlayer
    val exists: Boolean get() = selfNull != null

    val isSneaking: Boolean get() = self.isSneaking
    val heldItem: ItemStack? get() = self.heldItem
    val pos: LorenzVec get() = self.position.toLorenzVec()
    val yaw: Float get() = self.rotationYaw
    val pitch: Float get() = self.rotationPitch
    val lookedAtPos: LorenzVec? get() = McClient.self.objectMouseOver.blockPos?.toLorenzVec()

    val controller: PlayerControllerMP get() = McClient.self.playerController

}
