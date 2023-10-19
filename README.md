# Сертификаты минцифры

#### Данное сдк позволяет програмно поддержать сертификаты от минцифры в andrpid-приложении.

### Пример использования 

#### Добавить нужные зависимости
```gradle
//project gradle
maven { url = "https://jitpack.io" }

//application gradle
implementation ("com.github.RinJavDev:MinCertSdk:1.0.19")
```
#### Пример использования с OkHttp
```kotlin
    val minCertUtils = MinCertUtils(context)
    minCertUtils.init()
    
    OkHttpClient.Builder()
    .sslSocketFactory(minCertUtils.sslContext!!.socketFactory, minCertUtils.x509TrustManager!!)
    .build()
    //todo use OkHttpClient
```

#### Пример использования с HttpUrlConnection
```kotlin
    val minCertUtils = MinCertUtils(context)
    minCertUtils.init()
     
    HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketFactory.getDefault() as SSLSocketFactory)
    //todo use HttpConnection library
```

#### Пример использования с WebView
```kotlin
    //Переопределяем WebViewClient
    class CheckServerTrustedWebViewClient extends WebViewClient
    {
	private final TrustManagerFactory tmf;

	CheckServerTrustedWebViewClient(TrustManagerFactory tmf)
	{
		this.tmf = tmf;
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
	{
		Log.d("WEB_VIEW_EXAMPLE", "onReceivedSslError");
		boolean passVerify = false;
		if (error.getPrimaryError() == SslError.SSL_UNTRUSTED)
		{
			SslCertificate cert = error.getCertificate();
			String subjectDn = cert.getIssuedTo().getDName();
			Log.d("WEB_VIEW_EXAMPLE", "subjectDN: " + subjectDn);
			try
			{
				Field f = cert.getClass().getDeclaredField("mX509Certificate");
				f.setAccessible(true);
				X509Certificate x509 = (X509Certificate) f.get(cert);
				X509Certificate[] chain = new X509Certificate[]{x509};
				for (TrustManager trustManager : tmf.getTrustManagers())
				{
					if (trustManager instanceof X509TrustManager)
					{
						X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
						try
						{
							x509TrustManager.checkServerTrusted(chain, "generic");
							passVerify = true;
							break;
						}
						catch (Exception e)
						{
							Log.e("WEB_VIEW_EXAMPLE", "verify trustManager failed" + e);
							passVerify = false;
						}
					}
				}
				Log.d("WEB_VIEW_EXAMPLE", "passVerify: " + passVerify);
			}
			catch (Exception e)
			{
				Log.e("WEB_VIEW_EXAMPLE", "verify cert fail" + e);
			}
		}
		if (passVerify)
		{
			handler.proceed();
		}
		else
		{
			handler.cancel();
		}
	}
}

...

 val minCertUtils = MinCertUtils(context)
 minCertUtils.init()
            
 webView.webViewClient = CheckServerTrustedWebViewClient(minCertUtils.trustManagerFactory)
 webView.loadUrl("https://www.google.com/")
```

