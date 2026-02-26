package client

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tools.Protocole
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

@Volatile
var shutdown = false

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
            print("zadascadadzds<x")
            val reader: BufferedReader = socket.getInputStream().bufferedReader()
            val writer = PrintWriter(socket.getOutputStream(), true)
            var message = reader.readLine()
            if (message == null) {
                println("Serveur déconnecté.")
                shutdown = true
            }
            var objet = Json.decodeFromString<Protocole>(message)
            if (objet.action == "REQUIREMENT" && objet.message == "PSEUDO"){
                writer.println(Json.encodeToString(Protocole(action = "PSEUDO", message = compte.pseudo)))
                print("ça passe bien")
            }

            message = reader.readLine()
            if (message == null) {
                println("Serveur déconnecté.")
                shutdown = true
            }
            objet = Json.decodeFromString<Protocole>(message)
            if (objet.action == "REQUIREMENT" && objet.message == "KEY") {
                writer.println(Json.encodeToString(Protocole(action = "USERS", message = compte.getkey())))
                val message = reader.readLine()
                if (message == null) {
                    println("Serveur déconnecté.")
                    shutdown = true
                }
                val objet = Json.decodeFromString<Protocole>(message)
                if(objet.action != "keyTest") {
                    return@coroutineScope false
                }
                writer.println(Json.encodeToString(Protocole(action = "keyTest", message = compte.decrypt(objet.message))))
            }
            while (!shutdown) {
                val message = reader.readLine()
                if (message == null) {
                    println("Serveur déconnecté.")
                    shutdown = true
                    break
                }
                if (objet.action == "EXIT") {
                    println("Serveur déconnecté.")
                    shutdown = true
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
    val s= Scanner(System.`in`)
    val pseudo = s.nextLine().trim()
    val compte : Compte = Compte(pseudo=pseudo)

    val socket = try {
        Socket("localhost", 9999)
    } catch (e: Exception) {
        println("Impossible de se connecter au serveur.")
        return@runBlocking
    }
    val jobReader = launch(Dispatchers.IO) {
        reader(socket,compte)
    }
    val jobWriter = launch(Dispatchers.IO) {
        writeur(socket,compte)
    }

    joinAll(jobWriter, jobReader)
    socket.close()
    println("Fin du programme.")
}