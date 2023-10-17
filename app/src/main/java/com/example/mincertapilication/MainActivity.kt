package com.example.mincertapilication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mincertapilication.databinding.ActivityMainBinding
import com.mincert.library.MinCertUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

class MainActivity : AppCompatActivity()
{
    private val URL = "https://mincertad.mail.ru/9525"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoad.setOnClickListener {

            when (binding.radioGroup.checkedRadioButtonId)
            {
                R.id.rbHttpUrlConnection ->
                {
                    loadByHttpUrlConnection()
                }

                R.id.rbOkHttp ->
                {
                    loadByOkHttp()
                }

                R.id.rbWebView ->
                {
                    loadByWebView()
                }
            }
        }
    }

    private fun loadByWebView()
    {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("URL", URL)
        intent.putExtra("isUsingMinCert", binding.swMinCert.isChecked)

        startActivity(intent)
    }

    private fun loadByOkHttp()
    {
        Thread {
            val request: Request = Request.Builder()
                .url(URL)
                .build()

            val httpClient: OkHttpClient = if (binding.swMinCert.isChecked)
            {
                val minCertUtils = MinCertUtils(this)
                minCertUtils.init()
                OkHttpClient
                    .Builder()
                    .sslSocketFactory(minCertUtils.sslContext!!.socketFactory, minCertUtils.x509TrustManager!!)
                    .build()
            } else
            {
                OkHttpClient()
            }

            try
            {
                httpClient.newCall(request).execute().use { response ->
                    processResult(response.code, response.body!!.string())
                }
            } catch (e: IOException)
            {
                processResult(-1, e.message ?: "undefined error")
            }
        }.start()
    }

    private fun loadByHttpUrlConnection()
    {
        Thread {
            val minCertUtils = MinCertUtils(this)
            minCertUtils.init()

            if (binding.swMinCert.isChecked)
            {
                HttpsURLConnection.setDefaultSSLSocketFactory(minCertUtils.sslContext!!.socketFactory)
            } else
            {
                HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
            }

            try
            {
                val url = URL(URL)
                val urlConn = url.openConnection() as HttpURLConnection
                urlConn.setRequestProperty("Content-Type", "applicaiton/json; charset=utf-8")
                urlConn.setRequestProperty("Accept", "applicaiton/json")
                urlConn.doOutput = true
                urlConn.connect()

                val writer = BufferedWriter(OutputStreamWriter(urlConn.outputStream))
                writer.flush()
                writer.close()

                val inputStream = if (urlConn.responseCode == HttpURLConnection.HTTP_OK)
                {
                    urlConn.inputStream
                } else
                {
                    urlConn.errorStream
                }

                val reader = BufferedReader(
                    InputStreamReader(inputStream, "UTF-8"), 8
                )
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null)
                {
                    sb.append(
                        """
                    $line
                    
                    """.trimIndent()
                    )
                }
                inputStream?.close()

                val response = sb.toString()

                processResult(urlConn.responseCode, response)

            } catch (throwable: Throwable)
            {
                processResult(-1, throwable.message ?: "undefined error")
                throwable.printStackTrace()
            }
        }.start()
    }

    private fun processResult(resultCode: Int, result: String)
    {
        runOnUiThread {
            binding.tvResultCode.text = "resultCode: $resultCode"
            binding.tvResult.text = result

        }
    }
}



