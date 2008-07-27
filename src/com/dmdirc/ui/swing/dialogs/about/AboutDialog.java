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

package com.dmdirc.ui.swing.dialogs.about;

import com.dmdirc.Main;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * About dialog.
 */
public final class AboutDialog extends StandardDialog implements
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Previously created instance of AboutDialog. */
    private static volatile AboutDialog me = null;

    /** Creates a new instance of AboutDialog. */
    private AboutDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        initComponents();
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static void showAboutDialog() {
        me = getAboutDialog();

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the AboutDialog.
     *
     * @return The current AboutDialog instance
     */
    public static AboutDialog getAboutDialog() {
        synchronized (AboutDialog.class) {
            if (me == null) {
                me = new AboutDialog();
            }
        }

        return me;
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About DMDirc");
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        tabbedPane.add("About", new AboutPanel());
        tabbedPane.add("Credits", new CreditsPanel());
        tabbedPane.add("License", new LicensePanel());
        tabbedPane.add("Information", new InfoPanel());

        getContentPane().setLayout(new MigLayout("ins rel, wrap 1, fill, wmax 550, hmax 300"));
        getContentPane().add(tabbedPane, "grow");
        getContentPane().add(getOkButton(), "right");

        pack();
    }

    /** 
     * {@inheritDoc}.
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        dispose();
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
