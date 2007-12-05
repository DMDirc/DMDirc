/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.ui.swing.JWrappingLabel;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.URLProtocolPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

/** URL Protocol dialog. */
public class URLDialog extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** A previously created instance of URLDialog. */
    private static URLDialog me;
    /** URL protocol config panel. */
    private URLProtocolPanel panel;
    /** URL. */
    private URI url;
    /** Blurb label. */
    private JWrappingLabel blurb;

    /**
     * Instantiates the URLDialog.
     *
     * @param url URL to open once added
     */
    private URLDialog(final URI url) {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        this.url = url;

        initComponents();
        layoutComponents();
        addListeners();

        setTitle("DMDirc: Unknown URL Protocol");

        pack();
    }

    /**
     * Creates the new URLDialog if one doesn't exist, and displays it.
     *
     * @param url URL to open once added
     */
    public static synchronized void showURLDialog(final URI url) {
        me = getURLDialog(url);

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the URLDialog.
     *
     * @param url URL to open once added
     * 
     * @return The current URLDialog instance
     */
    public static synchronized URLDialog getURLDialog(final URI url) {
        if (me == null) {
            me = new URLDialog(url);
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        blurb = new JWrappingLabel("Please select the appropriate action to " +
                "handle " + url.getScheme() + " URL protocols from the list " +
                "below.");
        panel = new URLProtocolPanel(url, false);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1"));

        add(blurb, "");
        add(panel, "grow");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            panel.save();
        dispose();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void validate() {
        super.validate();

        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
    }
}
