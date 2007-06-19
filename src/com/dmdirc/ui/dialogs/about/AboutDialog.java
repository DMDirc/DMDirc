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

package com.dmdirc.ui.dialogs.about;

import com.dmdirc.ui.MainFrame;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.components.StandardDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

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
    private static final long serialVersionUID = 4;
    
    /** Previously created instance of AboutDialog. */
    private static AboutDialog me;
    
    /** Creates a new instance of AboutDialog. */
    private AboutDialog() {
        super(MainFrame.getMainFrame(), false);
        initComponents();
        setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showAboutDialog() {
        if (me == null) {
            me = new AboutDialog();
        } else {
            me.setVisible(true);
            me.requestFocus();
        }
    }
    
    /** Initialises the main UI components. */
    private void initComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JTabbedPane tabbedPane = new JTabbedPane();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("About DMDirc");
        setResizable(false);
        
        orderButtons(new JButton(), new JButton());
        
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        
        tabbedPane.add("About", new AboutPanel());
        tabbedPane.add("Credits", new CreditsPanel());
        tabbedPane.add("License", new LicensePanel());
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(0, 0,
                0, 0);
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.insets = new Insets(0, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
        getContentPane().add(getOkButton(), constraints);
        
        pack();
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            dispose();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
}
