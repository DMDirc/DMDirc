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

package uk.org.ownage.dmdirc.ui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Profiel editing dialog.
 */
public class ProfileEditorDialog extends StandardDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Creates a new instance of ProfileEditorDialog. */
    public ProfileEditorDialog() {
        super(MainFrame.getMainFrame(), false);
        
        initComponents();
        
        layoutComponents();
        
        addCallbacks();     
        
        this.setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }

    /** Initialises the components of the dialog. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        setTitle("Profile Editor");
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.insets.set(LARGE_BORDER, 0, LARGE_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.gridx = 2;
        getContentPane().add(getRightButton(), constraints);
        
        pack();
    }

    /** Adds listeners to various components in the dialog. */
    private void addCallbacks() {
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);
    }

    /** {@inheritDoc}. */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == getOkButton()) {
            this.dispose();
        } else if (event.getSource() == getCancelButton()) {
            this.dispose();
        }
    }
    
}
