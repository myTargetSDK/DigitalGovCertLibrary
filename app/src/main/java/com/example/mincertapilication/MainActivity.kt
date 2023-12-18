package com.example.mincertapilication

import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.mincertapilication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import io.github.mytargetsdk.CertManager
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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoad.setOnClickListener {
            if (!URLUtil.isValidUrl(URL))
            {
                Toast.makeText(this, "You need to redefine the URL variable", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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
        intent.putExtra(WebViewActivity.URL_EXTRA, URL)
        intent.putExtra(WebViewActivity.TO_USE_CERT, binding.swMinCert.isChecked)

        startActivity(intent)
    }

    private fun loadByOkHttp()
    {
        changeLoadingViewState(true)

        lifecycleScope.launch(Dispatchers.IO) {
            val request: Request = Request.Builder()
                .url(URL)
                .build()

            val httpClient: OkHttpClient = if (binding.swMinCert.isChecked)
            {
                val certManager = CertManager()
                val certData = certManager.createCertData(this@MainActivity)!!
                OkHttpClient
                    .Builder()
                    .sslSocketFactory(certData.sslContext.socketFactory, certData.x509TrustManager)
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
        }
    }

    private fun loadByHttpUrlConnection()
    {
        changeLoadingViewState(true)

        lifecycleScope.launch(Dispatchers.IO) {
            val certManager = CertManager()
            val certData = certManager.createCertData(this@MainActivity)!!

            if (binding.swMinCert.isChecked)
            {
                HttpsURLConnection.setDefaultSSLSocketFactory(certData.sslContext.socketFactory)
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
        }
    }

    private fun processResult(resultCode: Int, result: String)
    {
        lifecycleScope.launch(Dispatchers.Main) {
            changeLoadingViewState(false)
            binding.apply {
                btnLoad.isEnabled = true
                tvResultCode.text = "resultCode: $resultCode"
                tvResult.text = result
            }
        }
    }

    private fun changeLoadingViewState(isLoading: Boolean)
    {
        binding.apply {
            btnLoad.isEnabled = !isLoading
            progressBar.isVisible = isLoading
            if (isLoading)
            {
                tvResultCode.text = ""
                tvResult.text = ""
            }
        }
    }
}

//TODO You need to redefine the variable
const val URL = "use your url"

