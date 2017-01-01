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

/**
 * Describes an entry in the summary of a certificate.
 *
 * @since 0.6.3m1
 */
public class CertificateSummaryEntry {

    /** The text of the summary. */
    private final String text;
    /** Whether the summary is good or not. */
    private final boolean good;

    /**
     * Creates a new summary.
     *
     * @param text The text of the summary
     * @param good Whether the summary is good or not
     */
    public CertificateSummaryEntry(final String text, final boolean good) {
        this.text = text;
        this.good = good;
    }

    /**
     * Determines whether this summary is good or bad.
     *
     * @return True if the summary is good, false otherwise
     */
    public boolean isGood() {
        return good;
    }

    /**
     * Retrieves the text of this summary.
     *
     * @return This summary's text
     */
    public String getText() {
        return text;
    }

}
