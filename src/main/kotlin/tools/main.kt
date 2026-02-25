package tools

import client.Compte
import java.security.PublicKey

fun main(){
    val text="azddze"
    val compte : Compte = Compte()
    val publickey = stringToPublicKey(compte.getkey())
    if (publickey is PublicKey ) {
        val encrypted = encrypt(text, publickey)
        println(compte.decrypt(encrypted))
    }

}