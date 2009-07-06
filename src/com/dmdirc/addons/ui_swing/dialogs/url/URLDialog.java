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

package com.dmdirc.addons.ui_swing.dialogs.url;

import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.components.URLProtocolPanel;
import com.dmdirc.util.URLHandler;

import java.awt.Window;
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
    private static volatile URLDialog me;
    /** URL protocol config panel. */
    private URLProtocolPanel panel;
    /** URL. */
    private URI url;
    /** Blurb label. */
    private TextLabel blurb;
    /** Swing controller. */
    private Window parentWindow;

    /**
     * Instantiates the URLDialog.
     *
     * @param url URL to open once added
     * @param parentWindow Parent window
     */
    private URLDialog(final URI url, final Window parentWindow) {
        super(parentWindow, ModalityType.MODELESS);
        
        this.url = url;
        this.parentWindow = parentWindow;
        
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
     * @param parentWindow Parent window
     */
    public static void showURLDialog(final URI url, final Window parentWindow) {
        me = getURLDialog(url, parentWindow);

        me.setLocationRelativeTo(parentWindow);
        me.setVisible(true);
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the URLDialog.
     *
     * @param url URL to open once added
     * @param parentWindow Parent window
     * 
     * @return The current URLDialog instance
     */
    public static URLDialog getURLDialog(final URI url, final Window parentWindow) {
        synchronized (URLDialog.class) {
            if (me == null) {
                me = new URLDialog(url, parentWindow);
            }
        }

        return me;
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        blurb = new TextLabel("Please select the appropriate action to " +
                "handle " + url.getScheme() + " URL protocols from the list " +
                "below.");
        panel = new URLProtocolPanel(url, false);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, pack"));

        add(blurb, "");
        add(panel, "grow, push");
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
            URLHandler.getURLHander().launchApp(url);
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void validate() {
        super.validate();

        setLocationRelativeTo(parentWindow);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
