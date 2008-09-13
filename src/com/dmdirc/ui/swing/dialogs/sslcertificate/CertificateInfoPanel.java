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

package com.dmdirc.ui.swing.dialogs.sslcertificate;

import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateInformationEntry;
import com.dmdirc.ui.swing.components.TextLabel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 */
public class CertificateInfoPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Certificate info list. */
    private List<List<CertificateInformationEntry>> certificateInfo;

    public CertificateInfoPanel() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        certificateInfo = new ArrayList<List<CertificateInformationEntry>>();
    }

    private void layoutComponents() {
        setVisible(false);
        removeAll();
        setBorder(BorderFactory.createTitledBorder("Information for " + ""));
        setLayout(new MigLayout("fill, wrap 2"));

        for (List<CertificateInformationEntry> entry : certificateInfo) {
            for (CertificateInformationEntry info : entry) {
                add(new JLabel(info.getTitle()), "align right");
                final TextLabel text = new TextLabel(info.getValue());
                if (info.isInvalid()) {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setForeground(sas, Color.RED);
                    text.getDocument().setParagraphAttributes(0, info.getValue().
                            length(), sas, true);
                }
                add(text, "grow");
            }
            add(new JLabel(), "spanx");
        }
        setVisible(true);
    }

    public void setInfo(final List<List<CertificateInformationEntry>> certificateInfo) {
        this.certificateInfo = certificateInfo;

        if (certificateInfo == null) {
            this.certificateInfo =
                    new ArrayList<List<CertificateInformationEntry>>();
        }
        layoutComponents();
    }
}
