package com.github.itsempa.nautilus.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.data.repo.RepoUtils
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import com.github.itsempa.nautilus.Nautilus
import com.github.itsempa.nautilus.config.ConfigManager
import com.github.itsempa.nautilus.events.NautilusCommandRegistrationEvent
import com.github.itsempa.nautilus.events.NautilusRepositoryReloadEvent
import com.github.itsempa.nautilus.modules.Module
import com.github.itsempa.nautilus.utils.NautilusChat
import com.github.itsempa.nautilus.utils.NautilusUtils
import com.github.itsempa.nautilus.utils.tryCatch
import com.github.itsempa.nautilus.utils.tryOrNull
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.commons.io.FileUtils
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.zip.ZipInputStream

/** Taken from SkyHanni */
@Module
object NautilusRepoManager {

    class RepoError : Error {
        constructor(errorMessage: String) : super(errorMessage)
        constructor(errorMessage: String, cause: Throwable) : super(errorMessage, cause)
    }

    val gson get() = ConfigManager.gson

    private val repoLocation = File(ConfigManager.directory, "repo")

    private val config get() = Nautilus.feature.dev

    const val DEFAULT_USER = "ItsEmpa"
    const val DEFAULT_NAME = "Nautilus-REPO"
    const val DEFAULT_BRANCH = "main"

    fun initRepo() {
        shouldManuallyReload = true
        Nautilus.launchIOCoroutine {
            fetchRepository(command = false)
            reloadRepository()
        }
    }

    private var error = false
    private var lastRepoUpdate = SimpleTimeMark.now()
    private var repoDownloadFailed = false

    private val successfulConstants = mutableListOf<String>()
    private val unsuccessfulConstants = mutableListOf<String>()

    private var lastConstant: String? = null

    fun setLastConstant(constant: String) {
        lastConstant?.let {
            successfulConstants.add(it)
        }
        lastConstant = constant
    }

    private var shouldManuallyReload = false

    private var currentlyFetching = false
    private var commitTime: SimpleTimeMark? = null

    @HandleEvent
    fun onCommandRegistration(event: NautilusCommandRegistrationEvent) {
        event.register("nautilusupdaterepo") {
            this.aliases = listOf("ntupdaterepo")
            this.description = "Download the Nautilus repo again."
            this.category = CommandCategory.USERS_BUG_FIX
            this.callback { updateRepo() }
        }
    }

    @JvmStatic
    fun updateRepo() {
        shouldManuallyReload = true
        Nautilus.launchIOCoroutine {
            fetchRepository(command = true)
            reloadRepository("Repo updated successfully.")
            if (unsuccessfulConstants.isNotEmpty()) {
                NautilusUtils.logErrorWithData(
                    IllegalStateException("Repo update failed"),
                    "Error updating reading Sh Repo",
                    "unsuccessfulConstants" to unsuccessfulConstants,
                )
                ChatUtils.chat("§cFailed to load the repo! See above for more infos.")
                return@launchIOCoroutine
            }
        }
    }

    private fun fetchRepository(command: Boolean) {
        if (currentlyFetching) return
        currentlyFetching = true
        doTheFetching(command)
        currentlyFetching = false
    }

    private fun reloadRepository(answerMessage: String = "") {
        if (!shouldManuallyReload) return
        // TODO move away and use own error manager
        ErrorManager.resetCache()
        error = false
        successfulConstants.clear()
        unsuccessfulConstants.clear()
        lastConstant = null

        NautilusRepositoryReloadEvent(repoLocation, gson).post {
            error = true
            lastConstant?.let {
                unsuccessfulConstants.add(it)
            }
            lastConstant = null
        }
        if (answerMessage.isNotEmpty() && !error) NautilusChat.chat("§a$answerMessage")
        if (error) {
            NautilusChat.clickableChat(
                "Error with the repo detected, try /ntupdaterepo to fix it!",
                hover = "§eClick to update the repo!",
                prefixColor = "§c",
            ) { updateRepo() }
            if (unsuccessfulConstants.isEmpty()) unsuccessfulConstants.add("All Constants")
        }
    }

