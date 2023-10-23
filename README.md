# Сертификаты минцифры

#### Данное сдк позволяет програмно поддержать сертификаты от минцифры в andrpid-приложении.

## Пример использования 

#### Добавить нужные зависимости
```gradle
//project gradle
maven { url = "https://jitpack.io" }

//application gradle
implementation ("com.github.RinJavDev:DigitalGovCertLibrary:1.0.23")
```
#### Пример использования с OkHttp
```kotlin
    val certManager = CertManager()
    val certData = certManager.createCertData(this@MainActivity)!!
    val httpClient= OkHttpClient
    .Builder()
    .sslSocketFactory(certData.sslContext.socketFactory, certData.x509TrustManager)
    .build()
    //todo use OkHttpClient
```

#### Пример использования с HttpUrlConnection
```kotlin
    val certManager = CertManager()
    val certData = certManager.createCertData(context)
    HttpsURLConnection.setDefaultSSLSocketFactory(certData.sslContext.socketFactory)
    
    //todo use HttpConnection library
```

#### Пример использования с WebView
```kotlin
    val certManager = CertManager()
    val certData = certManager.createCertData(this)!!

    webView.webViewClient = object : WebViewClient()
    {
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError)
        {
            if (SslErrorVerifier.verifySslError(error, certData.trustManagerFactory))
                {
                    handler.proceed()
                } else
                {
                    handler.cancel()
                }
        }
    }
    
     webView.loadUrl("https://www.google.com/")
```

