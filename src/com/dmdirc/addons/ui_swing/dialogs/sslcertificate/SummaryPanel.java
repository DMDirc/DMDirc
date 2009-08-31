/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.sslcertificate;

import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateSummaryEntry;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/**
 * Displays a summary about the certificate chain.
 */
public class SummaryPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** List of certificate summary entries. */
    private List<CertificateSummaryEntry> summary;

    /**
     * Creates a Certificate chain summary panel.
     */
    public SummaryPanel() {
        summary = new ArrayList<CertificateSummaryEntry>();
        layoutComponents();
    }

    private void layoutComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Summary"));
        setLayout(new MigLayout("fill, wrap 1"));

        for (CertificateSummaryEntry entry : summary) {
            add(new JLabel(entry.getText(),
                    entry.isGood() ? IconManager.getIconManager().getIcon("tick")
                    : IconManager.getIconManager().getIcon("cross"), JLabel.LEFT),
                    "growx");
        }
    }

    /**
     * Sets the summary for this panel.
     *
     * @param summary Summary to show
     */
    void setSummary(final List<CertificateSummaryEntry> summary) {
        this.summary = summary;
        layoutComponents();
    }
}
