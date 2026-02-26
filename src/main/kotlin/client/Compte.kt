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

//contien la logique crypographique de l'utilisateur
class Compte(seedphrase:String? = null ,val pseudo:String) {

    private val publicKey: RSAPublicKey
    private val privateKey: RSAPrivateKey
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

    fun getkey(): String {
        val publicKeyString = Base64.getEncoder().encodeToString(publicKey.encoded)
        return publicKeyString
    }
    fun decrypt(text: String): String {
        return tools.decrypt(text,privateKey)
    }
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}