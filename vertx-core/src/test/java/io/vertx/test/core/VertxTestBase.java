package io.vertx.test.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.impl.ClasspathPathResolver;
import io.vertx.core.net.CaOptions;
import io.vertx.core.net.JKSOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.core.net.PKCS12Options;
import io.vertx.core.net.TrustStoreOptions;
import org.junit.After;
import org.junit.Before;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class VertxTestBase extends AsyncTestBase {

  protected Vertx vertx;

  @Before
  public void beforeVertxTestBase() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void afterVertxTestBase() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    vertx.close(ar -> {
      assertTrue(ar.succeeded());
      latch.countDown();
    });
    awaitLatch(latch);
  }

  protected String findFileOnClasspath(String fileName) {
    URL url = getClass().getClassLoader().getResource(fileName);
    if (url == null) {
      throw new IllegalArgumentException("Cannot find file " + fileName + " on classpath");
    }
    Path path = ClasspathPathResolver.urlToPath(url).toAbsolutePath();
    return path.toString();
  }

  protected <T> Handler<AsyncResult<T>> onSuccess(Consumer<T> consumer) {
    return result -> {
      if (result.failed()) {
        fail(result.cause().getMessage());
      } else {
        consumer.accept(result.result());
      }
    };
  }

  protected <T> Handler<AsyncResult<T>> onFailure(Consumer<T> consumer) {
    return result -> {
      assertFalse(result.succeeded());
      consumer.accept(result.result());
    };
  }

  protected void awaitLatch(CountDownLatch latch) throws InterruptedException {
    assertTrue(latch.await(10, TimeUnit.SECONDS));
  }

  protected void waitUntil(BooleanSupplier supplier) throws Exception {
    long start = System.currentTimeMillis();
    long timeout = 10000;
    while (true) {
      if (supplier.getAsBoolean()) {
        break;
      }
      Thread.sleep(10);
      long now = System.currentTimeMillis();
      if (now - start > timeout) {
        throw new IllegalStateException("Timed out");
      }
    }
  }

  protected TrustStoreOptions getClientTrustOptions(TS trust) {
    switch (trust) {
      case JKS:
        return new JKSOptions().setPath(findFileOnClasspath("tls/client-truststore.jks")).setPassword("wibble");
      case PKCS12:
        return new PKCS12Options().setPath(findFileOnClasspath("tls/client-truststore.p12")).setPassword("wibble");
      case PEM:
        return new CaOptions().addCertPath(findFileOnClasspath("tls/server-cert.pem"));
      case PEM_CA:
        return new CaOptions().addCertPath(findFileOnClasspath("tls/ca/ca-cert.pem"));
      default:
        return null;
    }
  }

  protected KeyStoreOptions getClientCertOptions(KS cert) {
    switch (cert) {
      case JKS:
        return new JKSOptions().setPath(findFileOnClasspath("tls/client-keystore.jks")).setPassword("wibble");
      case PKCS12:
        return new PKCS12Options().setPath(findFileOnClasspath("tls/client-keystore.p12")).setPassword("wibble");
      case PEM:
        return new KeyCertOptions().setKeyPath(findFileOnClasspath("tls/client-key.pem")).setCertPath(findFileOnClasspath("tls/client-cert.pem"));
      case PEM_CA:
        return new KeyCertOptions().setKeyPath(findFileOnClasspath("tls/client-key.pem")).setCertPath(findFileOnClasspath("tls/client-cert-ca.pem"));
      default:
        return null;
    }
  }

  protected TrustStoreOptions getServerTrustOptions(TS trust) {
    switch (trust) {
      case JKS:
        return new JKSOptions().setPath(findFileOnClasspath("tls/server-truststore.jks")).setPassword("wibble");
      case PKCS12:
        return new PKCS12Options().setPath(findFileOnClasspath("tls/server-truststore.p12")).setPassword("wibble");
      case PEM:
        return new CaOptions().addCertPath(findFileOnClasspath("tls/client-cert.pem"));
      case PEM_CA:
        return new CaOptions().addCertPath(findFileOnClasspath("tls/ca/ca-cert.pem"));
      default:
        return null;
    }
  }

  protected KeyStoreOptions getServerCertOptions(KS cert) {
    switch (cert) {
      case JKS:
        return new JKSOptions().setPath(findFileOnClasspath("tls/server-keystore.jks")).setPassword("wibble");
      case PKCS12:
        return new PKCS12Options().setPath(findFileOnClasspath("tls/server-keystore.p12")).setPassword("wibble");
      case PEM:
        return new KeyCertOptions().setKeyPath(findFileOnClasspath("tls/server-key.pem")).setCertPath(findFileOnClasspath("tls/server-cert.pem"));
      case PEM_CA:
        return new KeyCertOptions().setKeyPath(findFileOnClasspath("tls/server-key.pem")).setCertPath(findFileOnClasspath("tls/server-cert-ca.pem"));
      default:
        return null;
    }
  }

  protected static final String[] ENABLED_CIPHER_SUITES =
    new String[] {
      "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
      "TLS_RSA_WITH_AES_128_CBC_SHA256",
      "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
      "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
      "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
      "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
      "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
      "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
      "TLS_RSA_WITH_AES_128_CBC_SHA",
      "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
      "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
      "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
      "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
      "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
      "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
      "SSL_RSA_WITH_RC4_128_SHA",
      "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
      "TLS_ECDH_RSA_WITH_RC4_128_SHA",
      "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
      "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
      "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
      "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
      "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
      "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
      "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
      "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
      "SSL_RSA_WITH_RC4_128_MD5",
      "TLS_EMPTY_RENEGOTIATION_INFO_SCSV",
      "TLS_DH_anon_WITH_AES_128_GCM_SHA256",
      "TLS_DH_anon_WITH_AES_128_CBC_SHA256",
      "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
      "TLS_DH_anon_WITH_AES_128_CBC_SHA",
      "TLS_ECDH_anon_WITH_RC4_128_SHA",
      "SSL_DH_anon_WITH_RC4_128_MD5",
      "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
      "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
      "TLS_RSA_WITH_NULL_SHA256",
      "TLS_ECDHE_ECDSA_WITH_NULL_SHA",
      "TLS_ECDHE_RSA_WITH_NULL_SHA",
      "SSL_RSA_WITH_NULL_SHA",
      "TLS_ECDH_ECDSA_WITH_NULL_SHA",
      "TLS_ECDH_RSA_WITH_NULL_SHA",
      "TLS_ECDH_anon_WITH_NULL_SHA",
      "SSL_RSA_WITH_NULL_MD5",
      "SSL_RSA_WITH_DES_CBC_SHA",
      "SSL_DHE_RSA_WITH_DES_CBC_SHA",
      "SSL_DHE_DSS_WITH_DES_CBC_SHA",
      "SSL_DH_anon_WITH_DES_CBC_SHA",
      "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
      "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
      "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
      "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
      "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
      "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
      "TLS_KRB5_WITH_RC4_128_SHA",
      "TLS_KRB5_WITH_RC4_128_MD5",
      "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
      "TLS_KRB5_WITH_3DES_EDE_CBC_MD5",
      "TLS_KRB5_WITH_DES_CBC_SHA",
      "TLS_KRB5_WITH_DES_CBC_MD5",
      "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
      "TLS_KRB5_EXPORT_WITH_RC4_40_MD5",
      "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
      "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5"
    };

}
