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

    private final X509Certificate[] chain;

    private final CertificateManager manager;

    private final List<CertificateException> problems;

    public SSLCertificateDialogModel(final X509Certificate[] chain,
            final List<CertificateException> problems,
            final CertificateManager manager) {
        this.chain = chain;
        this.problems = problems;
        this.manager = manager;
    }

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

    public List<CertificateSummaryEntry> getSummary() {
        final List<CertificateSummaryEntry> res = new ArrayList<CertificateSummaryEntry>();

        res.add(new CertificateSummaryEntry("Stuffity mcStuff stuff", true));
        res.add(new CertificateSummaryEntry("Other stuffity mcStuff stuff", false));

        return res;
    }
    
    public boolean needsResponse() {
        return !problems.isEmpty();
    }

    public void dismiss(final CertificateAction action) {

    }

}
