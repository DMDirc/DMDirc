package com.dmdirc.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CertificateExceptionManager}.
 *
 * <p>These test use two certificates stored in a keystore. They were generated using:
 *
 * <pre>
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "test1" -dname "CN=Test1, O=DMDirc, C=GB"
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "test2" -dname "CN=Test2, O=DMDirc, C=GB"
 * </pre>
 */
public class CertificateExceptionManagerTest {

    @Rule
    public TemporaryFolder tempFolderRule = new TemporaryFolder();

    private Path keyStorePath;
    private CertificateExceptionManager manager;

    @Before
    public void setup() throws IOException {
        keyStorePath = tempFolderRule.newFile("certs.keystore").toPath();
        manager = new CertificateExceptionManager(keyStorePath);
    }

    @Test
    public void testGetCertsNoFile() {
        assertTrue(manager.getExceptedCertificates().isEmpty());
    }

    @Test
    public void testAddCert() throws GeneralSecurityException, IOException {
        final X509Certificate cert = getCertificate(1);
        assertTrue(manager.addExceptedCertificate(cert));
        assertTrue(Files.exists(keyStorePath));
        final Set<X509Certificate> certs = manager.getExceptedCertificates();
        assertEquals(1, certs.size());
        assertTrue(certs.contains(cert));
    }

    @Test
    public void testRemoveUnknownCert() throws GeneralSecurityException, IOException {
        final X509Certificate cert = getCertificate(1);
        assertFalse(manager.removeExceptedCertificate(cert));
    }

    @Test
    public void testRemoveCert() throws GeneralSecurityException, IOException {
        final X509Certificate cert = getCertificate(1);
        manager.addExceptedCertificate(cert);
        assertTrue(manager.removeExceptedCertificate(cert));
        assertTrue(manager.getExceptedCertificates().isEmpty());
    }

    @Test
    public void testRemoveCertLeavesExisting() throws GeneralSecurityException, IOException {
        final X509Certificate cert1 = getCertificate(1);
        final X509Certificate cert2 = getCertificate(2);
        manager.addExceptedCertificate(cert1);
        manager.addExceptedCertificate(cert2);
        assertTrue(manager.removeExceptedCertificate(cert1));
        final Set<X509Certificate> certs = manager.getExceptedCertificates();
        assertEquals(1, certs.size());
        assertTrue(certs.contains(cert2));
    }

    private X509Certificate getCertificate(final int num) throws GeneralSecurityException, IOException {
        try (InputStream is = getClass().getResourceAsStream("keystore.ks")) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, "dmdirc".toCharArray());
            return (X509Certificate) keyStore.getCertificate("test" + num);
        }
    }

}