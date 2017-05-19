package com.dmdirc.tls;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

/**
 * Checks that the host we're connecting to is one specified in a certificate.
 *
 * <p>Certificates match if any of their subjectAlternateName extensions, or the subject's common name, matches
 * the host we're connecting to.
 */
public class CertificateHostChecker {

    /**
     * Checks if the specified certificate is valid for the given hostname.
     *
     * @param certificate The certificate to check.
     * @param host The hostname that was connected to.
     * @return True if the certificate covers the given hostname, false otherwise.
     */
    public boolean isValidFor(final X509Certificate certificate, final String host) {
        return getAllNames(certificate).stream().anyMatch(name -> matches(name, host));
    }

    /**
     * Checks if the specified name matches against the host, taking into account wildcards.
     *
     * <p>Hosts are compared by splitting them into domain parts (test.dmdirc.com becomes [test, dmdirc, com]) and
     * comparing each part against the corresponding part of the supplied name. Wildcards may only expand within a
     * single part (i.e. *.example.com cannot match foo.bar.example.com).
     *
     * @param name The name to check.
     * @param host The host to check it against.
     * @return True if the name matches the host; false otherwise.
     */
    private boolean matches(final String name, final String host) {
        final String[] nameParts = name.split("\\.");
        final String[] hostParts = host.split("\\.");

        if (nameParts.length != hostParts.length) {
            return false;
        }

        for (int i = 0; i < nameParts.length; i++) {
            if (!partMatches(nameParts[i], hostParts[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the specified part of the name matches the corresponding part of the host, taking into account
     * wildcards.
     *
     * @param namePart The part of the name to be expanded and checked.
     * @param hostPart The corresponding part of the host to check the name against.
     * @return True if the name and host parts match; false otherwise.
     */
    private boolean partMatches(final String namePart, final String hostPart) {
        return "*".equals(namePart) || hostPart.toLowerCase().matches(
                Arrays.stream(namePart.toLowerCase().split("\\*"))
                        .map(Pattern::quote)
                        .collect(Collectors.joining(".*")));
    }

    /**
     * Returns all names for which a certificate is valid.
     *
     * @param cert The certificate to read.
     * @return The names which the certificate covers.
     */
    private Set<String> getAllNames(final X509Certificate cert) {
        final Set<String> names = new HashSet<>();
        getCommonName(cert).ifPresent(names::add);
        getSubjectAlternateNames(cert).ifPresent(names::addAll);
        return names;
    }

    /**
     * Reads the common name (CN) from the certificate's subject.
     *
     * @param cert The certificate to read.
     * @return The common name of the certificate, if present.
     */
    private Optional<String> getCommonName(final X509Certificate cert) {
        try {
            final LdapName name = new LdapName(cert.getSubjectX500Principal().getName());
            return name.getRdns().stream()
                    .filter(rdn -> "CN".equalsIgnoreCase(rdn.getType()))
                    .map(Rdn::getValue)
                    .map(Object::toString)
                    .findFirst();
        } catch (InvalidNameException ex) {
            return Optional.empty();
        }
    }

    /**
     * Reads all the subjectAlternateName extensions from the certificate.
     *
     * @param cert The certificate to read.
     * @return The (possibly empty) set of subject alternate names.
     */
    private Optional<Set<String>> getSubjectAlternateNames(final X509Certificate cert) {
        try {
            return Optional.ofNullable(cert.getSubjectAlternativeNames())
                    .map(sans -> sans.stream()
                            // Filter for GeneralName type 2 (dNSName)
                            .filter(san -> (Integer) san.get(0) == 2)
                            // Get the string representation of the value of those names
                            .map(san -> san.get(1))
                            .map(Object::toString)
                            .collect(Collectors.toSet()));
        } catch (CertificateParsingException e) {
            return Optional.empty();
        }
    }

}
