/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Listener interface for objects interested in being notified when there are certificate problems.
 */
public interface CertificateProblemListener {

    /**
     * Called when a problem is encountered with a server's certificate which requires user input.
     *
     * @param chain              The certificate chain received from the server
     * @param problems           A collection of errors that were encountered
     * @param certificateManager The certificate manager handling the connection
     */
    void certificateProblemEncountered(X509Certificate[] chain,
            Collection<CertificateException> problems,
            CertificateManager certificateManager);

    /**
     * Called when a certificate problem has been resolved.
     *
     * @param manager The manager that had raised the problem
     */
    void certificateProblemResolved(CertificateManager manager);

}
