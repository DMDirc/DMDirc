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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Colour chooser widget.
 */
public final class ColourChooser extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Text field showing current colour. */
    private JTextField textField;
    
    /** Edit button. */
    private JButton editButton;
    
    private ColourPickerDialog cpd;
    
    private boolean showIRC;
    
    private boolean showHex;
    
    /** Creates a new instance of ColourChooser. */
    public ColourChooser() {
        this("", true, true);
    }
    
    /**
     * Creates a new instance of ColourChooser.
     * @param initialColour initial colour
     */
    public ColourChooser(final String initialColour, boolean ircColours, boolean hexColours) {
        showIRC = ircColours;
        showHex = hexColours;
        textField = new JTextField(initialColour);
        
        editButton = new JButton("Edit");
        editButton.setMargin(new Insets(0, 2, 0, 2));
        
        editButton.addActionListener(this);
        
        this.setLayout(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        this.add(textField, BorderLayout.CENTER);
        this.add(editButton, BorderLayout.LINE_END);
    }
    
    /**
     * Returns the selected colour from this component.
     * @return This components colour, as a string
     */
    public String getColour() {
        return textField.getText();
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == editButton) {
            cpd = ColourPickerDialog.showColourPicker(showIRC, showHex);
            cpd.addActionListener(this);
        } else {
            textField.setText(e.getActionCommand());
            cpd.dispose();
        }
    }
}
