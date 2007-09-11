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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.layoutGrid;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.swing.components.ColourChooser;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * New nick colour input dialog.
 */
public class NewNickColourInputDialog extends StandardDialog
        implements ActionListener {
    
    /** Buttons panel. */
    private JPanel buttonsPanel;
    /** content panel. */
    private JPanel contentPanel;
    
    /** nickname textfield. */
    private JTextField nickname;
    /** network textfield. */
    private JTextField network;
    /** text colour input. */
    private ColourChooser textColour;
    /** nicklist colour input. */
    private ColourChooser nicklistColour;
    
    /** Creates a new instance of NewNickColourInputDialog. */
    public NewNickColourInputDialog() {
        //super((MainFrame) Main.getUI().getMainWindow(), false);
        super(null, false);
        
        initComponents();
        initListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        contentPanel = new JPanel();
        buttonsPanel = new JPanel();
        
        orderButtons(new JButton(), new JButton());
        
        nickname = new JTextField();
        network = new JTextField();
        textColour = new ColourChooser("", true, true);
        nicklistColour = new ColourChooser("", true, true);
        
        nickname.setPreferredSize(new Dimension(10,
                nickname.getFont().getSize() - SMALL_BORDER));
        network.setPreferredSize(new Dimension(10,
                network.getFont().getSize() - SMALL_BORDER));
        textColour.setPreferredSize(new Dimension(10,
                textColour.getFont().getSize() - SMALL_BORDER));
        nicklistColour.setPreferredSize(new Dimension(10,
                nicklistColour.getFont().getSize() - SMALL_BORDER));
    }
    
    /** Initialises the content panel. */
    private void layoutContentPanel() {
        contentPanel.setLayout(new SpringLayout());
        
        contentPanel.add(new JLabel("Nickname: "));
        contentPanel.add(nickname);
        
        contentPanel.add(new JLabel("Network: "));
        contentPanel.add(network);
        
        contentPanel.add(new JLabel("Text colour: "));
        contentPanel.add(textColour);
        
        contentPanel.add(new JLabel("Nicklist colour: "));
        contentPanel.add(nicklistColour);
        
        layoutGrid(contentPanel, 4, 2, SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Initialises the button panel. */
    private void layoutButtonsPanel() {
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(getRightButton());
    }
    
    /** Initialises the listeners. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /** Lays out the components. */
    private void layoutComponents() {
        layoutContentPanel();
        layoutButtonsPanel();
        
        setLayout(new BorderLayout());
        
        add(contentPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.PAGE_END);
        
        pack();
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            saveSettings();
            dispose();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }
    
    /** Saves settings. */
    public void saveSettings() {
        //do stuff.
    }
    
    public static void main(final String[] args) {
        new NewNickColourInputDialog().setVisible(true);
    }
    
}
