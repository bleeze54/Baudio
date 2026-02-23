package client

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.Protocole
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

@Volatile
var shutdown = false


@Serializable
data class Protocole(
    val action: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val salutation:String ="salut"
)
suspend fun writeur(socket: Socket) = coroutineScope {
    val writer = PrintWriter(socket.getOutputStream(), true)
    val s = Scanner(System.`in`)

    println("Client prêt. Tape 'ping' ou un message :")

    while (!shutdown) {

        if (s.hasNextLine()) {
            val messagebrut = s.nextLine().trim() // On récupère et on nettoie les espaces
            if (messagebrut.lowercase() == "exit") {
                shutdown = true
                break
            }
            //val message = messagebrut.split("|")
            val message = messagebrut.split("|", limit = 2)
            val protocole:Protocole = when (message[0].uppercase()) {
                    "TEXT"-> {
                        Protocole(action = "TEXT", message = message[1])
                    }
                    "PING" -> {
                        Protocole(action = "PING", message = "PING")
                    }
                    else -> {
                        Protocole(action = "TEXT", message = message[0])
                    }
                }
                val jsonExport = Json.encodeToString(protocole)
                writer.println(jsonExport)
        }
    }
}

suspend fun reader(socket: Socket)= coroutineScope {
        try {
            val reader: BufferedReader = socket.getInputStream().bufferedReader()
            while (!shutdown) {
                val message = reader.readLine()
                if (message == null) {
                    println("Serveur déconnecté.")
                    shutdown = true
                    break
                }
                val objet = Json.decodeFromString<Protocole>(message)
                println("[REÇU] :${objet.message}")
                print(">") // retrour ellegant a la ligne
            }
        } catch (e: Exception) {
            if (!shutdown) println("Erreur lecture : ${e.message}")
        } finally {
            shutdown = true
        }
    }


fun main() = runBlocking {
    val socket = try {
        Socket("localhost", 9999)
    } catch (e: Exception) {
        println("Impossible de se connecter au serveur.")
        return@runBlocking
    }

    val jobReader = launch(Dispatchers.IO) {
        reader(socket)
    }
    // On lance les deux coroutines en parallèle
    val jobWriter = launch(Dispatchers.IO) {
        writeur(socket)
    }

    joinAll(jobWriter, jobReader)

    socket.close()
    println("Fin du programme.")
}