package ru.digitalGovCert.library;

import android.content.Context;
import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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


/**
 * Сlass for working with SSL certificates that support Ministry of Digital Development certificates
 */
public final class CertManager
{
	private final static String TAG = "CertManager";
	private final @NonNull List<String> rawCertNames;

	public CertManager()
	{
		rawCertNames = new ArrayList<>();
		rawCertNames.add("russian_trusted_root_ca");
		rawCertNames.add("russian_trusted_sub_ca");
	}

	/**
	 * createCert and return {@link CertData}.
	 *
	 * @param context {@link Context}.
	 */
	public final @Nullable CertData createCertData(final @Nullable Context context)
	{
		if (context == null)
		{
			Log.d(TAG, "Error make certData – context is null");
			return null;
		}

		final CertificateFactory certificateFactory = createCertificateFactory();
		if (certificateFactory == null)
		{
			Log.d(TAG, "Error make certData – certificateFactory is null");
			return null;
		}

		final KeyStore keyStore = createKeyStore();
		if (keyStore == null)
		{
			Log.d(TAG, "Error make certData – keyStore is null");
			return null;
		}

		final CertLoader certLoader = new CertLoader(context, certificateFactory);
		for (String rawCertName : rawCertNames)
		{
			final Certificate rawCert = certLoader.getRawCert(rawCertName);
			if (rawCert != null)
			{
				try
				{
					keyStore.setCertificateEntry(rawCertName, rawCert);
				}
				catch (KeyStoreException e)
				{
					Log.e(TAG, "", e);
				}
			}
		}

		final ArrayList<X509Certificate> systemCerts = certLoader.getSystemCerts();
		for (X509Certificate certificate : systemCerts)
		{
			try
			{
				keyStore.setCertificateEntry(certificate.getIssuerDN().getName(), certificate);
			}
			catch (KeyStoreException e)
			{
				Log.e(TAG, "", e);
			}
		}

		final TrustManagerFactory trustManagerFactory = createTrustManagerFactory(keyStore);
		if (trustManagerFactory == null)
		{
			Log.d(TAG, "Error make certData – trustManagerFactory is null");
			return null;
		}

		final X509TrustManager x509TrustManager = findX509TrustManager(trustManagerFactory);
		if (x509TrustManager == null)
		{
			Log.d(TAG, "Error make certData – x509TrustManager is null");
			return null;
		}

		final SSLContext sslContext = createSslContext(trustManagerFactory);
		if (sslContext == null)
		{
			Log.d(TAG, "Error make certData – sslContext is null");
			return null;
		}
		return new CertData(x509TrustManager, sslContext, trustManagerFactory);
	}

	private final @Nullable CertificateFactory createCertificateFactory()
	{
		try
		{
			return CertificateFactory.getInstance("X.509");
		}
		catch (CertificateException e)
		{
			Log.e(TAG, "", e);
			return null;
		}
	}

	private final @Nullable KeyStore createKeyStore()
	{
		try
		{
			final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			return keyStore;
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);

			return null;
		}
	}

	private final @Nullable TrustManagerFactory createTrustManagerFactory(final @NonNull KeyStore keyStore)
	{
		final String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		try
		{
			final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
			trustManagerFactory.init(keyStore);
			return trustManagerFactory;
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);
			return null;
		}
	}

	private final @Nullable X509TrustManager findX509TrustManager(final @NonNull TrustManagerFactory trustManagerFactory)
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


	private final @Nullable SSLContext createSslContext(final @NonNull TrustManagerFactory trustManagerFactory)
	{
		try
		{
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
			return sslContext;
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);

			return null;
		}
	}
}

