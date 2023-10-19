package com.mincert.library;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MinCertUtils
{
	private final static String ROOT_CERTIFICATE = "root";
	private final static String SUB_CERTIFICATE = "sub";

	private final @NonNull Context context;
	@Nullable X509TrustManager x509TrustManager;
	@Nullable SSLContext sslContext;
	@Nullable TrustManagerFactory trustManagerFactory;

	public MinCertUtils(final @NonNull Context context)
	{
		this.context = context;
	}

	public @Nullable TrustManagerFactory getTrustManagerFactory()
	{
		return trustManagerFactory;
	}

	public @Nullable X509TrustManager getX509TrustManager()
	{
		return x509TrustManager;
	}

	public @Nullable SSLContext getSslContext()
	{
		return sslContext;
	}

	public void init() throws Throwable
	{
		try
		{
			final CertificateFactory cf = CertificateFactory.getInstance("X.509");

			final InputStream subIns = readPemCert(SUB_CERTIFICATE);
			final Certificate sub = cf.generateCertificate(subIns);

			final InputStream rootIns = readPemCert(ROOT_CERTIFICATE);
			final Certificate root = cf.generateCertificate(rootIns);

			final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

			keyStore.load(null, null);
			keyStore.setCertificateEntry(SUB_CERTIFICATE, sub);
			keyStore.setCertificateEntry(ROOT_CERTIFICATE, root);

			final ArrayList<X509Certificate> certificates = getSystemCerts();
			for (X509Certificate certificate : certificates)
			{
				keyStore.setCertificateEntry(certificate.getIssuerDN().getName(), certificate);
			}

			final String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);

			tmf.init(keyStore);
			initX509TrustManager(tmf);
			initSslContext(tmf);
			this.trustManagerFactory = tmf;
		}
		catch (Throwable e)
		{
			throw new Exception("Error during TrustManagerFactory initialization " + e.getMessage());
		}
	}

	private @NonNull ArrayList<X509Certificate> getSystemCerts()
	{
		final ArrayList<X509Certificate> certificates = new ArrayList<>();
		try
		{
			final KeyStore ks = KeyStore.getInstance("AndroidCAStore");

			if (ks != null)
			{
				ks.load(null, null);
				final Enumeration<String> aliases = ks.aliases();

				while (aliases.hasMoreElements())
				{
					try
					{

						String alias = aliases.nextElement();

						X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

						certificates.add(cert);
					}
					catch (Throwable throwable)
					{
						throwable.printStackTrace();
					}
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return certificates;
	}

	private void initX509TrustManager(TrustManagerFactory trustManagerFactory)
	{

		for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
		{
			if (trustManager instanceof X509TrustManager)
			{
				x509TrustManager = (X509TrustManager) trustManager;
			}
		}
	}

	private InputStream readPemCert(final @NonNull String certName)
	{
		return fromPem(getPemAsString(certName));
	}

	private String getPemAsString(final @NonNull String certName)
	{
		final InputStream ins = context.getResources().openRawResource(
				context.getResources().getIdentifier(
						certName, "raw", context.getPackageName()));
		StringBuilder textBuilder = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader
														(ins, Charset.forName(StandardCharsets.UTF_8.name()))))
		{
			int c = 0;
			while ((c = reader.read()) != -1)
			{
				textBuilder.append((char) c);
			}
		}
		catch (IOException e)
		{
			Log.d("WEB_VIEW_EXAMPLE", "read pem error");
		}
		return textBuilder.toString();
	}

	private void initSslContext(final @NonNull TrustManagerFactory trustManagerFactory) throws Throwable
	{
		sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
	}

	private @NonNull InputStream fromPem(final @NonNull String pem)
	{
		String base64cert = pemKeyContent(pem);
		return fromBase64String(base64cert);
	}

	private @NonNull InputStream fromBase64String(final @NonNull String base64cert)
	{
		byte[] decoded = Base64.decode(base64cert, Base64.NO_WRAP);
		return new ByteArrayInputStream(decoded);
	}

	private @NonNull String pemKeyContent(final @NonNull String pem)
	{
		return pem.replace("\\s+", "")
				  .replace("\n", "")
				  .replace("-----BEGIN PUBLIC KEY-----", "")
				  .replace("-----END PUBLIC KEY-----", "")
				  .replace("-----BEGIN CERTIFICATE-----", "")
				  .replace("-----END CERTIFICATE-----", "");
	}
}

