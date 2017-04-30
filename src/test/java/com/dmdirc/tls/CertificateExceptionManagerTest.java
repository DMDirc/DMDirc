package com.dmdirc.tls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CertificateExceptionManager}.
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
        X509Certificate cert = generateCertificate();
        assertTrue(manager.addExceptedCertificate(cert));
        assertTrue(Files.exists(keyStorePath));
        Set<X509Certificate> certs = manager.getExceptedCertificates();
        assertEquals(1, certs.size());
        assertTrue(certs.contains(cert));
    }

    @Test
    public void testRemoveUnknownCert() throws GeneralSecurityException, IOException {
        X509Certificate cert = generateCertificate();
        assertFalse(manager.removeExceptedCertificate(cert));
    }

    @Test
    public void testRemoveCert() throws GeneralSecurityException, IOException {
        X509Certificate cert = generateCertificate();
        manager.addExceptedCertificate(cert);
        assertTrue(manager.removeExceptedCertificate(cert));
        assertTrue(manager.getExceptedCertificates().isEmpty());
    }

    @Test
    public void testRemoveCertLeavesExisting() throws GeneralSecurityException, IOException {
        X509Certificate cert1 = generateCertificate();
        X509Certificate cert2 = generateCertificate();
        manager.addExceptedCertificate(cert1);
        manager.addExceptedCertificate(cert2);
        assertTrue(manager.removeExceptedCertificate(cert1));
        Set<X509Certificate> certs = manager.getExceptedCertificates();
        assertEquals(1, certs.size());
        assertTrue(certs.contains(cert2));
    }

    private X509Certificate generateCertificate() throws GeneralSecurityException, IOException {
        CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        certGen.generate(2048);
        return certGen.getSelfCertificate(new X500Name("CN=Test,O=DMDirc,C=GB"), 120);
    }

}