package com.eltonkola.quickplay.data

import android.R.attr.port
import android.content.Context
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.local.GameEntity
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.security.KeyStore
import java.util.*
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.KeyManagerFactory


@Singleton
class WebServerManager @Inject constructor(
    private val context: Context,
    private val gameDao: GameDao
) {

    data class ServerState(
        val running: Boolean?  = false,
        val ipAddress: String? = null,
        val port: Int? = null,
        val error: String? = null
    )

    private var server: NanoHTTPD? = null
    private val _serverState = MutableStateFlow(ServerState( running = false))
    val serverState: StateFlow<ServerState> = _serverState

    private val supportedExtensions = listOf("smc", "sfc", "zip")
    private lateinit var uploadDir: File

    fun startServer() {
        if (_serverState.value.running ==  true) return

        _serverState.value = ServerState(running = null)
        try {
            uploadDir = File(
                context.getExternalFilesDir("roms")?.absolutePath
                    ?: throw Exception("External storage unavailable")
            )
            if (!uploadDir.exists()) uploadDir.mkdirs()


            val password = "iboughtosmanysnesgamesasakid"
            val port =  findFreePort()
            val ip = getLocalIpAddress()
            val keyStoreFile = File(context.filesDir, "keystore.p12")

            if (keyStoreFile.exists()) {
                keyStoreFile.delete()
            }

            SelfSignedKeystore.createKeystore(file = keyStoreFile, password = password)

            val keyStore = KeyStore.getInstance("PKCS12").apply {
                load(keyStoreFile.inputStream(), password.toCharArray())
            }

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                init(keyStore, password.toCharArray())
            }

            server = object : NanoHTTPD(port) {
                init {
                    makeSecure(
                        makeSSLSocketFactory(keyStore, keyManagerFactory),
                        null
                    )
                }

                override fun serve(session: IHTTPSession): Response {
                    return when {
                        session.method == Method.POST && session.uri == "/upload" -> handleUpload(session)
                        else -> serveForm()
                    }
                }

                private fun serveForm(): Response {
                    return newFixedLengthResponse(
                        Response.Status.OK, "text/html", """
                        <html><body>
                        <form method='post' enctype='multipart/form-data' action='/upload'>
                            <input type='file' name='file'/>
                            <input type='submit'/>
                        </form>
                        </body></html>
                        """.trimIndent()
                    )
                }

                private fun handleUpload(session: IHTTPSession): Response {
                    return try {

                        val files = HashMap<String, String>()
                        session.parseBody(files) // This will fill 'files' with uploaded temp files
                        val params = session.parameters

                        val fileFieldName = "file" // the name attribute in your HTML form

                        val uploadedFilePath = files[fileFieldName] // path to temp file
                        val uploadedFile = File(uploadedFilePath)


                        // Extract the original filename
                        val originalFileName = params[fileFieldName]?.firstOrNull() ?: "uploaded.rom"

                        println("tempFile: $originalFileName -  ${uploadedFile.absolutePath}")


                        if (!isValidExtension(originalFileName)) {
                            return newFixedLengthResponse("Unsupported file type")
                        }



                        val romsDir = File(
                            context.getExternalFilesDir("roms")?.absolutePath
                                ?: throw Exception("External storage unavailable")
                        )

                        val savedFile = File(romsDir, originalFileName)
                        uploadedFile.copyTo(savedFile, overwrite = true)

                        val gameFile = if (originalFileName.endsWith(".zip", true)) {
                            unzipFirstRomFile(savedFile, uploadDir) ?: return newFixedLengthResponse("No ROM in ZIP")
                        } else savedFile

                        insertGame(gameFile)
                        newFixedLengthResponse("Uploaded successfully  ${savedFile.name}")



                    } catch (e: Exception) {
                        newFixedLengthResponse("Error: ${e.message}")
                    }
                }
            }

            server?.start()

            _serverState.value = ServerState(
                running = true,
                ipAddress = ip,
                port = port
            )

        } catch (e: Exception) {
            e.printStackTrace()
            _serverState.value = ServerState(false, error = e.message)
        }
    }

    private fun findFreePort(): Int {
        for (port in 8443..8453) {
            try {
                ServerSocket(port).use { return port }
            } catch (e: Exception) {
                continue
            }
        }
        return 0
    }

    fun stopServer() {
        server?.stop()
        server = null
        _serverState.value = ServerState( running = false)
    }

    private fun getLocalIpAddress(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "localhost"
                    }
                }
            }
            "localhost"
        } catch (e: Exception) {
            "localhost"
        }
    }

    private fun isValidExtension(fileName: String): Boolean {
        return fileName.substringAfterLast('.', "").lowercase() in supportedExtensions
    }

    private fun insertGame(file: File) {
        val gameEntity = GameEntity(
            name = file.nameWithoutExtension,
            imageUrl = "",
            filename = file.name,
            isFavorite = false
        )
        CoroutineScope(Dispatchers.IO).launch {
            gameDao.insertGame(gameEntity)
        }
    }

    private fun unzipFirstRomFile(zipFile: File, outputDir: File): File? {
        ZipInputStream(zipFile.inputStream()).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                if (!entry.isDirectory && (name.endsWith(".smc") || name.endsWith(".sfc"))) {
                    val outFile = File(outputDir, File(entry.name).name)
                    outFile.outputStream().use { out -> zipIn.copyTo(out) }
                    zipFile.delete()
                    return outFile
                }
                entry = zipIn.nextEntry
            }
        }
        return null
    }
}
