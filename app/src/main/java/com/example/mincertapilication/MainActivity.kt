package com.example.mincertapilication

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mincertapilication.SslUtils.getSslContextForCertificateFile
import com.example.mincertapilication.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.TrustManagerFactory

private val ROOT_CERTIFICATE = "root"
private val SUB_CERTIFICATE = "sub"

class MainActivity : AppCompatActivity()
{

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel



    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]


        binding.btnLoad.setOnClickListener {
            //viewModel.getJson("https://mincertad.mail.ru/9525", this)
            Thread {

                var inputStream: InputStream? = null
                val sslContext = getSslContextForCertificateFile(this@MainActivity, "russian_trusted_root_ca")
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)

                try
                {
                    val _url = URL("https://mincertad.mail.ru/9525")
                    val urlConn = _url.openConnection() as HttpURLConnection
                    urlConn.setRequestProperty("Content-Type", "applicaiton/json; charset=utf-8")
                    urlConn.setRequestProperty("Accept", "applicaiton/json")
                    urlConn.doOutput = true
                    urlConn.connect()
                    val writer = BufferedWriter(OutputStreamWriter(urlConn.outputStream))
                    writer.flush()
                    writer.close()
                    inputStream = if (urlConn.responseCode == HttpURLConnection.HTTP_OK)
                    {
                        urlConn.inputStream
                    } else
                    {
                        urlConn.errorStream
                    }
                } catch (thrwobale: Throwable)
                {
                    thrwobale.printStackTrace()
                }

                var response: String? = null
                try
                {
                    val reader = BufferedReader(
                        InputStreamReader(inputStream, "UTF-8"), 8
                    )
                    val sb = StringBuilder()
                    var line: String? = null
                    while (reader.readLine().also { line = it } != null)
                    {
                        sb.append(
                            """
                    $line
                    
                    """.trimIndent()
                        )
                    }
                    inputStream?.close()
                    response = sb.toString()
                    Log.e("JSON", response)
                } catch (e: Exception)
                {
                    Log.e("Buffer Error", "Error converting result $e")
                }
            }.start()
        }
    }

    private fun readPemCert(certName: String): InputStream?
    {
        return fromPem(getPemAsString(certName))
    }

    private fun getPemAsString(certName: String): String
    {
        val ins = resources.openRawResource(
            resources.getIdentifier(
                certName, "raw", packageName
            )
        )
        val textBuilder = java.lang.StringBuilder()
        try
        {
            BufferedReader(InputStreamReader(ins, Charset.forName(StandardCharsets.UTF_8.name()))).use { reader ->
                var c = 0
                while (reader.read().also { c = it } != -1)
                {
                    textBuilder.append(c.toChar())
                }
            }
        } catch (e: IOException)
        {
            Log.d("WEB_VIEW_EXAMPLE", "read pem error")
        }
        return textBuilder.toString()
    }

    @Throws(java.lang.Exception::class)
    private fun initTrustStore(): TrustManagerFactory?
    {
        return try
        {
            val cf = CertificateFactory.getInstance("X.509")
            val subIns = readPemCert(SUB_CERTIFICATE)
            val sub = cf.generateCertificate(subIns)
            val rootIns = readPemCert(ROOT_CERTIFICATE)
            val root = cf.generateCertificate(rootIns)
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry(SUB_CERTIFICATE, sub)
            keyStore.setCertificateEntry(ROOT_CERTIFICATE, root)
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)
            tmf
        } catch (e: java.lang.Exception)
        {
            throw java.lang.Exception("Error during TrustManagerFactory initialization")
        }
    }

    private fun fromPem(pem: String): InputStream?
    {
        val base64cert = pemKeyContent(pem)
        return fromBase64String(base64cert)
    }

    private fun fromBase64String(base64cert: String): InputStream?
    {
        val decoded = Base64.decode(base64cert, Base64.NO_WRAP)
        return ByteArrayInputStream(decoded)
    }

    private fun pemKeyContent(pem: String): String
    {
        return pem.replace("\\s+", "")
            .replace("\n", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
    }

}


