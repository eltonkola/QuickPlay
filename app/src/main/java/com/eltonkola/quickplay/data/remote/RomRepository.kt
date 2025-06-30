package com.eltonkola.quickplay.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import com.eltonkola.quickplay.data.RemoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.zip.ZipInputStream

//data class RomItem(
//    val name: String,
//    val filename: String,
//    val downloadUrl: String,
//    val mediaId: String = "",
//    val isDownloaded: Boolean = false,
//    val imageUrl: String = "",
//    val downloadFileName: String = "",
//    val isFavorite: Boolean = false
//)


interface RomRepository{
    suspend fun fetchRomsFromWebsite(): List<RemoteItem>
    suspend fun downloadRom(rom: RemoteItem): RemoteItem
    suspend fun deleteRom(filename: String)
}

class RomRepositoryImpl(private val context: Context) : RomRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("rom_prefs", Context.MODE_PRIVATE)
    private val romsDir = File(context.filesDir, "roms")

    init {
        if (!romsDir.exists()) {
            romsDir.mkdirs()
        }
    }

    override suspend fun fetchRomsFromWebsite(): List<RemoteItem> = withContext(Dispatchers.IO) {
        val baseUrl = "https://www.romsgames.net"
        val url = "$baseUrl/roms/super-nintendo/"

        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .get()

            val romElements = doc.select("a[href*='-rom-']")

            romElements.mapNotNull { element ->
                try {
                    val nameElement = element.select("div.text-sm").first()
                    val name = nameElement?.text()?.trim() ?: return@mapNotNull null
                    if (name.isBlank()) return@mapNotNull null

                    val imgElement = element.select("img").first()
                    val imageSrc = imgElement?.attr("src") ?: ""
                    val imageUrl = if (imageSrc.startsWith("//")) "https:$imageSrc" else imageSrc


                    val relativeUrl = element.attr("href")
                    val filename =
                        "${name.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim().replace(" ", "_")}.smc"
                    val downloadUrl =
                        if (relativeUrl.startsWith("http")) relativeUrl else "$baseUrl$relativeUrl"

                    // Extract mediaId from ROM page (we'll need to fetch it)
                    val mediaId = extractMediaId(downloadUrl)



                    RemoteItem(
                        name = name,
                        filename = filename,
                        downloadUrl = downloadUrl,
                        imageUrl = imageUrl,
                        mediaId = mediaId
                    )
                } catch (e: Exception) {
                    null
                }
            }


        } catch (e: Exception) {
            // Fallback with sample data for development
            listOf(
                RemoteItem(
                    "Super Mario World",
                    "super_mario_world.smc",
                    "$baseUrl/download/super-mario-world"
                ),
                RemoteItem(
                    "The Legend of Zelda: A Link to the Past",
                    "zelda_link_to_past.smc",
                    "$baseUrl/download/zelda-link-past"
                ),
                RemoteItem("Super Metroid", "super_metroid.smc", "$baseUrl/download/super-metroid"),
                RemoteItem(
                    "Donkey Kong Country",
                    "donkey_kong_country.smc",
                    "$baseUrl/download/donkey-kong-country"
                ),
                RemoteItem(
                    "Final Fantasy VI",
                    "final_fantasy_vi.smc",
                    "$baseUrl/download/final-fantasy-vi"
                ),
                RemoteItem("Chrono Trigger", "chrono_trigger.smc", "$baseUrl/download/chrono-trigger"),
                RemoteItem(
                    "Super Mario Kart",
                    "super_mario_kart.smc",
                    "$baseUrl/download/super-mario-kart"
                ),
                RemoteItem(
                    "Street Fighter II",
                    "street_fighter_ii.smc",
                    "$baseUrl/download/street-fighter-ii"
                )
            )
        }
    }


    private suspend fun extractMediaId(romPageUrl: String): String = withContext(Dispatchers.IO) {
        try {
            val fullUrl =
                if (romPageUrl.startsWith("http")) romPageUrl else "https://www.romsgames.net$romPageUrl"

            val doc = Jsoup.connect(fullUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .header("Accept-Language", "en-US,en;q=0.5")
                .timeout(10000)
                .get()

            // Look for mediaId in script tags - try multiple patterns
            val scripts = doc.select("script")
            for (script in scripts) {
                val scriptContent = script.html()

                // Pattern 1: mediaId: 12345
                var mediaIdRegex = Regex("mediaId\\s*:\\s*(\\d+)")
                var match = mediaIdRegex.find(scriptContent)
                if (match != null) {
                    return@withContext match.groupValues[1]
                }

                // Pattern 2: "mediaId": "12345"
                mediaIdRegex = Regex("\"mediaId\"\\s*:\\s*\"?(\\d+)\"?")
                match = mediaIdRegex.find(scriptContent)
                if (match != null) {
                    return@withContext match.groupValues[1]
                }

                // Pattern 3: mediaId = 12345
                mediaIdRegex = Regex("mediaId\\s*=\\s*(\\d+)")
                match = mediaIdRegex.find(scriptContent)
                if (match != null) {
                    return@withContext match.groupValues[1]
                }
            }

            // Look for download button with data attributes
            val downloadButton =
                doc.select("button[data-media-id], a[data-media-id], [data-media-id]").first()
            if (downloadButton != null) {
                val mediaId = downloadButton.attr("data-media-id")
                if (mediaId.isNotEmpty()) {
                    return@withContext mediaId
                }
            }

            // Look for form inputs with mediaId
            val mediaIdInput = doc.select("input[name=mediaId], input[id=mediaId]").first()
            if (mediaIdInput != null) {
                val mediaId = mediaIdInput.attr("value")
                if (mediaId.isNotEmpty()) {
                    return@withContext mediaId
                }
            }


            return@withContext ""
        } catch (e: Exception) {
            return@withContext ""
        }
    }


    private suspend fun cacheGameImagePersistently(
        context: Context,
        url: String,
        gameId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()

            val result = ImageLoader(context).execute(request)
            val drawable = result.drawable as? BitmapDrawable ?: return@withContext null
            val bitmap = drawable.bitmap

            val imageDir = File(context.getExternalFilesDir("images"), "")
            imageDir.mkdirs()

            val imageFile = File(imageDir, "$gameId.png")
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            return@withContext imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun downloadRom(rom: RemoteItem): RemoteItem = withContext(Dispatchers.IO) {
        try {
            if (rom.mediaId.isEmpty()) {
                throw Exception("Media ID not found for ${rom.name}")
            }

            // Prepare external roms directory
            val romsDir = File(
                context.getExternalFilesDir("roms")?.absolutePath
                    ?: throw Exception("External storage unavailable")
            )
            if (!romsDir.exists()) romsDir.mkdirs()

            // Step 1: POST to get download JSON
            val jsonUrl = "${rom.downloadUrl}?download"
            val jsonConnection = (URL(jsonUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
                )
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                setRequestProperty("Referer", rom.downloadUrl)
                setRequestProperty("Origin", "https://www.romsgames.net")
                doOutput = true
            }

            val postData = "mediaId=${rom.mediaId}"
            jsonConnection.outputStream.use { output ->
                output.write(postData.toByteArray(Charsets.UTF_8))
            }

            val jsonResponse = jsonConnection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonResponse)

            val finalDownloadUrl = json.optString("downloadUrl")
            val decodedFilename =
                URLDecoder.decode(json.optString("downloadName", rom.filename), "UTF-8")

            if (finalDownloadUrl.isBlank()) {
                throw Exception("downloadUrl missing in JSON response")
            }

            // Step 2: Download the ZIP file directly into external downloads folder
            val zipFile = File(romsDir, decodedFilename)
            (URL(finalDownloadUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                setRequestProperty("Referer", rom.downloadUrl)
            }.inputStream.use { input ->
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Step 3: Unzip .smc/.sfc from downloaded zip to roms folder
            val extractedRomFile = unzipFirstRomFile(zipFile, romsDir)
                ?: throw Exception("No valid ROM (.smc/.sfc) found inside ZIP")

            // Step 4: Delete the ZIP after extraction
            zipFile.delete()


            cacheGameImagePersistently(context, rom.imageUrl, rom.name)



            // Step 5: Return updated RomItem with the extracted ROM filename
            rom.copy(
                downloadFileName = extractedRomFile.name,
                isDownloaded = true,
                downloadUrl = "",  // clear or keep original if you want
                mediaId = ""       // clear mediaId after download if you want
            )

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Download failed: ${e.message}")
        }
    }

    // Helper unzip function (same as before)
    private fun unzipFirstRomFile(zipFile: File, outputDir: File): File? {
        ZipInputStream(zipFile.inputStream()).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                if (!entry.isDirectory && (name.endsWith(".smc") || name.endsWith(".sfc"))) {
                    val outFile = File(outputDir, File(entry.name).name)
                    FileOutputStream(outFile).use { out ->
                        zipIn.copyTo(out)
                    }
                    return outFile
                }
                entry = zipIn.nextEntry
            }
        }
        return null
    }

    override suspend fun deleteRom(filename: String) {
        val file = File(romsDir, filename)
        if (file.exists()) {
            file.delete()
        }
    }

}