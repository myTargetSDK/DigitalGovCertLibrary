package ru.digitalGovCert.library;

import android.net.http.SslCertificate;
import android.net.http.SslError;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;

public class SslErrorVerifier
{
	public static boolean verifySslError(final @NonNull SslError error, final @NonNull TrustManagerFactory trustManagerFactory)
	{
		if (error.getPrimaryError() == SslError.SSL_UNTRUSTED)
		{
			final SslCertificate cert = error.getCertificate();
			try
			{
				final Field f = cert.getClass().getDeclaredField("mX509Certificate");
				f.setAccessible(true);
				final X509Certificate x509 = (X509Certificate) f.get(cert);
				final X509Certificate[] chain = new X509Certificate[]{x509};
				for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
				{
					if (trustManager instanceof X509TrustManager)
					{
						final X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
						try
						{
							x509TrustManager.checkServerTrusted(chain, "generic");
							return true;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				return false;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
