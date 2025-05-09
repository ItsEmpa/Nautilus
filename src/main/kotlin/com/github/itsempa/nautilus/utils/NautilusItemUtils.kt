package com.github.itsempa.nautilus.utils

import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid
import net.minecraft.item.ItemStack
import java.util.UUID

object NautilusItemUtils {


    fun ItemStack.getUUID(): UUID? {
        val string = getItemUuid() ?: return null
        return UUID.fromString(string)
    }

    fun ItemStack.getBookOfStats(): Int? = getAttributeInt("stats_book")

    fun ItemStack.getAttributeInt(label: String): Int? =
        getExtraAttributes()?.getInteger(label)?.takeUnless { it == 0 }


}
