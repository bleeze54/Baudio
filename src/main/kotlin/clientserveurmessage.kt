package org.example

import java.net.Socket

class ClientServeurMessage(private val client: Socket) : Thread() {

    override fun run() {
        try {
            val reader = client.getInputStream().bufferedReader()

            while (true) {
                val message = reader.readLine()

                if (message == null) {
                    println("Client déconnecté")
                    break
                }

                println("Reçu : $message")
            }

        } catch (e: Exception) {
            println("Connexion perdue : ${e.message}")
        } finally {
            client.close()
        }
    }
}