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

package com.dmdirc.ui.dialogs.error;

import com.dmdirc.logger.ProgramError;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.layoutGrid;
import com.dmdirc.ui.textpane.TextPane;
import java.text.AttributedString;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

/**
 * Shows information about an error.
 */
public final class ErrorDetailPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Error to show. */
    private ProgramError error;
    
    /** ID field. */
    private JTextField id;
    
    /** Date field. */
    private JTextField date;
    
    /** Severity field. */
    private JTextField level;
    
    /** Status field. */
    private JTextField status;
    
    /** Details field. */
    private TextPane details;
    
    /** Creates a new instance of ErrorDetailPanel. */
    public ErrorDetailPanel() {
        this(null);
    }
    
    /**
     * Creates a new instance of ErrorDetailPanel.
     *
     * @param error Error to be displayed
     */
    public ErrorDetailPanel(final ProgramError error) {
        super();
        
        this.error = error;
        
        initComponents();
        
        updateDetails();
        
        layoutComponents();
    }
    
    /**
     * Sets the error used for this panel.
     *
     * @param newError New ProgramError
     */
    public void setError(final ProgramError newError) {
        error = newError;
        updateDetails();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        id = new JTextField();
        date = new JTextField();
        level = new JTextField();
        status = new JTextField();
        details = new TextPane(null);
        
        id.setEditable(false);
        date.setEditable(false);
        level.setEditable(false);
        status.setEditable(false);
    }
    
    /** Updates the panels details. */
    private void updateDetails() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (error == null) {
                    id.setText("");
                    date.setText("");
                    level.setText("");
                    status.setText("");
                    details.clear();
                    
                    return;
                }
                
                id.setText(String.valueOf(error.getID()));
                date.setText(error.getDate().toString());
                level.setText(error.getLevel().toString());
                status.setText(error.getStatus().toString());
                
                details.addText(new AttributedString(error.getMessage()));
                final String[] trace = error.getTrace();
                for (String traceLine : trace) {
                    details.addText(new AttributedString(traceLine));
                }
                
                details.setScrollBarPosition(0);
            }
        });
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new SpringLayout());
        
        JLabel label;
        
        label = new JLabel("ID: ");
        add(label);
        add(id);
        
        label = new JLabel("Date: ");
        add(label);
        add(date);
        
        label = new JLabel("Severity: ");
        add(label);
        add(level);
        
        label = new JLabel("Report status: ");
        add(label);
        add(status);
        
        label = new JLabel("Details: ");
        add(label);
        add(details);
        
        layoutGrid(this, 5, 2, 0, 0, SMALL_BORDER, SMALL_BORDER);
    }
    
}
