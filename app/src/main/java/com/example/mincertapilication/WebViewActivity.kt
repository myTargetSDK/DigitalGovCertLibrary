package com.example.mincertapilication

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.mincertapilication.databinding.ActivityWebViewBinding
import com.mincert.library.MinCertUtils

class WebViewActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.getBooleanExtra("isUsingMinCert", false))
        {
            val minCertUtils = MinCertUtils(this)
            minCertUtils.init()
            binding.root.webViewClient = CheckServerTrustedWebViewClient(minCertUtils.trustManagerFactory)
        } else
        {
            binding.root.webViewClient = WebViewClient()
        }

        binding.root.loadUrl(intent.getStringExtra("URL") ?: "")

    }
}