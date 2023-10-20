package com.mincert.library;

import android.content.Context;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CertManager
{
	private final @NonNull List<String> rawCertNames;

	public CertManager()
	{
		rawCertNames = new ArrayList<>();
		rawCertNames.add("root");
		rawCertNames.add("sub");
	}

	public @NonNull CertData makeCertData(final @Nullable Context context) throws Throwable
	{
		if (context == null)
		{
			throw new Exception("Error make certData – context is null");
		}

		final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		if (certificateFactory == null)
		{
			throw new Exception("Error make certData – certificate factory is null");
		}

		final CertLoader certLoader = new CertLoader(context, certificateFactory);
		final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);

		for (String rawCertName : rawCertNames)
		{
			final Certificate rawCert = certLoader.getRawCert(rawCertName);
			if (rawCert != null)
			{
				keyStore.setCertificateEntry(rawCertName, rawCert);
			}
		}

		final ArrayList<X509Certificate> systemCerts = certLoader.getSystemCerts();
		for (X509Certificate certificate : systemCerts)
		{
			keyStore.setCertificateEntry(certificate.getIssuerDN().getName(), certificate);
		}

		final TrustManagerFactory trustManagerFactory = getTrustManagerFactory(keyStore);

		final X509TrustManager x509TrustManager = getX509TrustManager(trustManagerFactory);
		if (x509TrustManager == null)
		{
			throw new Exception("Error during TrustManagerFactory initialization " + "can't initialize x509TrustManager");
		}

		final SSLContext sslContext = getSslContext(trustManagerFactory);

		return new CertData(x509TrustManager, sslContext, trustManagerFactory);
	}

	@NonNull TrustManagerFactory getTrustManagerFactory(final @NonNull KeyStore keyStore) throws Exception
	{
		final String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
		if (trustManagerFactory == null)
		{
			throw new Exception("Error during TrustManagerFactory initialization " + "can't initialize trust manager factory");
		}
		trustManagerFactory.init(keyStore);
		return trustManagerFactory;
	}

	private @Nullable X509TrustManager getX509TrustManager(final @NonNull TrustManagerFactory trustManagerFactory)
	{

		for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
		{
			if (trustManager instanceof X509TrustManager)
			{
				return (X509TrustManager) trustManager;
			}
		}
		return null;
	}


	private @NonNull SSLContext getSslContext(final @NonNull TrustManagerFactory trustManagerFactory) throws Throwable
	{
		final SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
		return sslContext;
	}
}

