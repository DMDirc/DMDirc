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

package com.dmdirc.ui.core.dialogs.sslcertificate;

import com.dmdirc.CertificateManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for SSL certificate dialogs.
 *
 * @since 0.6.3
 * @author chris
 */
public class SSLCertificateDialogModel {

    /** The certificate chain that we're displaying information about. */
    private final X509Certificate[] chain;

    /** The certificate manager for the connection attempt. */
    private final CertificateManager manager;

    /** The list of problems found with the certs, if any. */
    private final List<CertificateException> problems;

    /**
     * Creates a new SSLCertificateDialogModel for the specified chain.
     *
     * @param chain The chain of certificates to display info on
     * @param problems A list of problems with the certificates, if any
     * @param manager The certificate manager responsible for the certs
     */
    public SSLCertificateDialogModel(final X509Certificate[] chain,
            final List<CertificateException> problems,
            final CertificateManager manager) {
        this.chain = chain;
        this.problems = problems;
        this.manager = manager;
    }

    /**
     * Retrieves displayable information about the certificate with the
     * specified index in the chain.
     *
     * @param index The index of the certificate to request information on
     * @return A list of lists of {@link CertificateInformationEntry}s.
     */
    public List<List<CertificateInformationEntry>> getCertificateInfo(final int index) {
        final List<List<CertificateInformationEntry>> res
                = new ArrayList<List<CertificateInformationEntry>>();
        final X509Certificate cert = chain[index];
        List<CertificateInformationEntry> group;

        group = new ArrayList<CertificateInformationEntry>();
        group.add(new CertificateInformationEntry("Valid from",
                cert.getNotBefore().toString(), false, false));
        group.add(new CertificateInformationEntry("Valid to",
                cert.getNotAfter().toString(), false, false));
        res.add(group);

        group = new ArrayList<CertificateInformationEntry>();
        group.add(new CertificateInformationEntry("Organisation",
                "foo", false, false));

        return res;
    }

    /**
     * Retrieves a list of summary elements to describe the overall status
     * of the certificate chain.
     *
     * @return A list of summary entries
     */
    public List<CertificateSummaryEntry> getSummary() {
        final List<CertificateSummaryEntry> res = new ArrayList<CertificateSummaryEntry>();

        res.add(new CertificateSummaryEntry("Stuffity mcStuff stuff", true));
        res.add(new CertificateSummaryEntry("Other stuffity mcStuff stuff", false));

        return res;
    }
    
    /**
     * Determines whether or not a response is required from the user about
     * this certificate chain.
     *
     * @return True if a response is required, false otherwise
     */
    public boolean needsResponse() {
        return !problems.isEmpty();
    }

    /**
     * Performs the specified action on the certificate chain/connection.
     * Should only be called once per instance, and only if
     * {@link #needsResponse()} returns true.
     * 
     * @param action The action to be performed
     */
    public void performAction(final CertificateAction action) {

    }

}
