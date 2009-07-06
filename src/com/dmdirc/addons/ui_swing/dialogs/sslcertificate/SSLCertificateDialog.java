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

import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * SSL Certificate information dialog. Also provides the ability to accept and
 * reject certificates whilst connecting to an SSL server.
 */
public class SSLCertificateDialog extends StandardDialog implements ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** SSL Certificate dialog model. */
    private SSLCertificateDialogModel model;
    /** Panel listing actions that can be taken on a certificate. */
    private ActionsPanel actions;
    /** Panel showing the certificate chain of a certificate. */
    private CertificateChainPanel chain;
    /** Panel showing the information available for a certificate. */
    private CertificateInfoPanel info;
    /** Panel showing a summary of the certificate information. */
    private SummaryPanel summary;
    /** Informational blurb. */
    private TextLabel blurb;
    /** Parent window. */
    private Window parent;
    /** Selected index. */
    private int selectedIndex;

    /** 
     * Creates a new instance of ActionsManagerDialog.
     * 
     * @param parent Parent window for the dialog
     * @param model dialog model
     */
    public SSLCertificateDialog(final Window parent,
            final SSLCertificateDialogModel model) {
        super(parent, ModalityType.MODELESS);

        this.parent = parent;
        this.model = model;
        this.selectedIndex = 0;

        initComponents();
        addListeners();
        layoutComponents();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("DMDirc: Certificate Information");
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
        chain.setSelectedIndex(0);
    }

    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        chain.addListSelectionListener(this);
    }

    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        actions = new ActionsPanel();
        chain = new CertificateChainPanel();
        info = new CertificateInfoPanel();
        summary = new SummaryPanel();
        blurb = new TextLabel();

        chain.setChain(model.getCertificateChain());
        summary.setSummary(model.getSummary());

        actions.setVisible(model.needsResponse());
        if (model.needsResponse()) {
            blurb.setText("Theres is a problem with the certificate used by " +
                    model.getServerName());
        } else {
            blurb.setText("Your connection to " + model.getServerName() +
                    " is encrypted using SSL.");
        }
    }

    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 2, wmin 600, wmax 600, hmax 600, hidemode 3, pack"));

        add(blurb, "span 2");
        add(chain, "wmax 250, grow");
        add(info, "growx, pushx");
        add(summary, "span 2, growx");
        add(actions, "span 2, growx");
        add(getOkButton(), "span, right");
    }

    /** 
     * {@inheritDoc}.
     * 
     * @param e Action event    
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (!e.getSource().equals(getCancelButton())) {
            model.performAction(actions.getAction());
            dispose();
        }
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            final int index = chain.getSelectedIndex();
            if (index == -1) {
                chain.setSelectedIndex(selectedIndex);
            } else {
                info.setInfo(chain.getName(index),
                        model.getCertificateInfo(index));
                selectedIndex = index;
            }
        }
    }
}
