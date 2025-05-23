package com.github.itsempa.nautilus.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import com.github.itsempa.nautilus.data.core.NautilusErrorManager

// TODO: improve so it only actually creates the data that is necessary
class NautilusDebugEvent(private val list: MutableList<String>, private val search: String, private val all: Boolean) : SkyHanniEvent() {

    var empty = true
    private var currentTitle = ""
    private var irrelevant = false

    fun title(title: String) {
        if (currentTitle != "") {
            val msg = "NautilusDebugEvent duplicate titles and no data in between"
            NautilusErrorManager.logErrorWithData(
                IllegalStateException(msg),
                msg,
                "current title" to currentTitle,
                "new title" to title,
            )
        }

        currentTitle = title
    }

    fun title(title: String, block: NautilusDebugEvent.() -> Unit) {
        title(title)
        block(this)
        currentTitle = ""
        irrelevant = false
    }

    fun addIrrelevant(vararg data: Pair<String, Any?>) {
        addIrrelevant {
            for (pair in data) add("${pair.first}: ${pair.second}")
        }
    }

    fun addIrrelevant(builder: MutableList<String>.() -> Unit) = addIrrelevant(buildList(builder))

    fun addIrrelevant(text: String) = addIrrelevant(listOf(text))

    fun addIrrelevant(text: List<String>) {
        irrelevant = true
        addData(text)
    }

    fun addData(builder: MutableList<String>.() -> Unit) = addData(buildList(builder))

    fun addData(text: String) = addData(listOf(text))

    fun addData(text: List<String>) {
        if (currentTitle == "") error("Title not set")
        writeData(text)
        currentTitle = ""
        irrelevant = false
    }

    private fun writeData(text: List<String>) {
        fun addData() {
            empty = false
            list.add("")
            list.add("== $currentTitle ==")
            for (line in text) {
                list.add(" $line")
            }
        }
        if (all) {
            addData()
            return
        }
        if (irrelevant && search.isEmpty()) return
        if (search.isNotEmpty()) {
            if (!search.equalsIgnoreColor("all")) {
                if (!currentTitle.contains(search, ignoreCase = true)) {
                    return
                }
            }
        }
        addData()
    }
}
