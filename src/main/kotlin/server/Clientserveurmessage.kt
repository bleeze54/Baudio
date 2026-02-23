package server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.Socket
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class Protocole(
    val action: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
class Clientservermessage(private val client: Socket, private val password: String? = null,val server: Server) {

    suspend fun handle() = coroutineScope {
        try {

            val reader: BufferedReader = client.getInputStream().bufferedReader()
            val out = client.getOutputStream()
            val writer = out.writer().buffered()
            if (password != null) {
                writer.write(Json.encodeToString(Protocole(action = "REQUIREMENT", message = "password"))+ "\n")
                writer.flush()
                val message = try {
                    withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                } catch (e: CancellationException) {
                    throw e  // propage l'annulation
                }
                val objet = Json.decodeFromString<Protocole>(message)
                if (md5Hash(objet.message) != password){
                    writer.write(Json.encodeToString(Protocole(action = "ERROR", message = "acces refusé"))+ "\n")
                    writer.flush()
                    writer.close()
                    client.close()
                }
            }
            writer.write(Json.encodeToString(Protocole(action = "Info", message = "Bonjour"))+ "\n")
            writer.flush()
            println("DEBUG : Envoi du message de test...")
            while (isActive) {
                val message = try {
                    withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                } catch (e: CancellationException) {
                    throw e  // propage l'annulation
                }
                val objet = Json.decodeFromString<Protocole>(message)
                when (objet.action) {
                    "USERS"-> {
                        server.getclients()
                        println("Text Recu : ${server.getclients()}")
                        writer.write(Json.encodeToString(Protocole(action = "USERS", server.getclients().toString()))+ "\n")
                        writer.flush()
                    }
                    "TEXT"-> {
                        println("Text Recu : ${objet.message}")
                        writer.write(Json.encodeToString(Protocole(action = "INFO", message = "OK"))+ "\n")
                        writer.flush()

                    }
                    "EXIT"-> {
                        println("fin de connection de${client.inetAddress.hostAddress}")
                        writer.write(Json.encodeToString(Protocole(action = "EXIT", message = "OK"))+ "\n")
                        writer.flush()
                        client.close()
                        break

                    }
                    "PING" -> {
                        try {
                            val latence = System.currentTimeMillis() - objet.timestamp
                            println(latence)
                            writer.write(Json.encodeToString(Protocole(action = "PING", message = "PONG! $latence"))+ "\n")
                            writer.flush()
                        } catch (e: Exception) {
                            println(e)
                            writer.write(Json.encodeToString(Protocole(action = "ERREUR", message = "Format d'heure invalide"))+ "\n")
                            writer.flush()
                        }
                    }

                    else -> {
                        writer.write(Json.encodeToString(Protocole(action = "INFO", message = "commande Inconue"))+ "\n")
                        writer.flush()
                        println("Reçu : ${objet.message}")
                    }
                }

            }

        } catch (e: CancellationException) {
            println(e)
            // Annulation normale
        } catch (e: Exception) {
            println("Erreur avec le client ${client.inetAddress.hostAddress}: ${e.message}")
        } finally {
            try {
                client.close()
            } catch (_: Exception) {
            }
            println("Client déconnecté → ${client.inetAddress.hostAddress}")
        }
    }
}