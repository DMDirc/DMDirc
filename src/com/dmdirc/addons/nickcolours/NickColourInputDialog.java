/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.ui.swing.MainFrame;
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
import javax.swing.WindowConstants;

/**
 * New nick colour input dialog.
 */
public class NickColourInputDialog extends StandardDialog
        implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Whether or not this is a new entry (as opposed to editing an old one). */
    private boolean isnew;
    /** The row we're editing, if this isn't a new entry. */
    private int row;
    
    /** The NickColourPanel we're reporting to. */
    private final NickColourPanel panel;
    
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
    
    /**
     * Creates a new instance of NickColourInputDialog.
     *
     * @param panel The panel that's opening this dialog
     * @param row The row of the table we're editing
     * @param nickname The nickname that's currently set
     * @param network The network that's currently set
     * @param textcolour The text colour that's currently set
     * @param nickcolour The nicklist colour that's currently set
     */
    public NickColourInputDialog(final NickColourPanel panel, final int row,
            final String nickname, final String network,
            final String textcolour, final String nickcolour) {
        super((MainFrame) Main.getUI().getMainWindow(), false);
        
        this.panel = panel;
        this.row = row;
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        initComponents(nickname, network, textcolour, nickcolour);
        initListeners();
        layoutComponents();
        
        setTitle("Nick colour editor");
        
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        setVisible(true);
    }
    
    /**
     * Creates a new instance of NickColourInputDialog.
     *
     * @param panel The panel that's opening this dialog
     */
    public NickColourInputDialog(final NickColourPanel panel) {
        this(panel, -1, "", "", "", "");
        
        isnew = true;
    }
    
    /**
     * Initialises the components.
     *
     * @param defaultNickname The default value for the nickname text field
     * @param defaultNetwork The default value for the network text field
     * @param defaultTextColour The default value for the text colour option
     * @param defaultNickColour The default value for the nick colour option
     */
    private void initComponents(final String defaultNickname,
            final String defaultNetwork, final String defaultTextColour,
            final String defaultNickColour) {
        contentPanel = new JPanel();
        buttonsPanel = new JPanel();
        
        orderButtons(new JButton(), new JButton());
        
        nickname = new JTextField(defaultNickname);
        network = new JTextField(defaultNetwork);
        textColour = new ColourChooser(defaultTextColour, true, true);
        nicklistColour = new ColourChooser(defaultNickColour, true, true);
        
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
        final String myNetwork = network.getText().toLowerCase();
        final String myNickname = nickname.getText().toLowerCase();
        final String myTextColour = textColour.getColour();
        final String myNickColour = nicklistColour.getColour();
        
        if (!isnew) {
            panel.removeRow(row);
        }
        
        panel.addRow(myNetwork, myNickname, myTextColour, myNickColour);
    }
    
}
