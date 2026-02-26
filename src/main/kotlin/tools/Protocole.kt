package tools

import kotlinx.serialization.Serializable

@Serializable
data class Protocole(
    val action: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)