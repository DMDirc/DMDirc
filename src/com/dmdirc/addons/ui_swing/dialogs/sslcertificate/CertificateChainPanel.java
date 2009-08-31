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
import com.dmdirc.ui.core.dialogs.sslcertificate.CertificateChainEntry;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.renderers.CertificateChainEntryCellRenderer;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the certificate chain.
 */
public class CertificateChainPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Certificate chain list. */
    private List<CertificateChainEntry> certificateChain;
    /** Chain list. */
    private JList list;
    /** List model. */
    private DefaultListModel model;

    /**
     * Creates a new certificate chain panel.
     */
    public CertificateChainPanel() {
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        model = new DefaultListModel();
        list = new JList(model);
        list.setCellRenderer(new CertificateChainEntryCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new ListScroller(list);
    }

    private void layoutComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Certificate Chain"));
        setLayout(new MigLayout("fillx, wrap 1"));

        add(new JScrollPane(list), "grow, pushy");
        add(new JLabel("Certificate is trusted", IconManager.getIconManager().
                getIcon("tick"), JLabel.LEFT), "growx");
        add(new JLabel("Problem with certificate", IconManager.getIconManager().
                getIcon("cross"), JLabel.LEFT), "growx");
    }

    /**
     * Sets the chain for this certificate.
     *
     * @param certificateChain Certificate chain list
     */
    public void setChain(final List<CertificateChainEntry> certificateChain) {
        this.certificateChain = certificateChain;

        if (certificateChain == null) {
            model.clear();
        } else {
            for (CertificateChainEntry entry : certificateChain) {
                model.addElement(entry);
            }
        }
    }

    /**
     * Gets the name of the certificate at the specified point in the chain.
     *
     * @param index Index of certificate
     *
     * @return Name of specified index
     */
    public String getName(final int index) {
        return ((CertificateChainEntry) model.get(index)).getName();
    }

    /**
     * Gets the selected index.
     *
     * @return Selected index
     */
    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    /**
     * Sets the selected index.
     *
     * @param index Index to select
     */
    public void setSelectedIndex(final int index) {
        list.setSelectedIndex(index);
    }

    /**
     * Adds a list selection listener.
     *
     * @param listener Listener to add
     */
    public void addListSelectionListener(final ListSelectionListener listener) {
        list.addListSelectionListener(listener);
    }
}
