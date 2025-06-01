package com.github.itsempa.nautilus.config.core.loader

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.json.asIntOrNull
import at.hannibal2.skyhanni.utils.json.shDeepCopy
import com.github.itsempa.nautilus.data.core.NautilusLogger
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object NautilusConfigMigrator {

    private val logger = NautilusLogger("ConfigMigration")

    const val VERSION = 1

    fun fixConfig(config: JsonObject): JsonObject {
        val lastVersion = (config["lastVersion"] as? JsonPrimitive)?.asIntOrNull ?: -1
        if (lastVersion > VERSION) {
            logger.log("Attempted to downgrade config version")
            config.add("lastVersion", JsonPrimitive(VERSION))
            return config
        }
        if (lastVersion == VERSION) return config
        return (lastVersion until VERSION).fold(config) { acc, prev ->
            val newVersion = prev + 1
            logger.log("Starting config transformation from version $prev to $newVersion")
            val storage = acc["storage"]?.asJsonObject
            val dynamicPrefix = mapOf(
                "#profile" to (
                    storage?.get("profiles")?.asJsonObject?.entrySet()?.map { "storage.profiles.${it.key}" }.orEmpty()
                    ),
            )
            val migration = NautilusConfigFixEvent(
                acc,
                JsonObject().also {
                    it.add("lastVersion", JsonPrimitive(newVersion))
                },
                prev, 0, dynamicPrefix, logger,
            ).also { it.post() }
            logger.log("Transformations scheduled: ${migration.new}")
            val mergesPerformed = merge(migration.old, migration.new)
            logger.log("Migration done with $mergesPerformed merges and ${migration.movesPerformed} moves performed")
            migration.old
        }.also {
            logger.log("Final config: $it")
        }
    }

    private fun merge(originalObject: JsonObject, overrideObject: JsonObject): Int {
        var count = 0
        for ((key, newElement) in overrideObject.entrySet()) {
            val element = originalObject[key]
            if (element is JsonObject && newElement is JsonObject) {
                count += merge(element, newElement)
            } else {
                if (element != null) {
                    logger.log("Encountered destructive merge. Erasing $element in favour of $newElement.")
                    count++
                }
                originalObject.add(key, newElement)
            }
        }
        return count
    }
}

data class NautilusConfigFixEvent(
    val old: JsonObject,
    val new: JsonObject,
    val oldVersion: Int,
    var movesPerformed: Int,
    val dynamicPrefix: Map<String, List<String>>,
    private val logger: NautilusLogger,
) : SkyHanniEvent() {
    init {
        dynamicPrefix.entries
            .filter { it.value.isEmpty() }
            .forEach {
                logger.log("Dynamic prefix ${it.key} does not resolve to anything.")
            }
    }

    fun transform(since: Int, path: String, transform: (JsonElement) -> JsonElement = { it }) {
        move(since, path, path, transform)
    }

    fun add(since: Int, path: String, value: () -> JsonElement) {
        if (since <= oldVersion) {
            logger.log("Skipping add of $value to $path ($since <= $oldVersion)")
            return
        }
        if (since > NautilusConfigMigrator.VERSION) {
            error("Illegally new version $since > ${NautilusConfigMigrator.VERSION}")
        }
        if (since > oldVersion + 1) {
            logger.log("Skipping add of $value to $path (will be done in another pass)")
            return
        }
        val np = path.split(".")
        if (np.first().startsWith("#")) {
            val realPrefixes = dynamicPrefix[np.first()]
            if (realPrefixes == null) {
                logger.log("Could not resolve dynamic prefix $path")
                return
            }
            for (realPrefix in realPrefixes) {
                add(since, "$realPrefix.${path.substringAfter('.')}", value)
                return
            }
        }
        val newParentElement = new.at(np.dropLast(1), true)
        if (newParentElement !is JsonObject) {
            logger.log(
                "Skipping add of $value to $path, since another element already inhabits that path",
            )
            return
        }
        newParentElement.add(np.last(), value())
        logger.log("Added element to $path")
        return
    }

    fun move(since: Int, oldPath: String, newPath: String, transform: (JsonElement) -> JsonElement = { it }) {
        if (since <= oldVersion) {
            logger.log("Skipping move from $oldPath to $newPath ($since <= $oldVersion)")
            return
        }
        if (since > NautilusConfigMigrator.VERSION) {
            error("Illegally new version $since > ${NautilusConfigMigrator.VERSION}")
        }
        if (since > oldVersion + 1) {
            logger.log("Skipping move from $oldPath to $newPath (will be done in another pass)")
            return
        }
        val op = oldPath.split(".")
        val np = newPath.split(".")
        if (op.first().startsWith("#")) {
            require(np.first() == op.first())
            val realPrefixes = dynamicPrefix[op.first()]
            if (realPrefixes == null) {
                logger.log("Could not resolve dynamic prefix $oldPath")
                return
            }
            for (realPrefix in realPrefixes) {
                move(
                    since,
                    "$realPrefix.${oldPath.substringAfter('.')}",
                    "$realPrefix.${newPath.substringAfter('.')}", transform,
                )
                return
            }
        }
        val oldElem = old.at(op, false)
        if (oldElem == null) {
            logger.log("Skipping move from $oldPath to $newPath ($oldPath not present)")
            return
        }
        val newParentElement = new.at(np.dropLast(1), true)
        if (newParentElement !is JsonObject) {
            logger.log(
                "Catastrophic: element at path $old could not be relocated to $new, " +
                    "since another element already inhabits that path",
            )
            return
        }
        movesPerformed++
        newParentElement.add(np.last(), transform(oldElem.shDeepCopy()))
        logger.log("Moved element from $oldPath to $newPath")
    }

    companion object {

        private fun JsonElement.at(chain: List<String>, init: Boolean): JsonElement? {
            if (chain.isEmpty()) return this
            if (this !is JsonObject) return null
            var obj = this[chain.first()]
            if (obj == null && init) {
                obj = JsonObject()
                this.add(chain.first(), obj)
            }
            return obj?.at(chain.drop(1), init)
        }
    }
}
