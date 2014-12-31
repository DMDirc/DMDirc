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

package com.dmdirc.ui.core.dialogs.sslcertificate;

/**
 * Describes one piece of information about a certificate.
 *
 * @since 0.6.3m1
 */
public class CertificateInformationEntry {

    /** The title of the piece of information. */
    private final String title;
    /** The actual value. */
    private final String value;
    /** Whether this value is invalid. */
    private final boolean invalid;
    /** Whether this value is missing. */
    private final boolean missing;

    /**
     * Creates a new CertificateInformationEntry for the specified information.
     *
     * @param title   The title of the piece of information
     * @param value   The actual value
     * @param invalid Whether the value is invalid
     * @param missing Whether the value is missing
     */
    public CertificateInformationEntry(final String title, final String value,
            final boolean invalid, final boolean missing) {
        this.title = title;
        this.value = value;
        this.invalid = invalid;
        this.missing = missing;
    }

    /**
     * Checks whether the value is considered invalid.
     *
     * @return True if the value is invalid, false otherwise
     */
    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Checks whether the value is considered missing.
     *
     * @return True if the value is missing, false otherwise
     */
    public boolean isMissing() {
        return missing;
    }

    /**
     * Retrieves the title of this piece of information.
     *
     * @return This entry's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the value of this entry.
     *
     * @return This entry's value
     */
    public String getValue() {
        return value;
    }

}
