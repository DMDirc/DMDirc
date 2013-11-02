/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for parsing IRC URIs from user input.
 */
@Singleton
public class URIParser {

    /** Pattern used to match ports that start with a '+'. */
    private final Pattern plusPortPattern = Pattern.compile(".*://[^/:]+:\\+([0-9]+).*");

    /**
     * Creates a new instance of {@link URIParser}.
     */
    @Inject
    public URIParser() {
    }

    /**
     * Parses the given string as a URI, allowing for a '+' symbol at the start of the port to
     * indicate a secure connection.
     *
     * @param input The string to be parsed.
     * @return An equivalent URI, if one can be parsed.
     * @throws InvalidURIException If the string cannot be parsed as a URI.
     */
    public URI parseFromURI(final String input) throws InvalidURIException {
        try {
            final URI uri = new URI(input);
            final int port = uri.getPort();

            // Either no port was specified, or a +port was used, let's try to find one.
            if (port == -1) {
                final Matcher m = plusPortPattern.matcher(input);

                if (m.matches()) {
                    final int newPort = Integer.parseInt(m.group(2));

                    // Add 's' to scheme if not already there.
                    String scheme = uri.getScheme();
                    if (scheme.charAt(scheme.length() - 1) != 's') {
                        scheme += "s";
                    }
                    return new URI(scheme, uri.getUserInfo(), uri.getHost(), newPort, uri.getPath(),
                            uri.getQuery(), uri.getFragment());
                }
            }

            return uri;
        } catch (URISyntaxException ex) {
            throw new InvalidURIException("Invalid address specified", ex);
        }
    }

    /**
     * Parses the given string as a free-form address. This takes the form of a hostname and
     * optional port, followed by an optional password. If the input appears to contain a full
     * URI already, it is parsed by {@link #parseFromURI(java.lang.String)}.
     *
     * @param input The string to be parsed.
     * @return An equivalent URI, if one can be parsed.
     * @throws InvalidURIException If the string cannot be parsed as a URI, or an invalid component
     *         is specified.
     */
    public URI parseFromText(final String input) throws InvalidURIException {
        if (input.indexOf(' ') == -1 && input.contains("://")) {
            // Looks like a full URI, parse it as such.
            return parseFromURI(input);
        }

        boolean ssl = false;
        String host;
        String pass = null;
        int port = 6667;

        final String[] parts = input.split(" ", 2);

        // Check for port
        if (parts[0].indexOf(':') > -1) {
            final String[] portParts = parts[0].split(":");

            if (portParts.length < 2) {
                throw new InvalidURIException("Invalid port specified");
            }

            host = portParts[0];

            if (portParts[1].length() > 0 && portParts[1].charAt(0) == '+') {
                ssl = true;
                portParts[1] = portParts[1].substring(1);
            }

            try {
                port = Integer.parseInt(portParts[1]);
            } catch (NumberFormatException ex) {
                throw new InvalidURIException("Invalid port specified", ex);
            }

            if (port <= 0 || port > 65535) {
                throw new InvalidURIException("Invalid port specified",
                        new IllegalArgumentException("Port must be between 1 and 65535"));
            }
        } else {
            host = parts[0];
        }

        // Check for password
        if (parts.length > 1) {
            pass = parts[1];
        }

        try {
            return new URI("irc" + (ssl ? "s" : ""), pass, host, port, null, null, null);
        } catch (URISyntaxException ex) {
            throw new InvalidURIException("Invalid address specified", ex);
        }
    }

}
