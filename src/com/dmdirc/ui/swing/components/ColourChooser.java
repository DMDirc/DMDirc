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

package com.dmdirc.ui.swing.components;

import com.dmdirc.ui.messages.ColourManager;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

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
    
    /** Edit button. */
    private JButton editButton;
    
    /** Panel to show the colour preview. */
    private JPanel previewPanel;
    
    /** Colours picking dialog. */
    private ColourPickerDialog cpd;
    
    /** show irc colours. */
    private boolean showIRC;
    
    /** show hex colours. */
    private boolean showHex;
    
    /** The value of this component. */
    private String value;
    
    /** Event listeners. */
    private EventListenerList listeners;
    
    /** Action command. */
    private String command;
    
    /** Creates a new instance of ColourChooser. */
    public ColourChooser() {
        this("ffffff", true, true);
    }
    
    /**
     * Creates a new instance of ColourChooser.
     * @param initialColour initial colour
     * @param ircColours show irc colours
     * @param hexColours show hex colours
     */
    public ColourChooser(final String initialColour, final boolean ircColours,
            final boolean hexColours) {
        super();
        
        showIRC = ircColours;
        showHex = hexColours;
        value = initialColour;
        listeners = new EventListenerList();
        command = "";
        
        editButton = new JButton("Edit");
        editButton.setMargin(new Insets(0, 2, 0, 2));
        
        editButton.addActionListener(this);
        
        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(40, 10));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        setLayout(new BorderLayout(SMALL_BORDER, SMALL_BORDER));
        
        add(editButton, BorderLayout.LINE_END);
        add(previewPanel, BorderLayout.CENTER);
        
        updateColour(initialColour);
    }
    
    /**
     * Returns the selected colour from this component.
     * @return This components colour, as a string
     */
    public String getColour() {
        return value;
    }
    
    /**
     * Sets the selected colour for this component.
     * @param newValue New colour
     */
    public void setColour(final String newValue) {
        value = newValue;
        updateColour(value);
    }
    
    /** Sets the colour back to white. */
    public void clearColour() {
        value = "ffffff";
        previewPanel.setBackground(ColourManager.getColour("ffffff"));
        previewPanel.setToolTipText("");
    }
    
    /**
     * Updates the colour panel.
     * @param newColour The new colour to use.
     */
    private void updateColour(final String newColour) {
        if (newColour == null || newColour.isEmpty()) {
            previewPanel.setBackground(ColourManager.getColour("ffffff"));
            previewPanel.setToolTipText("");
        } else {
            previewPanel.setBackground(ColourManager.parseColour(newColour));
            previewPanel.setToolTipText(newColour);
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == editButton) {
            cpd = new ColourPickerDialog(showIRC, showHex);
            cpd.setLocationRelativeTo(editButton);
            cpd.addActionListener(this);
            cpd.setVisible(true);
        } else {
            value = e.getActionCommand();
            updateColour(e.getActionCommand());
            fireActionPerformed();
            cpd.dispose();
        }
    }
    
    /**
     * Sets this colour choosers action command.
     *
     * @param command New action command
     */
    public void setActionCommand(final String command) {
        this.command = command;
    }
    
    /**
     * Adds a ActionListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addActionListener(final ActionListener listener) {
        synchronized (listeners) {
            if (listener == null) {
                return;
            }
            listeners.add(ActionListener.class, listener);
        }
    }
    
    /**
     * Removes a ActionListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeActionListener(final ActionListener listener) {
        listeners.remove(ActionListener.class, listener);
    }
    
    /**
     * Fires the action performed method on all listeners.
     */
    protected void fireActionPerformed() {
        final Object[] listenerList = listeners.getListenerList();
        for (int i = 0; i < listenerList.length; i += 2) {
            if (listenerList[i] == ActionListener.class) {
                ((ActionListener) listenerList[i + 1]).actionPerformed(
                        new ActionEvent((Object) this, 
                        ActionEvent.ACTION_PERFORMED, command));
            }
        }
    }
}
