package me.wizos.loread.utils;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Wizos on 2019/8/3.
 */

public class HttpsUtils {
    private HttpsUtils() {
    }

    public static X509TrustManager UnSafeTrustManager = new X509TrustManager() {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    public static HostnameVerifier UnSafeHostnameVerifier = new HostnameVerifier() {
        @SuppressLint("BadHostnameVerifier")
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


    public static SSLParams getSslSocketFactory() {
        return getSslSocketFactoryBase((X509TrustManager) null, (InputStream) null, (String) null);
    }

    public static SSLParams getSslSocketFactory(X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, (InputStream) null, (String) null);
    }

    public static SSLParams getSslSocketFactory(InputStream... certificates) {
        return getSslSocketFactoryBase((X509TrustManager) null, (InputStream) null, (String) null, certificates);
    }

    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, InputStream... certificates) {
        return getSslSocketFactoryBase((X509TrustManager) null, bksFile, password, certificates);
    }

    public static SSLParams getSslSocketFactory(InputStream bksFile, String password, X509TrustManager trustManager) {
        return getSslSocketFactoryBase(trustManager, bksFile, password);
    }

    private static SSLParams getSslSocketFactoryBase(X509TrustManager trustManager, InputStream bksFile, String password, InputStream... certificates) {
        SSLParams sslParams = new SSLParams();

        try {
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            X509TrustManager manager;
            if (trustManager != null) {
                manager = trustManager;
            } else if (trustManagers != null) {
                manager = chooseTrustManager(trustManagers);
            } else {
                manager = UnSafeTrustManager;
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, new TrustManager[]{manager}, (SecureRandom) null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = manager;
            return sslParams;
        } catch (NoSuchAlgorithmException | KeyManagementException var9) {
            throw new AssertionError(var9);
        }
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile != null && password != null) {
                KeyStore clientKeyStore = KeyStore.getInstance("BKS");
                clientKeyStore.load(bksFile, password.toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(clientKeyStore, password.toCharArray());
                return kmf.getKeyManagers();
            } else {
                return null;
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    private static TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates != null && certificates.length > 0) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load((KeyStore.LoadStoreParameter) null);
                int index = 0;
                int var5 = certificates.length;

                for (InputStream certStream : certificates) {
                    String certificateAlias = Integer.toString(index++);
                    Certificate cert = certificateFactory.generateCertificate(certStream);
                    keyStore.setCertificateEntry(certificateAlias, cert);

                    try {
                        if (certStream != null) {
                            certStream.close();
                        }
                    } catch (IOException var11) {
                        var11.printStackTrace();
                    }
                }

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);
                return tmf.getTrustManagers();
            } catch (Exception var12) {
                var12.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
        public SSLParams() { }
    }
}
