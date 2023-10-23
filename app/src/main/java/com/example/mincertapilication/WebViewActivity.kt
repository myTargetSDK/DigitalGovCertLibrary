package com.example.mincertapilication

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mincertapilication.databinding.ActivityWebViewBinding
import ru.digitalGovCert.library.CertManager
import ru.digitalGovCert.library.SslErrorVerifier

class WebViewActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityWebViewBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val webView = binding.webView
        if (intent.getBooleanExtra(TO_USE_CERT, false))
        {
            val certManager = CertManager()
            val certData = certManager.createCertData(this)!!

            webView.webViewClient = object : WebViewClient()
            {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError)
                {
                    if (SslErrorVerifier.verifySslError(error, certData.trustManagerFactory))
                    {
                        handler.proceed()
                        Toast.makeText(this@WebViewActivity, "ssl error proceed", Toast.LENGTH_LONG).show()

                    } else
                    {
                        handler.cancel()
                        Toast.makeText(this@WebViewActivity, "ssl error happened", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else
        {
            webView.webViewClient = WebViewClient()
        }
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(intent.getStringExtra(URL_EXTRA) ?: "")

    }

    companion object
    {
        const val URL_EXTRA = "URL_EXTRA"
        const val TO_USE_CERT = "TO_USE_CERT"
    }

}