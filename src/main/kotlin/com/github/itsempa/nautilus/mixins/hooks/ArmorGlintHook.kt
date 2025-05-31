package com.github.itsempa.nautilus.mixins.hooks

import com.github.itsempa.nautilus.Nautilus
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

object ArmorGlintHook {

    private val config get() = Nautilus.feature.misc.hideArmorGlint

    @JvmStatic
    fun shouldHide(): Boolean = config

    @JvmStatic
    fun shouldHideGlint(stack: ItemStack): Boolean = stack.item is ItemArmor && config

}
