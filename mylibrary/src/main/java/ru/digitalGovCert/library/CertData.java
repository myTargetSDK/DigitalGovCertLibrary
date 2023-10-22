package ru.digitalGovCert.library;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;

public class CertData
{
	public final @NonNull X509TrustManager x509TrustManager;
	public final @NonNull SSLContext sslContext;
	public final @NonNull TrustManagerFactory trustManagerFactory;

	public CertData(final @NonNull X509TrustManager x509TrustManager, final @NonNull SSLContext sslContext, final @NonNull TrustManagerFactory trustManagerFactory)
	{
		this.x509TrustManager = x509TrustManager;
		this.sslContext = sslContext;
		this.trustManagerFactory = trustManagerFactory;
	}
}
