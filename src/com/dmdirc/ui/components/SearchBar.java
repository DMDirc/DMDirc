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

package com.dmdirc.ui.components;

import com.dmdirc.IconManager;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.textpane.TextPane;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static com.dmdirc.ui.UIUtilities.layoutGrid;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Status bar, shows message and info on the gui.
 */
public final class SearchBar extends JPanel implements ActionListener,
        KeyListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Frame parent. */
    private final Frame parent;
    
    /** Close button. */
    private JButton closeButton;
    
    /** Next match button. */
    private JButton nextButton;
    
    /** Previous match button. */
    private JButton prevButton;
    
    /** Case sensitive checkbox. */
    private JCheckBox caseCheck;
    
    /** Search text field. */
    private JTextField searchBox;
    
    /** Direction used for searching. */
    public enum Direction {
        /** Move up through the document. */
        UP,
        /** Move down through the document. */
        DOWN,
    };
    
    /** Line to search from. */
    private int line;
    /** Character to search from. */
    private int index;
    
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
        nextButton = new JButton();
        prevButton = new JButton();
        caseCheck = new JCheckBox();
        searchBox = new JTextField();
        
        nextButton.setText("Later");
        prevButton.setText("Earlier");
        caseCheck.setText("Case sensitive");
        
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setPreferredSize(new Dimension(50, 0));
        
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setPreferredSize(new Dimension(50, 0));
        
        prevButton.setMargin(new Insets(0, 0, 0, 0));
        prevButton.setPreferredSize(new Dimension(50, 0));
        
        caseCheck.setPreferredSize(new Dimension(110, 0));
        
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                searchBox.getBorder(), new EmptyBorder(2, 2, 2, 2)));
        
        searchBox.setPreferredSize(new Dimension(300, searchBox.getFont().getSize() - SMALL_BORDER));
        
        
        
        closeButton.setIcon(IconManager.getIconManager().getIcon("close-inactive"));
        closeButton.setRolloverIcon(IconManager.getIconManager().getIcon("close-active"));
        closeButton.setPressedIcon(IconManager.getIconManager().getIcon("close-active"));
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        closeButton.setPreferredSize(new Dimension(16, 16));
        
        line = -1;
        index = 0;
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        this.setLayout(new SpringLayout());
        
        add(closeButton);
        add(searchBox);
        add(prevButton);
        add(nextButton);
        add(caseCheck);
        
        layoutGrid(this, 1, 5, SMALL_BORDER, 0, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** Adds listeners to components. */
    private void addListeners() {
        closeButton.addActionListener(this);
        this.addKeyListener(this);
        searchBox.addKeyListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == closeButton) {
            close();
        } else if (e.getSource() == nextButton) {
            search(Direction.DOWN, searchBox.getText(), caseCheck.isSelected());
        } else if (e.getSource() == prevButton) {
            search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
        }
    }
    
    /** Shows the search bar in the frame. */
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
                searchBox.requestFocus();
                searchBox.setBackground(ColourManager.getColour("FFFFFF"));
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
    
    /**
     * Searches the textpane for text.
     */
    public void search() {
        if (line == -1) {
            line = parent.getTextPane().getLastVisibleLine();
        }
        search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
    }
    
    /**
     * Searches the textpane for text.
     *
     * @param direction the direction to search from
     * @param text the text to search for
     * @param caseSensitive whether the search is case sensitive
     */
    public void search(final Direction direction, final String text,
            final boolean caseSensitive) {
        boolean foundText = false;
        
        if (!"".equals(text) && checkOccurs(searchBox.getText(), caseCheck.isSelected())) {
            if (direction == Direction.UP) {
                foundText = searchUp(text, caseSensitive);
            } else {
                foundText = searchDown(text, caseSensitive);
            }
        }
        
        if (foundText) {
            searchBox.setBackground(ColourManager.getColour("FFFFFF"));
        } else {
            searchBox.setBackground(ColourManager.getColour("FF0000"));
        }
    }
    
    /**
     * Searches up in the buffer for the text.
     *
     * @param text Text to search for
     * @param caseSensitive Whether to match case.
     *
     * @return Whether the specified text was found
     */
    private boolean searchUp(final String text, final boolean caseSensitive) {
        final TextPane textPane = parent.getTextPane();
        
        //check value within bounds
        if (line > textPane.getNumLines() - 1) {
            line = textPane.getNumLines() - 1;
        }
        if (line < 0) {
            line = 0;
        }
        
        //loop through lines
        while (line >= 0) {
            final String lineText;
            int position;
            
            //get text
            if (caseSensitive) {
                lineText = textPane.getTextFromLine(line);
            } else {
                lineText = textPane.getTextFromLine(line).toLowerCase(Locale.getDefault());
            }
            
            if (index == 0) {
                position = lineText.length();
            } else {
                position = index;
            }
            //loop through the line
            while (position >= 0) {
                //check for position
                if (caseSensitive) {
                    position = lineText.substring(0, position).lastIndexOf(text);
                } else {
                    position = lineText.substring(0, position).lastIndexOf(text.toLowerCase(Locale.getDefault()));
                }
                if (position == -1) {
                    //not found, break look and move to next line
                    line--;
                    index = 0;
                    break;
                } else {
                    //found, select and return found
                    textPane.setScrollBarPosition(line);
                    textPane.setSelectedTexT(line, position, line, position + text.length());
                    index = position;
                    return true;
                }
            }
        }
        
        //want to continue?
        if (JOptionPane.showConfirmDialog(MainFrame.getMainFrame(),
                "Do you want to continue searching from the end?",
                "Beginning reached", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            line = parent.getTextPane().getNumLines() - 1;
            index = 0;
            return searchUp(text, caseSensitive);
        }
        
        return false;
    }
    
    /**
     * Searches down in the buffer for the text.
     *
     * @param text Text to search for
     * @param caseSensitive Whether to match case.
     *
     * @return Whether the specified text was found
     */
    private boolean searchDown(final String text, final boolean caseSensitive) {
        final TextPane textPane = parent.getTextPane();
        
        //check value within bounds
        if (line > textPane.getNumLines() - 1) {
            line = textPane.getNumLines() - 1;
        }
        if (line < 0) {
            line = 0;
        }
        
        //loop through lines
        while (line < textPane.getNumLines()) {
            final String lineText;
            //get text
            if (caseSensitive) {
                lineText = textPane.getTextFromLine(line);
            } else {
                lineText = textPane.getTextFromLine(line).toLowerCase(Locale.getDefault());
            }
            int position = index;
            //loop through the line
            while (position < lineText.length()) {
                //check for position
                if (caseSensitive) {
                    position = lineText.indexOf(text, position);
                } else {
                    position = lineText.indexOf(text.toLowerCase(Locale.getDefault()), position);
                }
                if (position == -1) {
                    //not found, break look and move to next line
                    line++;
                    index = 0;
                    break;
                } else {
                    //found, select and return found
                    textPane.setScrollBarPosition(line);
                    textPane.setSelectedTexT(line, position, line, position + text.length());
                    position = position + text.length();
                    index = position;
                    return true;
                }
            }
        }
        
        //want to continue?
        if (JOptionPane.showConfirmDialog(MainFrame.getMainFrame(),
                "Do you want to continue searching from the beginning?",
                "End reached", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            line = 0;
            index = 0;
            return searchDown(text, caseSensitive);
        }
        
        return false;
    }
    
    /**
     * Checks to see if the specified text exist in the document.
     *
     * @param text Text to check for
     * @param caseSensitive Case sensitive check
     *
     * @return Whether the text exists
     */
    public boolean checkOccurs(final String text, final boolean caseSensitive) {
        final TextPane textPane = parent.getTextPane();
        boolean foundText = false;
        int i;
        
        for (i = textPane.getNumLines() - 1; i >= 0; i--) {
            final String lineText;
            final int position;
            if (caseSensitive) {
                lineText = textPane.getTextFromLine(i);
                position = lineText.indexOf(text);
            } else {
                lineText = textPane.getTextFromLine(i).toLowerCase(Locale.getDefault());
                position = lineText.indexOf(text.toLowerCase(Locale.getDefault()));
            }
            if (position != -1 && textPane.getSelectedRange()[0] != i) {
                foundText = true;
                break;
            }
        }
        return foundText;
    }
    
    /** {@inheritDoc}. */
    public void keyTyped(final KeyEvent event) {
        //Ignore
    }
    
    /** {@inheritDoc}. */
    public void keyPressed(final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_F3) {
            search();
        }
        
        if (event.getKeyCode() == KeyEvent.VK_F
                && (event.getModifiers() & KeyEvent.CTRL_MASK) !=  0) {
            getFocus();
        }
        
        if (event.getSource() == searchBox) {
            searchBox.setBackground(ColourManager.getColour("FFFFFF"));
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                close();
            } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
            } else {
                line = parent.getTextPane().getLastVisibleLine();
                index = 0;
            }
        }
    }
    
    /** {@inheritDoc}. */
    public void keyReleased(final KeyEvent event) {
        //Ignore
    }
    
    /** Focuses the search box in the search bar. */
    public void getFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                searchBox.requestFocus();
                searchBox.setSelectionStart(0);
                searchBox.setSelectionEnd(searchBox.getText().length());
            }
        }
        );
    }
}