    private fun doTheFetching(command: Boolean) {
        try {
            val (currentDownloadedCommit, currentDownloadedCommitTime) = readCurrentCommit() ?: (null to null)
            commitTime = currentDownloadedCommitTime
            var latestRepoCommit: String? = null
            var latestRepoCommitTime: SimpleTimeMark? = null

            try {
                InputStreamReader(URL(getCommitApiUrl()).openStream())
                    .use { inReader ->
                        val commits: JsonObject = gson.fromJson(inReader, JsonObject::class.java)
                        latestRepoCommit = commits["sha"].asString
                        val formattedDate = commits["commit"].asJsonObject["committer"].asJsonObject["date"].asString
                        latestRepoCommitTime = Instant.parse(formattedDate).toEpochMilli().asTimeMark()
                    }
            } catch (e: Exception) {
                NautilusUtils.logErrorWithData(
                    e,
                    "Error while loading data from repo",
                    "command" to command,
                    "currentDownloadedCommit" to currentDownloadedCommit,
                )
                repoDownloadFailed = true
            }

            if (repoLocation.exists() &&
                currentDownloadedCommit == latestRepoCommit &&
                unsuccessfulConstants.isEmpty()
            ) {
                if (command) {
                    NautilusChat.clickToClipboard(
                        "§7The repo is already up to date!",
                        lines = buildList {
                            add("latest commit sha: §e$currentDownloadedCommit")
                            latestRepoCommitTime?.let { latestTime ->
                                add("latest commit time: §b$latestTime")
                                add("  (§b${latestTime.passedSince().format()} ago§7)")
                            }
                        },
                    )
                    shouldManuallyReload = false
                }
                return
            }

            if (command) {
                NautilusChat.clickToClipboard(
                    "Repo is outdated, updating..",
                    lines = buildList {
                        add("local commit sha: §e$latestRepoCommit")
                        currentDownloadedCommitTime?.let { localTime ->
                            add("local commit time: §b$localTime")
                            add("  (§b${localTime.passedSince().format()} ago§7)")
                        }
                        add("")
                        add("latest commit sha: §e$currentDownloadedCommit")
                        latestRepoCommitTime?.let { latestTime ->
                            add("latest commit time: §b$latestTime")
                            add("  (§b${latestTime.passedSince().format()} ago§7)")
                            currentDownloadedCommitTime?.let { localTime ->
                                val outdatedDuration = latestTime - localTime
                                add("")
                                add("outdated by: §b${outdatedDuration.format()}")
                            }
                        }
                    },
                )
            }

            lastRepoUpdate = SimpleTimeMark.now()

            repoLocation.mkdirs()
            val itemsZip = File(repoLocation, "nt-repo-main.zip")
            itemsZip.createNewFile()

            val url = URL(getDownloadUrl(latestRepoCommit))
            val urlConnection = url.openConnection().apply {
                connectTimeout = 15000
                readTimeout = 30000
            }

            RepoUtils.recursiveDelete(repoLocation)
            repoLocation.mkdirs()

            try {
                urlConnection.getInputStream().use { stream ->
                    FileUtils.copyInputStreamToFile(
                        stream,
                        itemsZip,
                    )
                }
            } catch (e: IOException) {
                NautilusUtils.logErrorWithData(
                    e,
                    "Failed to download Nautilus Repo",
                    "url" to url,
                    "command" to command,
                )
                repoDownloadFailed = true
                return
            }

            unzipIgnoreFirstFolder(
                itemsZip.absolutePath,
                repoLocation.absolutePath,
            )
            if (currentDownloadedCommit == null || currentDownloadedCommit != latestRepoCommit) {
                writeCurrentCommit(latestRepoCommit, latestRepoCommitTime)
            }
            commitTime = latestRepoCommitTime

        } catch (e: Exception) {
            NautilusUtils.logErrorWithData(
                e,
                "Failed to download Nautilus Repository",
                "command" to command,
            )
            repoDownloadFailed = true
            return
        }
        repoDownloadFailed = false
    }

    private fun writeCurrentCommit(commit: String?, time: SimpleTimeMark?) {
        val newCurrentCommitJSON = JsonObject()
        newCurrentCommitJSON.addProperty("sha", commit)
        time?.let {
            newCurrentCommitJSON.addProperty("time", it.toMillis())
        }
        tryCatch {
            writeJson(newCurrentCommitJSON, getCurrentCommitFile())
        }
    }

