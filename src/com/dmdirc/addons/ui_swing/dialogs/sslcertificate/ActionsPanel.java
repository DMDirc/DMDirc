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

import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateAction;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * SSL certificate actions.
 */
public class ActionsPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Radio button for temporarily accept. */
    private JRadioButton tempAccept;
    /** Radio button for permanently accept. */
    private JRadioButton permAccept;
    /** Radio button for do not connect. */
    private JRadioButton disconnect;
    /** Radio button group. */
    private ButtonGroup group;

    /**
     * Creates a new actions panel.
     */
    public ActionsPanel() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        tempAccept = new JRadioButton("Temporarily accept the problems with " +
                "this certificate and connect.");
        permAccept = new JRadioButton("Permanently accept the problems with " +
                "this certificate and connect.");
        disconnect = new JRadioButton("Do not connect.");
        group = new ButtonGroup();
        group.add(tempAccept);
        group.add(permAccept);
        group.add(disconnect);
        group.setSelected(tempAccept.getModel(), true);
    }

    private void layoutComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Actions"));
        setLayout(new MigLayout("fill, wrap 1"));

        add(tempAccept, "growx, pushx");
        add(permAccept, "growx, pushx");
        add(disconnect, "growx, pushx");
    }

    /**
     * Gets the action select for this chain.
     *
     * @return Selected action
     */
    public CertificateAction getAction() {
        if (group.getSelection().equals(tempAccept.getModel())) {
            return CertificateAction.IGNORE_TEMPORARILY;
        } else if (group.getSelection().equals(permAccept.getModel())) {
            return CertificateAction.IGNORE_PERMANENTY;
        } else {
            return CertificateAction.DISCONNECT;
        }
    }
}
