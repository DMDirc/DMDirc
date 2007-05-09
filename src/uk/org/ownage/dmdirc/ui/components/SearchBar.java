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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import static uk.org.ownage.dmdirc.ui.UIUtilities.*;

/**
 * Status bar, shows message and info on the gui.
 */
public final class SearchBar extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Close button. */
    private JButton closeButton;
    
    /** Search button. */
    private JButton searchButton;
    
    /** Next match button. */
    private JButton nextButton;
    
    /** Previous match button. */
    private JButton prevButton;
    
    /** Case sensitive checkbox. */
    private JCheckBox caseCheck;
    
    /** Search text field. */
    private JTextField searchBox;
    
    /** Frame parent. */
    private final Frame parent;
    
    /**
     * Creates a new instance of StatusBar.
     * @param newParent parent frame for the dialog
     */
    public SearchBar(final Frame newParent) {
        super();
        
        this.parent = newParent;
        
        initComponents();
        layoutComponents();
        addListeners();
    }
    
    /** Initialises components. */
    private void initComponents() {
        closeButton = new JButton();
        searchButton = new JButton();
        nextButton = new JButton();
        prevButton = new JButton();
        caseCheck = new JCheckBox();
        searchBox = new JTextField();
        
        searchButton.setText("Search");
        nextButton.setText("Next");
        prevButton.setText("Prev");
        caseCheck.setText("Case sensitive");
        
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setPreferredSize(new Dimension(50, 0));
        
        searchButton.setMargin(new Insets(0, 0, 0, 0));
        searchButton.setPreferredSize(new Dimension(50, 0));
        
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setPreferredSize(new Dimension(50, 0));
        
        prevButton.setMargin(new Insets(0, 0, 0, 0));
        prevButton.setPreferredSize(new Dimension(50, 0));
        
        caseCheck.setPreferredSize(new Dimension(110, 0));
        
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                searchBox.getBorder(), new EmptyBorder(2, 2, 2, 2)));
        
        searchBox.setPreferredSize(new Dimension(300, searchBox.getFont().getSize()));
        
        closeButton.setIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-inactive.png")));
        closeButton.setRolloverIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        closeButton.setPressedIcon(new ImageIcon(this.getClass()
        .getClassLoader().getResource("uk/org/ownage/dmdirc/res/close-active.png")));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        closeButton.setPreferredSize(new Dimension(16, 16));
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        this.setLayout(new SpringLayout());
        
        add(closeButton);
        add(searchBox);
        add(searchButton);
        add(nextButton);
        add(prevButton);
        add(caseCheck);
        
        layoutGrid(this, 1, 6, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Adds listeners to components. */
    private void addListeners() {
        closeButton.addActionListener(this);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == closeButton) {
            close();
        }
    }
    
    /** Shows the search bar in the frame. */
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        }
        );
    }
    
    /** Hides the search bar in the frame. */
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(false);
            }
        }
        );
    }
}
