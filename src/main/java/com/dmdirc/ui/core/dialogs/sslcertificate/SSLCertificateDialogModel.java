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

package com.dmdirc.ui.core.dialogs.sslcertificate;

import com.dmdirc.tls.CertificateAction;
import com.dmdirc.tls.CertificateDoesntMatchHostException;
import com.dmdirc.tls.CertificateHostChecker;
import com.dmdirc.tls.CertificateManager;
import com.dmdirc.tls.CertificateNotTrustedException;

import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Model for SSL certificate dialogs.
 *
 * @since 0.6.3m1
 */
public class SSLCertificateDialogModel {

    /** The text to use if a field isn't present on the certificate. */
    private static final String NOTPRESENT = "(not present on certificate)";
    /** The certificate chain that we're displaying information about. */
    private final List<X509Certificate> chain;
    /** The certificate manager for the connection attempt. */
    private final CertificateManager manager;
    /** The list of problems found with the certs, if any. */
    private final Collection<CertificateException> problems;
    /** Checker to use for hostnames. */
    private final CertificateHostChecker hostChecker;

    /**
     * Creates a new SSLCertificateDialogModel for the specified chain.
     *
     * @param chain    The chain of certificates to display info on
     * @param problems A list of problems with the certificates, if any
     * @param manager  The certificate manager responsible for the certs
     */
    public SSLCertificateDialogModel(final List<X509Certificate> chain,
            final Collection<CertificateException> problems,
            final CertificateManager manager) {
        this.chain = chain;
        this.problems = problems;
        this.manager = manager;
        this.hostChecker = new CertificateHostChecker();
    }

    /**
     * Retrieves the certificate chain that's under question.
     *
     * @return A list of {@link CertificateChainEntry}s corresponding to the certificate chain being
     *         questioned.
     */
    public List<CertificateChainEntry> getCertificateChain() {
        final List<CertificateChainEntry> res = new ArrayList<>();

        boolean first = true;

        for (X509Certificate cert : chain) {
            boolean invalid = first && !hostChecker.isValidFor(cert, manager.getServerName());
            first = false;

            try {
                cert.checkValidity();
            } catch (CertificateException ex) {
                invalid |= true;
            }

            res.add(new CertificateChainEntry(CertificateManager
                    .getDNFieldsFromCert(cert).get("CN"),
                    manager.isTrusted(cert).isTrusted(), invalid));
        }

        return res;
    }

    /**
     * Retrieves displayable information about the certificate with the specified index in the
     * chain.
     *
     * @param index The index of the certificate to request information on
     *
     * @return A list of lists of {@link CertificateInformationEntry}s.
     */
    public List<List<CertificateInformationEntry>> getCertificateInfo(final int index) {
        final List<List<CertificateInformationEntry>> res = new ArrayList<>();
        final X509Certificate cert = chain.get(index);
        List<CertificateInformationEntry> group;

        boolean tooOld = false;
        boolean tooNew = false;

        try {
            cert.checkValidity();
        } catch (CertificateExpiredException ex) {
            tooOld = true;
        } catch (CertificateNotYetValidException ex) {
            tooNew = true;
        }

        group = new ArrayList<>();
        group.add(new CertificateInformationEntry("Valid from",
                cert.getNotBefore().toString(), tooNew, false));
        group.add(new CertificateInformationEntry("Valid to",
                cert.getNotAfter().toString(), tooOld, false));
        res.add(group);

        final boolean wrongName = index == 0 && !hostChecker.isValidFor(cert, manager.getServerName());
        final String names = getAlternateNames(cert);
        final Map<String, String> fields = CertificateManager.getDNFieldsFromCert(cert);

        group = new ArrayList<>();
        addCertField(fields, group, "Common name", "CN", wrongName);

        group.add(new CertificateInformationEntry("Alternate names",
                names == null ? NOTPRESENT : names, wrongName, names == null));

        addCertField(fields, group, "Organisation", "O", false);
        addCertField(fields, group, "Unit", "OU", false);
        addCertField(fields, group, "Locality", "L", false);
        addCertField(fields, group, "State", "ST", false);
        addCertField(fields, group, "Country", "C", false);
        res.add(group);

        group = new ArrayList<>();
        group.add(new CertificateInformationEntry("Serial number",
                cert.getSerialNumber().toString(), false, false));
        group.add(new CertificateInformationEntry("Algorithm",
                cert.getSigAlgName(), false, false));
        group.add(new CertificateInformationEntry("SSL version",
                String.valueOf(cert.getVersion()), false, false));
        res.add(group);

        return res;
    }

