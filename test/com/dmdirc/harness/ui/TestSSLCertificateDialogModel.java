/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.harness.ui;

import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateAction;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateChainEntry;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateInformationEntry;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateSummaryEntry;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSSLCertificateDialogModel extends SSLCertificateDialogModel {

    public CertificateAction action = null;
    public boolean needsResponse = true;

    public TestSSLCertificateDialogModel() {
        super(null, null, null);
    }

    @Override
    public List<CertificateChainEntry> getCertificateChain() {
        return Arrays.asList(new CertificateChainEntry[]{
            new CertificateChainEntry("first cert", false, false),
            new CertificateChainEntry("second cert", false, false),
            new CertificateChainEntry("invalid cert", false, true),
            new CertificateChainEntry("trusted cert", true, false),
            new CertificateChainEntry("invalid+trusted", true, true),
        });
    }

    @Override
    public List<List<CertificateInformationEntry>> getCertificateInfo(int index) {
        final List<List<CertificateInformationEntry>> res
                = new ArrayList<List<CertificateInformationEntry>>();
        
        res.add(Arrays.asList(new CertificateInformationEntry[]{
            new CertificateInformationEntry("G1 T1", "value", false, false),
            new CertificateInformationEntry("G1 T2 missing", "value", false, true),
            new CertificateInformationEntry("G1 T3 invalid", "value", true, false),
        }));

        res.add(Arrays.asList(new CertificateInformationEntry[]{
            new CertificateInformationEntry("G2 T1", "value", false, false),
            new CertificateInformationEntry("G2 T2 missing", "value", false, true),
            new CertificateInformationEntry("G2 T3 invalid", "value", true, false),
        }));

        res.add(Arrays.asList(new CertificateInformationEntry[]{
            new CertificateInformationEntry("G3 T1", "value", false, false),
            new CertificateInformationEntry("G3 T2 missing", "value", false, true),
            new CertificateInformationEntry("G3 T3 invalid", "value", true, false),
        }));

        return res;
    }

    @Override
    public List<CertificateSummaryEntry> getSummary() {
        return Arrays.asList(new CertificateSummaryEntry[]{
            new CertificateSummaryEntry("Valid entry one", true),
            new CertificateSummaryEntry("Valid entry two", true),
            new CertificateSummaryEntry("INVALID ENTRY!!1111one", false),
        });
    }

    @Override
    public boolean needsResponse() {
        return needsResponse;
    }

    @Override
    public void performAction(CertificateAction action) {
        this.action = action;
    }

    @Override
    public String getServerName() {
        return "server.name";
    }

}
