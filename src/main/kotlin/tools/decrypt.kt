package tools

import java.security.PrivateKey
import java.util.*
import javax.crypto.Cipher
import kotlin.text.Charsets.UTF_8

fun decrypt(encryptedtext: String,privatekey: PrivateKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privatekey)
    val bytesToDecrypt = Base64.getDecoder().decode(encryptedtext)
    val decryptedBytes = cipher.doFinal(bytesToDecrypt)
    return String(decryptedBytes, UTF_8)
}