    /**
     * Retrieves a list of all the alternate names of the specified certificates as a
     * comma-separated string.
     *
     * @param cert The certificate to retrieve alternate names for
     *
     * @return A comma-separated list of alternate names
     */
    private String getAlternateNames(final X509Certificate cert) {
        final StringBuilder res = new StringBuilder();

        try {
            if (cert.getSubjectAlternativeNames() == null) {
                return null;
            }

            for (List<?> entry : cert.getSubjectAlternativeNames()) {
                final int type = (Integer) entry.get(0);

                // DNS or IP
                if (type == 2 || type == 7) {
                    if (res.length() > 0) {
                        res.append(", ");
                    }

                    res.append(entry.get(1));
                }
            }
        } catch (CertificateParsingException ex) {
            // Do nothing
        }

        return res.toString();
    }

    /**
     * Adds a field to the specified group.
     *
     * @param fields  The fields extracted from the certificate
     * @param group   The group to add an entry to
     * @param title   The user-friendly title of the field
     * @param field   The name of the field to look for
     * @param invalid Whether or not the field is a cause for concern
     */
    private void addCertField(
            final Map<String, String> fields,
            final List<CertificateInformationEntry> group,
            final String title,
            final String field,
            final boolean invalid) {
        group.add(new CertificateInformationEntry(title, fields.getOrDefault(field, NOTPRESENT), invalid,
                !fields.containsKey(field)));
    }

    /**
     * Retrieves a list of summary elements to describe the overall status of the certificate chain.
     *
     * @return A list of summary entries
     */
    public List<CertificateSummaryEntry> getSummary() {
        final List<CertificateSummaryEntry> res = new ArrayList<>();

        boolean outOfDate = false;
        boolean wrongHost = false;
        boolean notTrusted = false;

        for (CertificateException ex : problems) {
            if (ex instanceof CertificateExpiredException
                    || ex instanceof CertificateNotYetValidException) {
                outOfDate = true;
            } else if (ex instanceof CertificateDoesntMatchHostException) {
                wrongHost = true;
            } else if (ex instanceof CertificateNotTrustedException) {
                notTrusted = true;
            }
        }

        if (outOfDate) {
            res.add(new CertificateSummaryEntry("One or more certificates are "
                    + "not within their validity period", false));
        } else {
            res.add(new CertificateSummaryEntry("All certificates are "
                    + "within their validity period", true));
        }

        if (notTrusted) {
            res.add(new CertificateSummaryEntry("The certificate is not issued "
                    + "by a trusted authority", false));
        } else {
            res.add(new CertificateSummaryEntry("The certificate chain is "
                    + "trusted", true));
        }

        if (wrongHost) {
            res.add(new CertificateSummaryEntry("The certificate is not issued "
                    + "to the host you are connecting to", false));
        } else {
            res.add(new CertificateSummaryEntry("The certificate is issued "
                    + "to the host you are connecting to", true));
        }

        return res;
    }

    /**
     * Determines whether or not a response is required from the user about this certificate chain.
     *
     * @return True if a response is required, false otherwise
     */
    public boolean needsResponse() {
        return !problems.isEmpty();
    }

    /**
     * Retrieves the name of the server to which the user is trying to connect.
     *
     * @return The name of the server that the user is trying to connect to
     */
    public String getServerName() {
        return manager.getServerName();
    }

    /**
     * Performs the specified action on the certificate chain/connection. Should only be called once
     * per instance, and only if {@link #needsResponse()} returns true.
     *
     * @param action The action to be performed
     */
    public void performAction(final CertificateAction action) {
        if (!needsResponse()) {
            throw new IllegalStateException("Can't perform action when "
                    + "no action is needed");
        }

        manager.setAction(action);
    }

}
