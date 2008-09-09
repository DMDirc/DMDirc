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
import com.dmdirc.CertificateManager.CertificateDoesntMatchHostException;
import com.dmdirc.CertificateManager.CertificateNotTrustedException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /** The text to use if a field isn't present on the certificate. */
    private static final String NOTPRESENT = "(not present on certificate)";

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
     * Retrieves the certificate chain that's under question.
     *
     * @return A list of {@link CertificateChainEntry}s corresponding to the
     * certificate chain being questioned.
     */
    public List<CertificateChainEntry> getCertificateChain() {
        final List<CertificateChainEntry> res = new ArrayList<CertificateChainEntry>();

        for (X509Certificate cert : chain) {
            boolean invalid = false;

            try {
                cert.checkValidity();
            } catch (CertificateException ex) {
                invalid = true;
            }

            res.add(new CertificateChainEntry(cert.getSubjectDN().getName(),
                    false, invalid)); // TODO: false hardcoded, name?
        }

        return res;
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
                cert.getNotBefore().toString(), false, false)); // TODO: false!
        group.add(new CertificateInformationEntry("Valid to",
                cert.getNotAfter().toString(), false, false)); // TODO: false!
        res.add(group);

        final Map<String, String> fields = CertificateManager.getDNFieldsFromCert(cert);
        group = new ArrayList<CertificateInformationEntry>();
        addCertField(fields, group, "Common name", "CN");
        addCertField(fields, group, "Organisation", "O");
        addCertField(fields, group, "Unit", "OU");
        addCertField(fields, group, "Locality", "L");
        addCertField(fields, group, "State", "ST");
        addCertField(fields, group, "Country", "C");
        res.add(group);

        group = new ArrayList<CertificateInformationEntry>();
        group.add(new CertificateInformationEntry("Serial number",
                cert.getSerialNumber().toString(), false, false));
        group.add(new CertificateInformationEntry("Signature",
                "todo", false, false)); // TODO: cert.getSignature()
        group.add(new CertificateInformationEntry("Algorithm",
                cert.getSigAlgName(), false, false));
        group.add(new CertificateInformationEntry("SSL version",
                String.valueOf(cert.getVersion()), false, false));
        res.add(group);

        return res;
    }

    /**
     * Adds a field to the specified group.
     *
     * @param fields The fields extracted from the certiciate
     * @param group The group to add an entry to
     * @param title The user-friendly title of the field
     * @param field The name of the field to look for
     */
    protected void addCertField(final Map<String, String> fields,
            final List<CertificateInformationEntry> group, final String title,
            final String field) {
        group.add(new CertificateInformationEntry(title,
                fields.containsKey(title) ? fields.get(title) : NOTPRESENT, false,
                !fields.containsKey(title)));
    }

    /**
     * Retrieves a list of summary elements to describe the overall status
     * of the certificate chain.
     *
     * @return A list of summary entries
     */
    public List<CertificateSummaryEntry> getSummary() {
        final List<CertificateSummaryEntry> res = new ArrayList<CertificateSummaryEntry>();

        boolean outofdate = false, wronghost = false, nottrusted = false;

        for (CertificateException ex : problems) {
            if (ex instanceof CertificateExpiredException
                    || ex instanceof CertificateNotYetValidException) {
                outofdate = true;
            } else if (ex instanceof CertificateDoesntMatchHostException) {
                wronghost = true;
            } else if (ex instanceof CertificateNotTrustedException) {
                nottrusted = true;
            }
        }

        if (outofdate) {
            res.add(new CertificateSummaryEntry("One or more certificates are " +
                    "not within their validity period", false));
        } else {
            res.add(new CertificateSummaryEntry("All certificates are " +
                    "within their validity period", true));
        }

        if (nottrusted) {
            res.add(new CertificateSummaryEntry("The certificate is not issued "
                    + "by a trusted authority", false));
        } else {
            res.add(new CertificateSummaryEntry("The certificate chain is "
                    + "trusted", true));
        }

        if (wronghost) {
            res.add(new CertificateSummaryEntry("The certificate is not issued "
                    + "to the host you are connecting to", false));
        } else {
            res.add(new CertificateSummaryEntry("The certificate is issued "
                    + "to the host you are connecting to", false));
        }

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
        // TODO: ...
    }

}
