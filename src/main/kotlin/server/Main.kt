package server

import kotlinx.coroutines.*
import java.net.ServerSocket

fun main() = runBlocking {
    val server = ServerSocket(9999)
    println("serveur démarré")

    val serverScope = CoroutineScope(Dispatchers.IO)

    try {
        while (isActive) {
            val client = server.accept()
            println("Nouvelle connexion de ${client.inetAddress.hostAddress}")

            // Lance le handler
            serverScope.launch {
                Clientservermessage(client).handle()
            }

        }
    } catch (e: CancellationException) {
        // arrêt demandé
    } finally {
        server.close()
        serverScope.cancel()
        serverScope.coroutineContext.job.join()
        println("server arrêté")
    }
}