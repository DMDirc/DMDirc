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

package com.dmdirc.events;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.tls.CertificateManager;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Event raised when a problem is encountered with a server certificate.
 */
public class ServerCertificateProblemEncounteredEvent extends ServerCertificateEvent {

    private final List<X509Certificate> certificateChain;
    private final Collection<CertificateException> problems;

    public ServerCertificateProblemEncounteredEvent(
            final Connection connection,
            final CertificateManager certificateManager,
            final List<X509Certificate> certificateChain,
            final Collection<CertificateException> problems) {
        super(connection, certificateManager);
        this.certificateChain = Collections.unmodifiableList(certificateChain);
        this.problems = Collections.unmodifiableCollection(problems);
    }

    public List<X509Certificate> getCertificateChain() {
        return Collections.unmodifiableList(certificateChain);
    }

    public Collection<CertificateException> getProblems() {
        return Collections.unmodifiableCollection(problems);
    }

}
