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

package com.dmdirc.ui.swing.components;

import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.ui.interfaces.SearchBar;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.actions.SearchAction;
import com.dmdirc.ui.swing.textpane.TextPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Status bar, shows message and info on the gui.
 */
public final class SwingSearchBar extends JPanel implements ActionListener,
        KeyListener, SearchBar {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 6;
    
    /** Frame parent. */
    private final TextFrame parent;
    
    /** Close button. */
    private ImageButton closeButton;
    
    /** Next match button. */
    private JButton nextButton;
    
    /** Previous match button. */
    private JButton prevButton;
    
    /** Case sensitive checkbox. */
    private JCheckBox caseCheck;
    
    /** Search text field. */
    private JTextField searchBox;
    
    /** Line to search from. */
    private int line;
    /** Character to search from. */
    private int index;
    
    /**
     * Creates a new instance of StatusBar.
     * @param newParent parent frame for the dialog
     */
    public SwingSearchBar(final TextFrame newParent) {
        super();
        
        this.parent = newParent;
        
        getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");
        
        getActionMap().put("searchAction", new SearchAction(this));
        
        initComponents();
        layoutComponents();
        addListeners();
    }
    
    /** Initialises components. */
    private void initComponents() {
        closeButton = new ImageButton("close", 
                IconManager.getIconManager().getIcon("close-inactive"), 
                IconManager.getIconManager().getIcon("close-active"));
        nextButton = new JButton();
        prevButton = new JButton();
        caseCheck = new JCheckBox();
        searchBox = new JTextField();
        
        nextButton.setText("Later");
        prevButton.setText("Earlier");
        caseCheck.setText("Case sensitive");
        
        line = -1;
        index = 0;
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        this.setLayout(new MigLayout("ins rel 0 0 0, fill"));
        
        add(closeButton);
        add(searchBox, "growx, pushx, sgy all");
        add(prevButton, "sgx button, sgy all");
        add(nextButton, "sgx button, sgy all");
        add(caseCheck, "sgy all");
    }
    
    /** Adds listeners to components. */
    private void addListeners() {
        closeButton.addActionListener(this);
        addKeyListener(this);
        searchBox.addKeyListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
    }
    
    /** 
     * {@inheritDoc}. 
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == closeButton) {
            close();
        } else if (e.getSource() == nextButton) {
            search(Direction.DOWN, searchBox.getText(), caseCheck.isSelected());
        } else if (e.getSource() == prevButton) {
            search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
        }
    }
    
    /** {@inheritDoc}. */
    @Override
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
    
    /** {@inheritDoc}. */
    @Override
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(false);
                if (parent instanceof InputTextFrame) {
                    ((InputTextFrame) parent).getInputField().requestFocus();
                } else {
                    parent.requestFocus();
                }
            }
        }
        );
    }
    
    /** {@inheritDoc}. */
    @Override
    public void search(final String text, final boolean caseSensitive) {
        if (!searchBox.getText().isEmpty()) {
            if (line == -1) {
                line = parent.getTextPane().getLastVisibleLine();
            }
            search(Direction.UP, text, caseSensitive);
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public void search(final Direction direction, final String text,
            final boolean caseSensitive) {
        boolean foundText = false;
        
        if (!text.isEmpty() && checkPhraseOccurs(searchBox.getText(), caseCheck.isSelected())) {
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
        if (JOptionPane.showConfirmDialog((MainFrame) Main.getUI().getMainWindow(),
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
        if (JOptionPane.showConfirmDialog((MainFrame) Main.getUI().getMainWindow(),
                "Do you want to continue searching from the beginning?",
                "End reached", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            line = 0;
            index = 0;
            return searchDown(text, caseSensitive);
        }
        
        return false;
    }
    
    /** {@inheritDoc}. */
    @Override
    public boolean checkPhraseOccurs(final String text, final boolean caseSensitive) {
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
    
    /** 
     * {@inheritDoc}.
     * 
     * @param event Key event
     */
    @Override
    public void keyPressed(final KeyEvent event) {
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
    
    /** 
     * {@inheritDoc}.
     * 
     * @param event Key event
     */
    @Override
    public void keyTyped(final KeyEvent event) {
        //Ignore
    }
    
    /** 
     * {@inheritDoc}.
     * 
     * @param event Key event
     */
    @Override
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

    /** {@inheritDoc}. */
    @Override
    public String getSearchPhrase() {
        return searchBox.getText();
    }

    /** {@inheritDoc}. */
    @Override
    public boolean isCaseSensitive() {
        return caseCheck.isSelected();
    }
}
