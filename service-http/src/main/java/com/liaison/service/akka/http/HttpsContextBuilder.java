package com.liaison.service.akka.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.HttpsConnectionContext;
import com.typesafe.config.Config;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class HttpsContextBuilder {

    public static HttpsConnectionContext build(ActorSystem system) {
        Config config = system.settings().config();
        final char[] password = config.getString("akka.remote.netty.ssl.security.trust-store-password").toCharArray();
        String keyStorePath = config.getString("akka.remote.netty.ssl.security.trust-store");
        try (InputStream keyStoreStream = new FileInputStream(keyStorePath)) {

            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreStream, password);

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            return ConnectionContext.https(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | KeyStoreException | UnrecoverableKeyException | IOException e) {
            throw new IllegalStateException("Exception while configuring HTTPS.", e);
        }
    }
}
