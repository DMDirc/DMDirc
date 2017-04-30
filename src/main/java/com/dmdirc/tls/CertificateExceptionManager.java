package com.dmdirc.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Manages certificates that the user has explicitly trusted, excepting them from the normal PKIX checks.
 */
public class CertificateExceptionManager {

    /** Password to use for our exception keystore. */
    private static final char[] PASSWORD = "dmdirc".toCharArray();

    private final Path keyStorePath;

    public CertificateExceptionManager(final Path keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Returns the set of certificates that the user has explicitly trusted.
     */
    public Set<X509Certificate> getExceptedCertificates() {
        try {
            final KeyStore keyStore = getKeyStore();
            if (keyStore == null) {
                return Collections.emptySet();
            } else {
                final PKIXParameters params = new PKIXParameters(keyStore);
                return params.getTrustAnchors().stream()
                        .map(TrustAnchor::getTrustedCert)
                        .collect(Collectors.toSet());
            }
        } catch (GeneralSecurityException | IOException e) {
            return Collections.emptySet();
        }
    }

    /**
     * Adds a new certificate that will be excepted from future PKIX checks.
     *
     * @return True if the certificate was successfully added; false otherwise.
     */
    public boolean addExceptedCertificate(final X509Certificate certificate) {
        try {
            final KeyStore keyStore = getKeyStore();
            keyStore.setCertificateEntry(
                    "excepted-" + System.currentTimeMillis(),
                    certificate);
            try (OutputStream os = Files.newOutputStream(keyStorePath)) {
                keyStore.store(os, PASSWORD);
                return true;
            }
        } catch (GeneralSecurityException | IOException e) {
            return false;
        }
    }

    /**
     * Removes a certificate that was previously added.
     *
     * @return True if the remove was successful; false otherwise
     */
    public boolean removeExceptedCertificate(final X509Certificate certificate) {
        try {
            final KeyStore keyStore = getKeyStore();

            @Nullable final String alias = keyStore.getCertificateAlias(certificate);
            if (alias == null) {
                // Not found
                return false;
            }

            keyStore.deleteEntry(alias);
            try (OutputStream os = Files.newOutputStream(keyStorePath)) {
                keyStore.store(os, PASSWORD);
                return true;
            }
        } catch (GeneralSecurityException | IOException e) {
            return false;
        }
    }

    /**
     * Loads the existing excepted certs KeyStore from disk, if it exists, or creates a new KeyStore otherwise.
     */
    private KeyStore getKeyStore() throws GeneralSecurityException, IOException {
        try (InputStream is = Files.newInputStream(keyStorePath)) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, PASSWORD);
            return keyStore;
        } catch (GeneralSecurityException | IOException ex) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            return keyStore;
        }
    }


}
