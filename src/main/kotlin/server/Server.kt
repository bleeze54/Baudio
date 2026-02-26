package server

import kotlinx.coroutines.*
import tools.md5Hash
import java.net.ServerSocket
import java.net.Socket

class Server(val port: Int=9999, val public: Boolean=true,clearpassword: String?=null) {
    private val clientsconnect: MutableMap<Socket,String> // contient un pseudo et un socket
    private val account: MutableMap<String,String> //contient un pseudo et une clé publique
    val encryptpassword :String?
    init {
        this.clientsconnect= mutableMapOf()
        this.account= mutableMapOf()
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
    private val lockclientsonnect = Any()
    fun setclient(socket: Socket,pseudo:String) {

        synchronized(lockclientsonnect) {
            this.clientsconnect[socket] = pseudo
        }
    }

    fun deleteclient(socket: Socket) {
        synchronized(lockclientsonnect) {
            this.clientsconnect.remove(socket)
        }

    }
    private val lockaccount = Any()
    fun setaccount(pseudo:String,publickey:String) {

        synchronized(lockaccount) {
            this.account.set(key=pseudo,publickey)
        }
    }

    fun deleteaccount(pseudo:String,publickey:String) {
        synchronized(lockaccount) {
            this.account.remove(pseudo)
        }

    }

}