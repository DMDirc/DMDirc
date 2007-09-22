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
import com.dmdirc.actions.CoreActionComponent;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 * Panel to display an alias.
 */
public final class AliasPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Name field. */
    private final JTextField command;
    
    /** argument component combo box. */
    private final JComboBox argumentComponent;
    
    /** Argument number spinner. */
    private final JSpinner argumentNumber;
    
    /** Response field. */
    private final JTextArea response;
    
    /** Alias. */
    private Alias alias;
    
    /** Creates a new instance of AliasPanel. */
    public AliasPanel() {
        super();
        
        command = new JTextField();
        command.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.
                getKeyStroke(new Character(' '), 0), "none");
        command.setEnabled(false);
        
        argumentComponent = new JComboBox(new CoreActionComparison[]{null,
        CoreActionComparison.INT_GREATER, CoreActionComparison.INT_EQUALS,
        CoreActionComparison.INT_LESS, });
        argumentNumber = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        response = new JTextArea();
        
        argumentNumber.setEnabled(false);
        response.setRows(5);
        
        argumentComponent.setRenderer(new ActionComparisonCellRenderer());
        argumentComponent.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        argumentComponent.addActionListener(this);
        
        layoutComponents();
        
        clear();
    }
    
    /** Lays out and initialises the components. */
    private void layoutComponents() {
        final JPanel panel = new JPanel(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        command.setPreferredSize(new Dimension(0, getFont().getSize()));
        argumentComponent.setPreferredSize(new Dimension(
                12 * getFont().getSize(), getFont().getSize()));
        argumentNumber.setPreferredSize(new Dimension(0, getFont().getSize()));
        response.setPreferredSize(new Dimension(0, getFont().getSize()));
        
        panel.add(argumentComponent, BorderLayout.LINE_START);
        panel.add(argumentNumber, BorderLayout.CENTER);
        
        setLayout(new SpringLayout());
        
        add(new JLabel("Command: "));
        add(command);
        
        add(new JLabel("# Arguments: "));
        add(panel);
        
        add(new JLabel("Response: "));
        add(new JScrollPane(response));
        
        layoutGrid(this, 3, 2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER);
    }
    
    /** Clears the details. */
    public void clear() {
        alias = null;
        command.setText("");
        command.setEnabled(false);
        argumentComponent.setSelectedItem(null);
        argumentNumber.setValue(0);
        response.setText("");
        command.setEnabled(false);
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
            return;
        }
        this.alias = alias;
        command.setEnabled(true);
        argumentComponent.setEnabled(true);
        response.setEnabled(true);
        command.setText(alias.getCommand());
        
        final List<ActionCondition> arguments = alias.getArguments();
        ActionCondition argument;
        
        if (arguments.size() == 1) {
            argumentComponent.setSelectedItem(null);
            argumentNumber.setValue(0);
            argumentNumber.setEnabled(false);
        } else {
            argument = arguments.get(0);
            
            if (argument.getComparison() == CoreActionComparison.STRING_EQUALS) {
                argument = arguments.get(1);
            }
            argumentComponent.setSelectedItem(argument.getComparison());
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
        ((SpinnerNumberModel) argumentNumber.getModel()).setMinimum(0);
        if (argumentComponent.getSelectedIndex() > 0) {
            argumentNumber.setEnabled(true);
            if (argumentComponent.getSelectedItem() == CoreActionComparison.INT_LESS) {
                if (argumentNumber.getModel().getValue().equals(0)) {
                    argumentNumber.getModel().setValue(1);
                }
                ((SpinnerNumberModel) argumentNumber.getModel()).setMinimum(1);
            }
        } else {
            argumentNumber.setEnabled(false);
        }
    }
    
    /**
     * Returns the current command.
     *
     * @return Alias command
     */
    public String getCommand() {
        return command.getText();
    }
    
    /**
     * Returns the arguments condition.
     *
     * @return Action argument condition
     */
    public ActionCondition getArguments() {
        if (argumentComponent.getSelectedItem() == null) {
            return null;
        }
        switch ((CoreActionComparison) argumentComponent.getSelectedItem()) {
            case INT_EQUALS:
                return new ActionCondition(2,
                        CoreActionComponent.STRINGARRAY_LENGTH,
                        CoreActionComparison.INT_EQUALS,
                        argumentNumber.getValue().toString());
            case INT_GREATER:
                return new ActionCondition(2,
                        CoreActionComponent.STRINGARRAY_LENGTH,
                        CoreActionComparison.INT_EQUALS,
                        argumentNumber.getValue().toString());
            case INT_LESS:
                return new ActionCondition(2,
                        CoreActionComponent.STRINGARRAY_LENGTH,
                        CoreActionComparison.INT_EQUALS,
                        argumentNumber.getValue().toString());
            default:
                return null;
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
    
    /**
     * Returns the alias being shown in this panel.
     *
     * @return Alias
     */
    protected Alias getAlias() {
        return alias;
    }
}