    private fun getCurrentCommitFile(): File = File(ConfigManager.directory, "currentCommit.json")

    private fun readCurrentCommit(): Pair<String, SimpleTimeMark?>? {
        val currentCommitJSON = getJsonFromFile(getCurrentCommitFile()) ?: return null
        val sha = currentCommitJSON.get("sha")?.asString ?: return null
        val time = currentCommitJSON.get("time")?.asLong?.asTimeMark()
        return sha to time
    }

    /**
     * Parses a file in to a JsonObject.
     */
    private fun getJsonFromFile(file: File?): JsonObject? {
        if (file == null) return null
        return tryOrNull {
            BufferedReader(
                InputStreamReader(
                    FileInputStream(file),
                    StandardCharsets.UTF_8,
                ),
            ).use { reader ->
                gson.fromJson(reader, JsonObject::class.java)
            }
        }
    }

    private fun getCommitApiUrl(): String {
        val (repoUser, repoName, repoBranch) = config.repoLocation
        return "https://api.github.com/repos/$repoUser/$repoName/commits/$repoBranch"
    }

    private fun getDownloadUrl(commitId: String?): String {
        val (repoUser, repoName) = config.repoLocation
        return "https://github.com/$repoUser/$repoName/archive/$commitId.zip"
    }

    @Throws(IOException::class)
    fun writeJson(json: JsonObject?, file: File) {
        file.createNewFile()
        BufferedWriter(
            OutputStreamWriter(
                FileOutputStream(file),
                StandardCharsets.UTF_8,
            ),
        ).use { writer -> writer.write(gson.toJson(json)) }
    }

    /**
     * Modified from https://www.journaldev.com/960/java-unzip-file-example
     */
    fun unzipIgnoreFirstFolder(zipFilePath: String, destinationDirectory: String) {
        val dir = File(destinationDirectory)
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs()
        val fis: FileInputStream
        // buffer for read and write data to file
        val buffer = ByteArray(1024)
        try {
            fis = FileInputStream(zipFilePath)
            val zis = ZipInputStream(fis)
            var ze = zis.nextEntry
            while (ze != null) {
                if (!ze.isDirectory) {
                    var fileName = ze.name
                    fileName = fileName.substring(fileName.split("/").toTypedArray()[0].length + 1)
                    val newFile = File(destinationDirectory + File.separator + fileName)
                    // create directories for sub directories in zip
                    File(newFile.parent).mkdirs()
                    if (!isInTree(dir, newFile)) {
                        throw RuntimeException(
                            "Nautilus detected an invalid zip file. This is a potential security risk, " +
                                "please report this on the Nautilus discord.",
                        )
                    }
                    val fos = FileOutputStream(newFile)
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                // close this ZipEntry
                zis.closeEntry()
                ze = zis.nextEntry
            }
            // close last ZipEntry
            zis.closeEntry()
            zis.close()
            fis.close()
        } catch (e: IOException) {
            NautilusUtils.logErrorWithData(
                e,
                "unzipIgnoreFirstFolder failed",
                "zipFilePath" to zipFilePath,
                "destinationDirectory" to destinationDirectory,
            )
        }
    }

    @Suppress("NAME_SHADOWING")
    @Throws(IOException::class)
    private fun isInTree(rootDirectory: File, file: File): Boolean {
        var rootDirectory = rootDirectory
        var file: File? = file
        file = file!!.canonicalFile
        rootDirectory = rootDirectory.canonicalFile
        while (file != null) {
            if (file == rootDirectory) return true
            file = file.parentFile
        }
        return false
    }

    fun <T> getConstant(repoLocation: File, constant: String, gson: Gson, clazz: Class<T>?, type: Type? = null): T {
        val name = "constants/$constant.json"
        val jsonFile = File(repoLocation, name)
        if (!jsonFile.isFile) throw RepoError("Repo file '$name' not found.")
        BufferedReader(InputStreamReader(FileInputStream(jsonFile), StandardCharsets.UTF_8)).use { reader ->
            return if (type == null) gson.fromJson(reader, clazz)
            else gson.fromJson(reader, type)
        }
    }

}
