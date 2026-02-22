package org.example
import Clientserveurmessage
import jdk.internal.joptsimple.internal.Messages.message
import java.io.PrintWriter
import java.net.Socket
import java.util.Scanner
import kotlinx.coroutines.*
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.lang.Thread.sleep

@Volatile
var shutdown = false

suspend fun writeur(socket: Socket) {
    print("yo")
    val writer = PrintWriter(socket.getOutputStream(), true)
    var message :String? = "Hello World"
    writer.println(message)
    val s: Scanner = Scanner(System.`in`)
    while (true) {
        message = s.nextLine()
        if(message=="exit"){
            break
        }else{
            writer.println(message)
        }
    }}

suspend fun reader(socket: Socket) {
    print("yes")
    try {
        val reader: BufferedReader = socket.getInputStream().bufferedReader()
        while (true) {
            if (shutdown) break
            var message = reader.readLine() ?: continue
            println(message)
        }
    }finally {
        shutdown = true
    }


}
fun main(){
    val socket = Socket("localhost", 9999)
    val Scope = CoroutineScope(Dispatchers.IO)
    Scope.launch {
        writeur(socket)
    }
    Scope.launch {
        reader(socket)
    }

while (true) {
    sleep(1000)
}
}
