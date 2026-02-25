package server

import java.net.Socket

data class ClientSession(
    val id: String,
    val socket: Socket
)