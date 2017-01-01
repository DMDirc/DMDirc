/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.tls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.annotation.Nullable;

/**
 * Locates a platform-appropriate {@link KeyStore} to read trusted root certificates from.
 */
public class KeyStoreLocator {

    private static final String WINDOWS_PROVIDER = "SunMSCAPI";
    private static final String WINDOWS_TYPE = "Windows-ROOT";
    private static final String MAC_PROVIDER = "Apple";
    private static final String MAC_TYPE = "KeychainStore";
    private static final String LINUX_CACERTS_PATH = "/etc/ssl/certs/java/cacerts";

    /**
     * Attempts to locate an appropriate system-wide source of trusted certificates. If a system
     * store cannot be located or read, the bundled Java cacerts list will be use. If that also
     * fails, {@code null} will be returned.
     *
     * @return The best available KeyStore to use, or {@code null} if no working store was found.
     */
    @Nullable
    public KeyStore getKeyStore() {
        try {
            final String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                return getMacKeyStore();
            } else if (osName.startsWith("Windows")) {
                return getWindowsKeyStore();
            } else {
                return getLinuxKeyStore();
            }
        } catch (IOException | GeneralSecurityException ex1) {
            // Couldn't load a system KeyStore, try to load the Java one.
            try {
                return getJavaKeyStore();
            } catch (IOException | GeneralSecurityException ex2) {
                // Couldn't even load the Java one...
                return null;
            }
        }
    }

    /**
     * Returns the root (system-wide) keystore on Windows operating systems.
     */
    private KeyStore getWindowsKeyStore() throws NoSuchProviderException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(WINDOWS_TYPE, WINDOWS_PROVIDER);
        keyStore.load(null, null);
        return keyStore;
    }

    /**
     * Returns the root (system-wide) keystore on Mac operating systems.
     */
    private KeyStore getMacKeyStore() throws NoSuchProviderException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(MAC_TYPE, MAC_PROVIDER);
        keyStore.load(null, null);
        return keyStore;
    }

    /**
     * Returns a KeyStore that reads system-wide trusted certificates on Linux. These are found in
     * {@code /etc/ssl/certs}, and the Java version is provided by the {@code ca-certificates-java}
     * package.
     */
    private KeyStore getLinuxKeyStore() throws IOException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException {
        try (FileInputStream is = new FileInputStream(LINUX_CACERTS_PATH)) {
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, null);
            return keystore;
        }
    }

    /**
     * Returns a KeyStore that reads from the {@code cacerts} file bundled with the in-use
     * Java Runtime Environment. This is less preferable to using a system-provided KeyStore,
     * as it will not include any user/admin-defined certificates.
     */
    private KeyStore getJavaKeyStore() throws IOException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException {
        final String filename = System.getProperty("java.home")
                + File.separatorChar + "lib"
                + File.separatorChar + "security"
                + File .separatorChar + "cacerts";
        try (FileInputStream is = new FileInputStream(filename)) {
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, null);
            return keystore;
        }
    }

}
