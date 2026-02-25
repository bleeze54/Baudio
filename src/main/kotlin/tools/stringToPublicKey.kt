package tools

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

fun stringToPublicKey(publicKeyString: String): PublicKey? {
    try {
        val publicBytes = Base64.getDecoder().decode(publicKeyString)

        val keySpec = X509EncodedKeySpec(publicBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }catch (e:Exception){
        println(e)
        return null
    }
}