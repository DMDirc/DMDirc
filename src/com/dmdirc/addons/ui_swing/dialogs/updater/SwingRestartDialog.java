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

package com.dmdirc.addons.ui_swing.dialogs.updater;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.StandardDialog;
import com.dmdirc.addons.ui_swing.components.TextLabel;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

/**
 * Prompts the user to restart the client, and restarts the client.
 */
public class SwingRestartDialog extends StandardDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -7446499281414990074L;
    /** Informational label. */
    private TextLabel info;
    /** Swing controller. */
    private SwingController controller;
    

    /**
     * Dialog to restart the client.
     * 
     * @param controller Swing controller
     * @param modal Modality
     */
    public SwingRestartDialog(final SwingController controller, final ModalityType modal) {
        super(controller.getMainFrame(), modal);
        this.controller = controller;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initComponents();
        layoutComponents();
        
        pack();
        setLocationRelativeTo(controller.getMainFrame());
    }
    
    /** Initialise components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        info = new TextLabel("Your client needs to be restart to finish updating.");
        getOkButton().setText("Now");
        getCancelButton().setText("Later");
        
        getOkButton().addActionListener(this);  
        getCancelButton().addActionListener(this);
    }
    
    /** Layout Components. */
    public void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 2"));
        
        add(info, "grow, pushy, span 2");
        add(getLeftButton(), "split, right");
        add(getRightButton(), "right");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (getOkButton().equals(e.getSource())) {
            controller.getMainFrame().quit(42);
        }
        dispose();
    }
    
}