
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.Socket
import java.time.Duration
import java.time.LocalTime
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class Protocole(
    val action: String,
    val message: String,
    val heure: LocalTime? = null
)
class Clientserveurmessage(private val client: Socket) {


    suspend fun handle() = coroutineScope {
        try {

            val reader: BufferedReader = client.getInputStream().bufferedReader()
            val out = client.getOutputStream()
            val writer = out.writer().buffered()
            writer.write("BIENVENUE SUR LE SERVEUR\n")
            writer.flush() // FORCE l'envoi sur le réseau

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
                    "TEXT"-> {
                        println("Reçu : ${objet.message}")

                    }
                    "PING" -> {
                        try {
                            val latence = Duration.between(objet.heure, LocalTime.now()).toMillis()
                            println(latence)
                            writer.write(Json.encodeToString(Protocole(action = "PING", message = "PONG", heure = LocalTime.now())))
                            writer.flush()
                        } catch (e: Exception) {
                            writer.write(Json.encodeToString(Protocole(action = "ERREUR", message = "Format d'heure invalide", heure = LocalTime.now())))
                            writer.flush()
                        }
                    }
                    ""-> {
                        break
                    }

                    else -> {
                        writer.write("ERREUR : Commande inconnue")
                        writer.flush()
                        println("Reçu : ${objet.message}")
                    }
                }
                if (message == null) break  // client déconnecté
            }

        } catch (e: CancellationException) {
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