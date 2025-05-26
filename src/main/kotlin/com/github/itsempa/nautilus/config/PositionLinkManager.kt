package com.github.itsempa.nautilus.config

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.core.config.PositionList
import at.hannibal2.skyhanni.deps.moulconfig.annotations.ConfigLink
import at.hannibal2.skyhanni.utils.IdentityCharacteristics
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.data.core.NautilusErrorManager
import com.github.itsempa.nautilus.mixins.hooks.NautilusPositionData.Companion.isNautilus
import com.github.itsempa.nautilus.utils.tryError

object PositionLinkManager {

    fun init(features: Features) {
        tryError("Couldn't load config links!") {
            findPositionLinks(features, mutableSetOf())
        }
    }

    private fun findPositionLinks(obj: Any?, slog: MutableSet<IdentityCharacteristics<Any>>) {
        if (obj == null) return
        if (!obj.javaClass.name.startsWith(Nautilus.PATH)) return
        val ic = IdentityCharacteristics(obj)
        if (ic in slog) return
        slog.add(ic)
        for (field in obj.javaClass.declaredFields.map { it.makeAccessible() }) {
            if (field.type != Position::class.java && field.type != PositionList::class.java) {
                findPositionLinks(field.get(obj), slog)
                continue
            }
            val configLink = field.getAnnotation(ConfigLink::class.java) ?: continue
            if (field.type == Position::class.java) {
                val position = field.get(obj) as Position
                println("setting link for $field")
                position.setLink(configLink)
                position.isNautilus = true
            } else if (field.type == PositionList::class.java) {
                NautilusErrorManager.nautilusError(
                    "PositionList config links are currently not supported",
                    "field" to field,
                    "obj" to obj,
                )
            }
        }
    }

}
