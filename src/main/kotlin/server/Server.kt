package server

import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket

class Server(val port: Int=9999, val public: Boolean=true,clearpassword: String?=null) {
    private var clients: MutableSet<Socket>
    val encryptpassword :String?
    init {
        this.clients= mutableSetOf()
        this.encryptpassword=  if (clearpassword != null) {
            md5Hash(clearpassword)
        }else{
            null
        }
    }


    fun start() {
        val server = ServerSocket(this.port)
        println("serveur démarré")

        val serverScope = CoroutineScope(Dispatchers.IO)

        try {
            while (true) {
                val client = server.accept()
                this.clients.add(client)
                println("Nouvelle connexion de ${client.inetAddress.hostAddress}")

                // Lance le handler
                serverScope.launch {
                    Clientservermessage(client,encryptpassword, this@Server).handle()
                }

            }
        } catch (e: CancellationException) {
            // arrêt demandé
        } finally {
            server.close()
            serverScope.cancel()
            println("server arrêté")
        }
    }
    fun getclients(): Set<Socket> = return this.clients

}