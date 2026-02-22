package org.example

import java.net.ServerSocket

fun main() {
    val server = ServerSocket(9999)
    println("Serveur en attente...")

    val client = server.accept()
    println("Client connecté")

    val reader = client.getInputStream().bufferedReader()
    println(reader.readLine())

    client.close()
    server.close()
}