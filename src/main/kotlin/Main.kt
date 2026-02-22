package org.example
import Protocole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.time.LocalTime
import java.util.*

@Volatile
var shutdown = false


@Serializable
data class Protocole(
    val action: String,
    val message: String,
    val heure: String? = null
)

suspend fun writeur(socket: Socket) {
    val writer = PrintWriter(socket.getOutputStream(), true)
    val s = Scanner(System.`in`)
    while (!shutdown) {
        if (s.hasNextLine()) {
            var message = s.nextLine()
            if (message == "exit") {
                shutdown = true
                break
            }
            if( message in "pingPING"){
                writer.write(Json.encodeToString(Protocole(action = "PING", message = "PING", heure = LocalTime.now())))
            }
            writer.println(message)
        }
    }
}

suspend fun reader(socket: Socket) {
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
                println("\n[REÇU] : ,${objet.message}")
                print("> ") // retrour ellegant a la ligne
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

    // On lance les deux coroutines en parallèle
    val jobWriter = launch(Dispatchers.IO) {
        writeur(socket)
    }

    val jobReader = launch(Dispatchers.IO) {
        reader(socket)
    }

    // Au lieu du while(true) sleep, on attend que l'un des deux finisse
    // Si le writer s'arrête (exit) ou le reader (déconnexion), on ferme tout
    joinAll(jobWriter, jobReader)

    socket.close()
    println("Fin du programme.")
}