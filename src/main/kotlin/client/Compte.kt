package client

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.toSeed
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.Cipher
import kotlin.text.Charsets.UTF_8

class Compte(seedphrase:String? = null) {
    val publicKey: RSAPublicKey
    val privateKey: RSAPrivateKey
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")  // ou OAEP pour mieux
    init {
        val mnemonic = if (seedphrase != null) {
            //importation de la phrase
            MnemonicCode(seedphrase)
        } else {
            // Sinon, on génère 24 mots aléatoires
            MnemonicCode(Mnemonics.WordCount.COUNT_24)
        }
        val seedPhrase: String = mnemonic.toList().joinToString(" ")
        println(seedPhrase)
        val seedBytes = mnemonic.toSeed()
        val rngSeed = seedBytes.copyOf(32)
        val deterministicRandom = SecureRandom(rngSeed)
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(4096, deterministicRandom)
        val keyPair: KeyPair = keyGen.generateKeyPair()
        this.publicKey = keyPair.public as RSAPublicKey
        this.privateKey = keyPair.private as RSAPrivateKey
    }

    fun encrypt(text: String,): String {
        val plaintext = "salut".toByteArray(UTF_8)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(plaintext).toString()
    }

    fun decrypt(text: String): String {
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decrypted = cipher.doFinal(text.toByteArray(UTF_8))
        return String(decrypted, UTF_8)
    }

    fun getkey(): String {
        val publicKeyString = Base64.getEncoder().encodeToString(publicKey.encoded)
        return publicKeyString
    }

    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}