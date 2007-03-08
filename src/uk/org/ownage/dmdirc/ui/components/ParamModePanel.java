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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import uk.org.ownage.dmdirc.Config;

/**
 * A component to encapsulate one parameter-requiring channel mode, displaying
 * the user a checkbox, the mode's name, and a text field.
 * @author chris
 */
public class ParamModePanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The checkbox used in this mode panel */
    private JCheckBox checkBox;
    
    /** The textfield for the value of the mode */
    private JTextField textField;
    
    /**
     * Creates a new instance of ParamModePanel.
     * @param mode The mode that this panel should deal with
     * @param state The current state of the mode
     * @param value The current value of the mode
     */
    public ParamModePanel(String mode, boolean state, String value) {
        String text = "Mode "+mode;
        GridBagConstraints constraints = new GridBagConstraints();
        //constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(5,5,0,5));
        
        if (Config.hasOption("server","mode"+mode)) {
            text = Config.getOption("server", "mode"+mode);
        }
        
        checkBox = new JCheckBox(text, state);
        add(checkBox, constraints);
        
        constraints.anchor = GridBagConstraints.EAST;
        textField = new JTextField(value);
        textField.setColumns(10);
        add(textField, constraints);
        
        if (!state) {
            textField.setEnabled(false);
        }
        
        checkBox.addActionListener(this);
        checkBox.setBorder(new EmptyBorder(0,0,0,10));
    }
    
    /**
     * Called when our checkbox is toggled.
     * @param actionEvent associated action event
     */
    public void actionPerformed(ActionEvent actionEvent) {
        textField.setEnabled(checkBox.isSelected());
    }
    
}
