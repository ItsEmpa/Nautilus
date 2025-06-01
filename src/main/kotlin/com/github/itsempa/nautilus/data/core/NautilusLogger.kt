package com.github.itsempa.nautilus.data.core

import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SkyBlockUtils
import at.hannibal2.skyhanni.utils.TimeUtils.formatCurrentTime
import com.github.itsempa.nautilus.utils.tryCatch
import java.io.File
import java.text.SimpleDateFormat
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.time.Duration.Companion.days

class NautilusLogger(filePath: String) {

    fun log(message: String?) = logger.info(message)


    companion object {
        private val LOG_DIRECTORY = File("config/nautilus/logs")
        private val PREFIX_PATH =
            "config/nautilus/logs/${SimpleDateFormat("yyyy_MM_dd/HH_mm_ss").formatCurrentTime()}"
        private var hasDone = false
    }

    private val format = SimpleDateFormat("HH:mm:ss")
    private val fileName = "$PREFIX_PATH/$filePath.log"

    private val logger: Logger by lazy {
        val logger = Logger.getLogger("Nautilus-Logger-${System.nanoTime()}")
        tryCatch {
            createParent(File(fileName))
            val handler = FileHandler(fileName)
            handler.encoding = "utf-8"
            logger.addHandler(handler)
            logger.useParentHandlers = false
            handler.formatter = object : Formatter() {
                override fun format(logRecord: LogRecord): String {
                    val message = logRecord.message
                    return format.formatCurrentTime() + " $message\n"
                }
            }
        }

        if (!hasDone && SkyBlockUtils.onHypixel) {
            hasDone = true
            OSUtils.deleteExpiredFiles(LOG_DIRECTORY, 7.days)
        }

        return@lazy logger
    }

    private fun createParent(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.isDirectory) {
            parent.mkdirs()
        }
    }
}
