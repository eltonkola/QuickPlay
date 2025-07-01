package com.eltonkola.quickplay.data

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import java.io.File
import java.math.BigInteger
import java.security.*
import java.security.cert.X509Certificate
import java.util.*

object SelfSignedKeystore {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun createKeystore(
        file: File,
        password: String = "changeit",
        alias: String = "alias",
        dn: String = "CN=localhost"
    ): File {
        if (file.exists()) return file

        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        val startDate = Date()
        val endDate = Date(startDate.time + 365L * 24 * 60 * 60 * 1000) // valid for 1 year

        val certBuilder = JcaX509v3CertificateBuilder(
            org.bouncycastle.asn1.x500.X500Name(dn),
            BigInteger.valueOf(System.currentTimeMillis()),
            startDate,
            endDate,
            org.bouncycastle.asn1.x500.X500Name(dn),
            keyPair.public
        )

        val signer = JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.private)
        val cert: X509Certificate = JcaX509CertificateConverter()
            .setProvider(BouncyCastleProvider())
            .getCertificate(certBuilder.build(signer))

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        keyStore.setKeyEntry(alias, keyPair.private, password.toCharArray(), arrayOf(cert))

        file.outputStream().use {
            keyStore.store(it, password.toCharArray())
        }

        return file
    }
}
