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

package com.dmdirc.ui.swing.dialogs.aliases;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.CoreActionComparison;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 * Panel to display an alias
 */
public final class AliasPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private final JTextField name;
    
    private final JComboBox argumentComponent;
    
    private final JSpinner argumentNumber;
    
    private final JTextArea response;
    
    /** Creates a new instance of AliasPanel. */
    public AliasPanel() {
        super();
        
        name = new JTextField();
        name.setEnabled(false);
        
        argumentComponent = new JComboBox(new String[]{"N/A", ">", "==", "<", });
        argumentNumber = new JSpinner(new SpinnerNumberModel());
        response = new JTextArea();
        
        argumentNumber.setEnabled(false);
        response.setRows(5);
        
        argumentComponent.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        argumentComponent.addActionListener(this);
        
        layoutComponents();
        
        clear();
    }
    
    /** Lays out and initialises the components. */
    private void layoutComponents() {
        final JPanel panel = new JPanel(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        name.setPreferredSize(new Dimension(0, getFont().getSize()));
        argumentComponent.setPreferredSize(new Dimension(
                12 * getFont().getSize(), getFont().getSize()));
        argumentNumber.setPreferredSize(new Dimension(0, getFont().getSize()));
        response.setPreferredSize(new Dimension(0, getFont().getSize()));
        
        panel.add(argumentComponent, BorderLayout.LINE_START);
        panel.add(argumentNumber, BorderLayout.CENTER);
        
        setLayout(new SpringLayout());
        
        add(new JLabel("Name: "));
        add(name);
        
        add(new JLabel("# Arguments: "));
        add(panel);
        
        add(new JLabel("Mapping: "));
        add(new JScrollPane(response));
        
        layoutGrid(this, 3, 2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER);
    }
    
    /** Clears the details. */
    public void clear() {
        name.setText("");
        argumentComponent.setSelectedItem("N/A");
        argumentNumber.setValue(0);
        response.setText("");
        name.setEnabled(false);
        argumentComponent.setEnabled(false);
        argumentNumber.setEnabled(false);
        response.setEnabled(false);
    }
    
    /**
     * Sets the alias details.
     *
     * @param alias List of alias details to display
     */
    public void setAlias(final Alias alias) {
        if (alias == null) {
            clear();
        }
        name.setEnabled(false);
        argumentComponent.setEnabled(true);
        response.setEnabled(true);
        name.setText(alias.getName());
        
        final List<ActionCondition> arguments = alias.getArguments();
        ActionCondition argument;
        
        if (arguments.size() == 1) {
            argumentNumber.setValue(0);
            argumentNumber.setEnabled(false);
        } else {
            argument = arguments.get(0);
            
            if (argument.getComparison() == CoreActionComparison.STRING_EQUALS) {
                argument = arguments.get(1);
            }
            argumentComponent.setSelectedItem(argument.getComparison().getName());
            argumentNumber.setValue(Integer.parseInt(argument.getTarget()));
            argumentNumber.setEnabled(true);
        }
        
        final StringBuffer sb = new StringBuffer();
        for (String line : alias.getResponse()) {
            sb.append(line).append('\n');
        }
        response.setText(sb.substring(0, sb.length() - 1));
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (argumentComponent.getSelectedIndex() > 0) {
            argumentNumber.setEnabled(true);
        } else {
            argumentNumber.setEnabled(false);
        }
    }
    
    /**
     * Returns the user response to the alias.
     *
     * @return Alias response
     */
    protected String[] getResponse() {
        return response.getText().split("\n");
    }
}
