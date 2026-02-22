import kotlinx.coroutines.*
import java.net.Socket
import java.io.BufferedReader
import kotlin.coroutines.cancellation.CancellationException

class Clientserveurmessage(private val client: Socket) {


    suspend fun handle() = coroutineScope {
        try {
            val reader: BufferedReader = client.getInputStream().bufferedReader()

            // Option A : version avec warning réduit (recommandée)
            while (isActive) {
                val message = try {
                    withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                } catch (e: CancellationException) {
                    throw e  // propage l'annulation
                }

                if (message == null) break  // client déconnecté

                println("Reçu : $message")

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