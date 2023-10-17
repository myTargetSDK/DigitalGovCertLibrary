package com.example.mincertapilication;

import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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