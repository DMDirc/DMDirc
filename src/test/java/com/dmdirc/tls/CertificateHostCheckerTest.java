package com.dmdirc.tls;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link CertificateHostChecker}.
 *
 * <p>These tests use several certificates stored in a keystore. They were generated using:
 *
 * <pre>
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "name_cn_only" -dname "CN=test.example.com, O=DMDirc, C=GB"
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "name_cn_wildcard" -dname "CN=*.example.com, O=DMDirc, C=GB"
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "name_san_dns" -dname "CN=other.example.com, O=DMDirc, C=GB" -ext SAN=dns:test.example.com
 * keytool -genkey -validity 18250 -keystore "keystore.ks" -storepass "dmdirc" -keypass "dmdirc" -alias "name_san_dns_multiple" -dname "CN=other.example.com, O=DMDirc, C=GB" -ext SAN=dns:foo.example.com,dns:test.example.com
 * </pre>
 */
public class CertificateHostCheckerTest {

    private CertificateHostChecker checker;

    @Before
    public void setup() {
        checker = new CertificateHostChecker();
    }

    @Test
    public void testBasicCn() throws GeneralSecurityException, IOException {
        final X509Certificate certificate = getCertificate("name_cn_only");
        assertTrue(checker.isValidFor(certificate, "test.example.com"));
        assertTrue(checker.isValidFor(certificate, "TEsT.example.com"));
        assertFalse(checker.isValidFor(certificate, "foo.example.com"));
        assertFalse(checker.isValidFor(certificate, "test.example.org"));
        assertFalse(checker.isValidFor(certificate, "foo.test.example.com"));
    }

    @Test
    public void testWildcardCn() throws GeneralSecurityException, IOException {
        final X509Certificate certificate = getCertificate("name_cn_wildcard");
        assertTrue(checker.isValidFor(certificate, "test.example.com"));
        assertTrue(checker.isValidFor(certificate, "TEsT.example.com"));
        assertTrue(checker.isValidFor(certificate, "foo.example.com"));
        assertFalse(checker.isValidFor(certificate, "test.example.org"));
        assertFalse(checker.isValidFor(certificate, "foo.test.example.com"));
    }

    @Test
    public void testSanDns() throws GeneralSecurityException, IOException {
        final X509Certificate certificate = getCertificate("name_san_dns");
        assertTrue(checker.isValidFor(certificate, "test.example.com"));
        assertTrue(checker.isValidFor(certificate, "TEsT.example.com"));
        assertTrue(checker.isValidFor(certificate, "other.example.com"));
        assertFalse(checker.isValidFor(certificate, "foo.example.com"));
        assertFalse(checker.isValidFor(certificate, "test.example.org"));
        assertFalse(checker.isValidFor(certificate, "foo.test.example.com"));
    }

    @Test
    public void testSanDnsMultiple() throws GeneralSecurityException, IOException {
        final X509Certificate certificate = getCertificate("name_san_dns_multiple");
        assertTrue(checker.isValidFor(certificate, "test.example.com"));
        assertTrue(checker.isValidFor(certificate, "TEsT.example.com"));
        assertTrue(checker.isValidFor(certificate, "other.example.com"));
        assertTrue(checker.isValidFor(certificate, "foo.example.com"));
        assertFalse(checker.isValidFor(certificate, "test.foo.example.org"));
        assertFalse(checker.isValidFor(certificate, "test.example.org"));
        assertFalse(checker.isValidFor(certificate, "foo.test.example.com"));
    }

    private X509Certificate getCertificate(final String name) throws GeneralSecurityException, IOException {
        try (InputStream is = getClass().getResourceAsStream("keystore.ks")) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, "dmdirc".toCharArray());
            return (X509Certificate) keyStore.getCertificate(name);
        }
    }

}