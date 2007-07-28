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

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionCondition;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.util.Arrays;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * Panel to display an alias
 */
public final class AliasPanel extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private final JTextField name;
    
    private final JTextField arguments;
    
    private final JTextArea response;
    
    /**
     * Creates a new instance of AliasPanel.
     *
     * @param alias Alias to display, or null
     */
    public AliasPanel(final Action alias) {
        super();
        
        name = new JTextField();
        arguments = new JTextField();
        response = new JTextArea();
        
        response.setRows(5);
        
        layoutComponents();
        
        setAlias(alias);
    }
    
    private void layoutComponents() {
        setLayout(new SpringLayout());
        
        add(new JLabel("Name: "));
        add(name);
        
        add(new JLabel("# Arguments: "));
        add(arguments);
        
        add(new JLabel("Mapping: "));
        add(new JScrollPane(response));
        
        layoutGrid(this, 3, 2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 
                SMALL_BORDER);
    }
    
    public void setAlias(final Action alias) {
        if (alias == null) {
            name.setText("");
            arguments.setText("");
            response.setText("");
        } else {
            name.setText(alias.getName());
            if (alias.getConditions().size() > 1) {
                final ActionCondition condition = alias.getConditions().get(1);
                arguments.setText(condition.getComparison().getName()
                + " " + condition.getTarget());
            } else {
                arguments.setText("N/A");
            }
            final String actionResponse = Arrays.toString(alias.getResponse());
            response.setText(actionResponse.substring(1, actionResponse.length() - 1));
        }
    }
    
}
