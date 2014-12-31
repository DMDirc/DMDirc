/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.ui.core.dialogs.sslcertificate;

/**
 * Represents one certificate within a chain.
 *
 * @since 0.6.3m1
 */
public class CertificateChainEntry {

    /** The common name of the certificate's subject. */
    private final String name;
    /** Whether or not this certificate is trusted. */
    private final boolean trusted;
    /** Whether or not there are problems with this certificate. */
    private final boolean invalid;

    /**
     * Creates a new entry with the specified details.
     *
     * @param name    The common name of the certificate's subject
     * @param trusted Whether or not this certificate is trusted
     * @param invalid Whether or not this certificate is invalid
     */
    public CertificateChainEntry(final String name, final boolean trusted,
            final boolean invalid) {
        this.name = name;
        this.trusted = trusted;
        this.invalid = invalid;
    }

    /**
     * Determines whether or not this certificate is invalid.
     *
     * @return True if the certificate is invalid, false otherwise
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Determines whether or not this certificate is trusted.
     *
     * @return True if the certificate is from a trusted issuer, false otherwise
     */
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Retrieves the (common) name of this certificate.
     *
     * @return The name of this certificate
     */
    public String getName() {
        return name;
    }

}
