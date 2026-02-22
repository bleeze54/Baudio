package org.example
import java.io.PrintWriter
import java.net.Socket
import java.util.Scanner

fun main() {
    val socket = Socket("localhost", 9999)

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
    }

    socket.close()
}