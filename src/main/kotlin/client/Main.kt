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
suspend fun writeur(socket: Socket,compte: Compte) = coroutineScope {
    val writer = PrintWriter(socket.getOutputStream(), true)
    val s = Scanner(System.`in`)

    println("Client prêt. Tape 'ping' ou un message :")

    while (!shutdown) {

        if (s.hasNextLine()) {
            val messagebrut = s.nextLine().trim() // On récupère et on nettoie les espaces
            //val message = messagebrut.split("|")
            val message = messagebrut.split("|", limit = 2)
            val protocole:Protocole = when (message[0].uppercase()) {
                    "USERS"-> {
                        Protocole(action = "USERS", message = "")
                    }
                    "TEXT"-> {
                        Protocole(action = "TEXT", message = message[1])
                    }
                    "PING" -> {
                        Protocole(action = "PING", message = "PING")
                    }
                    "EXIT" -> {
                        shutdown=true
                        Protocole(action = "EXIT", message = "EXIT")
                    }
                    else -> {
                        Protocole(action = "TEXT", message = messagebrut)
                    }
                }
                val jsonExport = Json.encodeToString(protocole)
                writer.println(jsonExport)
        }
    }
}

suspend fun reader(socket: Socket,compte: Compte)= coroutineScope {
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
                if (objet.action == "REQUIREMENT" && objet.message == "KEY") {
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    val protocole =Protocole(action = "USERS", message = compte.getkey())
                    val jsonExport = Json.encodeToString(protocole)
                    writer.println(jsonExport)
                    continue
                }
                if (objet.action == "EXIT") {
                    println("Serveur déconnecté.")
                    shutdown = true
                    break
                }

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
    val compte : Compte = Compte()
    val socket = try {
        Socket("localhost", 9999)
    } catch (e: Exception) {
        println("Impossible de se connecter au serveur.")
        return@runBlocking
    }
    val jobReader = launch(Dispatchers.IO) {
        reader(socket,compte)
    }
    // On lance les deux coroutines en parallèle
    val jobWriter = launch(Dispatchers.IO) {
        writeur(socket,compte)
    }

    joinAll(jobWriter, jobReader)
    socket.close()
    println("Fin du programme.")
}