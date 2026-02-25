package tools

import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher
import kotlin.text.Charsets.UTF_8

fun encrypt(text: String,publickey: PublicKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")  // ou OAEP pour mieux
    val plaintext = text.toByteArray(UTF_8)
    cipher.init(Cipher.ENCRYPT_MODE, publickey)
    val encryptedBytes =cipher.doFinal(plaintext)
    return Base64.getEncoder().encodeToString(encryptedBytes)
}