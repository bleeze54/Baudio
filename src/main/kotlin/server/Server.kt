package server

import kotlinx.coroutines.*
import tools.md5Hash
import java.net.ServerSocket
import java.net.Socket

class Server(val port: Int=9999, val public: Boolean=true,clearpassword: String?=null) {
    private val clientsconnect: MutableMap<Socket,String>
    val encryptpassword :String?
    init {
        this.clientsconnect= mutableMapOf()
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
                clientsconnect[client]="anonyme"
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
    fun getclients(): List<String> = this.clientsconnect.values.toList()

    fun setclient(socket: Socket,publickey:String) {
        this.clientsconnect[socket] = publickey
    }

    fun deleteclient(socket: Socket) {
        this.clientsconnect.remove(socket)
    }

}