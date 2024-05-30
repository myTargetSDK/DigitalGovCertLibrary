package io.github.mytargetsdk;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

final class CertLoader
{
	private final @NonNull Context context;
	private final @NonNull CertificateFactory certificateFactory;
	private final static String TAG = "CertLoader";

	public CertLoader(final @NonNull Context context, final @NonNull CertificateFactory certificateFactory)
	{
		this.context = context;
		this.certificateFactory = certificateFactory;
	}

	final @Nullable Certificate getRawCert(final @RawRes int certResId)
	{
		try
		{
			final InputStream subIns = readPemCert(certResId);
			if (subIns != null)
			{
				return certificateFactory.generateCertificate(subIns);
			}
		}
		catch (CertificateException e)
		{
			Log.e(TAG, "", e);
		}

		return null;
	}

	final @NonNull ArrayList<X509Certificate> getSystemCerts()
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

						final String alias = aliases.nextElement();
						final X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

						if (cert != null)
						{
							certificates.add(cert);
						}
					}
					catch (Throwable throwable)
					{
						Log.e(TAG, "", throwable);

					}
				}
			}
		}
		catch (Throwable e)
		{
			Log.e(TAG, "", e);
		}
		return certificates;
	}

	private final @Nullable InputStream readPemCert(final @RawRes int certResId)
	{
		try
		{
			String pem = getPemAsString(certResId);

			if (TextUtils.isEmpty(pem))
			{
				return null;
			}

			return fromPem(pem);
		}
		catch (Exception e)
		{
			Log.e(TAG, "unexpected error, certResId=" + certResId, e);
			return null;
		}
	}

	private final @Nullable String getPemAsString(final @RawRes int certResId)
	{
		final InputStream ins;

		try
		{
			ins = context.getResources().openRawResource(certResId);
		}
		catch (Resources.NotFoundException e)
		{
			Log.e(TAG, "resource not found, certResId=" + certResId, e);
			return null;
		}

		StringBuilder textBuilder = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader(ins, StandardCharsets.UTF_8)))
		{
			int c = 0;
			while ((c = reader.read()) != -1)
			{
				textBuilder.append((char) c);
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, "", e);
		}

		return textBuilder.toString();
	}

	private @NonNull InputStream fromPem(final @NonNull String pem)
	{
		String base64cert = pemKeyContent(pem);
		return fromBase64String(base64cert);
	}

	private final @NonNull InputStream fromBase64String(final @NonNull String base64cert)
	{
		byte[] decoded = Base64.decode(base64cert, Base64.NO_WRAP);
		return new ByteArrayInputStream(decoded);
	}

	private final @NonNull String pemKeyContent(final @NonNull String pem)
	{
		return pem.replace("\\s+", "")
				  .replace("\n", "")
				  .replace("-----BEGIN PUBLIC KEY-----", "")
				  .replace("-----END PUBLIC KEY-----", "")
				  .replace("-----BEGIN CERTIFICATE-----", "")
				  .replace("-----END CERTIFICATE-----", "");
	}
}
