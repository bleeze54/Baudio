package org.example

import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

fun main() {

    val server = ServerSocket(9999)
    println("Serveur demaré")
    val clients = mutableListOf<Socket>()
    thread {

    }
    while (true) {

        val client = server.accept()
        clients.add(client)

        ClientServeurMessage(client).start()
    }
}