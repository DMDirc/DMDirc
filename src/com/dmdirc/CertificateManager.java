/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

/**
 *
 * @author chris
 */
public class CertificateManager implements X509TrustManager {

    private String cacertpass = "changeit";

    private final String serverName;

    private Set<X509Certificate> trustedCAs = new HashSet<X509Certificate>();

    private boolean checkDate = true, checkIssuer = true, checkHost = true;

    public CertificateManager(final String serverName) {
        this.serverName = serverName;

        loadTrustedCAs();
    }

    protected void loadTrustedCAs() {
        try {
            String filename = System.getProperty("java.home")
                + "/lib/security/cacerts".replace('/', File.separatorChar);
            FileInputStream is = new FileInputStream(filename);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, cacertpass.toCharArray());

            // This class retrieves the most-trusted CAs from the keystore
            PKIXParameters params = new PKIXParameters(keystore);
            for (TrustAnchor anchor : params.getTrustAnchors()) {
                trustedCAs.add(anchor.getTrustedCert());
            }
        } catch (CertificateException ex) {

        } catch (IOException ex) {

        } catch (InvalidAlgorithmParameterException ex) {

        } catch (KeyStoreException ex) {

        } catch (NoSuchAlgorithmException ex) {

        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException {
        throw new CertificateException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException {
        boolean verified = false;

        if (checkHost) {
            System.out.println(chain[0].getSubjectDN().getName());
            if (chain[0].getSubjectAlternativeNames() != null) {
                for (List<?> entry : chain[0].getSubjectAlternativeNames()) {
                    final int type = ((Integer) entry.get(0)).intValue();

                    System.out.println(type + " -- " + entry.get(1) + " -- " + serverName);
                    // DNS or IP
                    if ((type == 2 || type == 7) && entry.get(1).equals(serverName)) {
                        verified = true;
                        break;
                    }
                }
            }

            if (!verified) {
                System.out.println("Certificate not valid for address");
                throw new CertificateException("Certificate not valid for that address");
            }

            verified = false;
        }

        for (X509Certificate cert : chain) {
            if (checkDate) {
                // Check that the certificate is in-date
                cert.checkValidity();
            }

            if (checkIssuer) {
                try {
                    for (X509Certificate trustedCert : trustedCAs) {
                        System.out.println(cert.getIssuerDN().getName() + " <> " + trustedCert.getIssuerDN().getName());
                        if (cert.getSerialNumber().equals(trustedCert.getSerialNumber())
                                && cert.getIssuerDN().getName().equals(trustedCert.getIssuerDN().getName())) {
                            cert.verify(trustedCert.getPublicKey());
                            verified = true;
                            break;
                        }
                    }
                } catch (Exception ex) {
                   throw new CertificateException("Issuer couldn't be verified", ex);
                }
            }
        }

        if (!verified) {
            System.out.println("Issuer is not trusted");
            throw new CertificateException("Issuer is not trusted");
        }
    }

    /** {@inheritDoc} */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        System.out.println("getAcceptedIssuers: " + trustedCAs.size());
        return trustedCAs.toArray(new X509Certificate[trustedCAs.size()]);
    }

}
