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

package com.dmdirc.tls;

/**
 * An enumeration of the possible trust states for an X509 cert chain received from a remote server.
 */
public enum TrustResult {

    /** There are no issues and the CA is in our root file. */
    TRUSTED_CA(true),
    /** There are no issues and the cert is manually trusted by the user. */
    TRUSTED_MANUALLY(true),
    /** The certificate chain was
     * untrusted because it failed validation. */
    UNTRUSTED_EXCEPTION(false),
    /** The certificate chain validated but isn't explicitly trusted. */
    UNTRUSTED_GENERAL(false);
    /** Whether or not this result means the cert chain is trusted. */
    private final boolean trusted;

    /**
     * Creates a new TrustResult.
     *
     * @param trusted Whether or not this result means the cert chain is trusted.
     */
    TrustResult(final boolean trusted) {
        this.trusted = trusted;
    }

    /**
     * Returns whether or not this result indicates a trusted cert chain.
     *
     * @return True if the chain should be trusted, false otherwise.
     */
    public boolean isTrusted() {
        return trusted;
    }

}
