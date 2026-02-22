import jdk.internal.joptsimple.internal.Messages.message
import kotlinx.coroutines.*
import java.net.Socket
import java.io.BufferedReader
import java.io.PrintWriter
import kotlin.coroutines.cancellation.CancellationException

class Clientserveurmessage(private val client: Socket) {


    suspend fun handle() = coroutineScope {
        try {
            val reader: BufferedReader = client.getInputStream().bufferedReader()
            val out = client.getOutputStream()
            val writer = out.writer().buffered() // Plus direct que PrintWriter pour tester
            writer.write("BIENVENUE SUR LE SERVEUR\n")
            writer.flush() // FORCE l'envoi sur le réseau

            println("DEBUG : Envoi du message de test...")
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