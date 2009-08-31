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

import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateInformationEntry;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the certificate info panel. Listing various informational 
 * snippets about a certificate.
 */
public class CertificateInfoPanel extends JScrollPane {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Certificate info list. */
    private List<List<CertificateInformationEntry>> certificateInfo;
    /** Certificate name. */
    private String certificateName;
    /** Content panel. */
    private JPanel content;

    /**
     * Creates a certificate info panel.
     */
    public CertificateInfoPanel() {
        initComponents();
        layoutComponents();
        setViewportView(content);
    }

    private void initComponents() {
        content = new JPanel();
        certificateInfo = new ArrayList<List<CertificateInformationEntry>>();
    }

    private void layoutComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Information for " +
                certificateName));
        content.setVisible(false);
        content.removeAll();
        content.setLayout(new MigLayout("fillx, wmax 100%, wrap 2, pack"));

        int i = 1;
        for (List<CertificateInformationEntry> entry : certificateInfo) {
            for (CertificateInformationEntry info : entry) {
                content.add(new TextLabel(info.getTitle() + ": "),
                        "alignx right");
                final TextLabel text = new TextLabel(info.getValue(), false);
                if (info.isInvalid()) {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setForeground(sas, Color.RED);
                    text.getDocument().setParagraphAttributes(0, info.getValue().
                            length(), sas, true);
                }
                if (info.isMissing()) {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setItalic(sas, true);
                    text.getDocument().setParagraphAttributes(0, info.getValue().
                            length(), sas, true);
                }
                content.add(text, "growx, pushx");
            }
            if (i < certificateInfo.size()) {
                content.add(new JLabel(), "spanx, gaptop 2*unrel");
            }
            i++;
        }
        content.setVisible(true);
    }

    /**
     * Sets the certificate info lists.
     *
     * @param certificateName Certificate name
     * @param certificateInfo Certificate info list
     */
    public void setInfo(final String certificateName,
            final List<List<CertificateInformationEntry>> certificateInfo) {
        this.certificateInfo = certificateInfo;
        this.certificateName = certificateName;

        if (certificateInfo == null) {
            this.certificateInfo =
                    new ArrayList<List<CertificateInformationEntry>>();
        }
        layoutComponents();
    }
}
