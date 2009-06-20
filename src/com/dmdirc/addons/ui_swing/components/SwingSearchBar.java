/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.SearchBar;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.SearchAction;
import com.dmdirc.addons.ui_swing.textpane.IRCDocument;
import com.dmdirc.addons.ui_swing.textpane.IRCDocumentSearcher;
import com.dmdirc.addons.ui_swing.textpane.LinePosition;
import com.dmdirc.addons.ui_swing.textpane.TextPane;
import com.dmdirc.util.ListenerList;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Status bar, shows message and info on the gui.
 */
public final class SwingSearchBar extends JPanel implements ActionListener,
        KeyListener, SearchBar, DocumentListener {

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
    /** Listener list. */
    private final ListenerList listeners;
    /** Parent window. */
    private Window parentWindow;

    /**
     * Creates a new instance of StatusBar.
     * 
     * @param newParent parent frame for the dialog
     * @param parentWindow Parent window
     */
    public SwingSearchBar(final TextFrame newParent, final Window parentWindow) {
        super();

        listeners = new ListenerList();

        this.parent = newParent;
        this.parentWindow = parentWindow;

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
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        caseCheck.setText("Case sensitive");

        line = -1;
    }

    /** Lays out components. */
    private void layoutComponents() {
        this.setLayout(new MigLayout("ins 0, fill"));

        add(closeButton);
        add(searchBox, "growx, pushx, sgy all");
        add(prevButton, "sgx button, sgy all");
        add(nextButton, "sgx button, sgy all");
        add(caseCheck, "sgy all");
    }

    /** Adds listeners to components. */
    private void addListeners() {
        closeButton.addActionListener(this);
        searchBox.addKeyListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
        caseCheck.addActionListener(this);
        searchBox.getDocument().addDocumentListener(this);
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
        } else if (e.getSource() == caseCheck) {
            searchBox.setBackground(ColourManager.getColour("FFFFFF"));
            line = parent.getTextPane().getLastVisibleLine();
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setVisible(true);
                searchBox.setBackground(ColourManager.getColour("FFFFFF"));
                getFocus();
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setVisible(false);
                if (parent instanceof InputTextFrame) {
                    ((InputTextFrame) parent).getInputField().requestFocusInWindow();
                } else {
                    parent.requestFocusInWindow();
                }
            }
        });
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

        final boolean up = Direction.UP == direction;

        final TextPane textPane = parent.getTextPane();
        final IRCDocument document = textPane.getDocument();
        final IRCDocumentSearcher searcher = new IRCDocumentSearcher(text, document,
                caseSensitive);
        searcher.setPosition(textPane.getSelectedRange());

        final LinePosition result = up ? searcher.searchUp() : searcher.searchDown();

        if (result == null) {
            foundText = false;
        } else if ((textPane.getSelectedRange().getEndLine() != 0 || 
                textPane.getSelectedRange().getEndPos() != 0) 
                && ((up && result.getEndLine() > textPane.getSelectedRange().getEndLine()) 
                || (!up && result.getStartLine() < textPane.getSelectedRange().getStartLine())) 
                && JOptionPane.showConfirmDialog(parentWindow,
                "Do you want to continue searching from the " + (up ? "end" : "beginning") + "?",
                "No more results", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
            // It's wrapped, and they don't want to continue searching

            foundText = false;
        } else {
            //found, select and return found
            textPane.setScrollBarPosition(result.getEndLine());
            textPane.setSelectedTexT(result);
            foundText = true;
        }

        if (foundText) {
            searchBox.setBackground(ColourManager.getColour("FFFFFF"));
        } else {
            searchBox.setBackground(ColourManager.getColour("FF0000"));
        }
    }

    /**
     * {@inheritDoc}.
     *
     * @param event Key event
     */
    @Override
    public void keyPressed(final KeyEvent event) {
        if (event.getSource() == searchBox) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                close();
            } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                search(Direction.UP, searchBox.getText(), caseCheck.isSelected());
            } else if (event.getKeyCode() != KeyEvent.VK_F3 && event.getKeyCode() != KeyEvent.VK_F) {
                line = parent.getTextPane().getLastVisibleLine();
            }
        }

        for (KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyPressed(event);
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

            /** {@inheritDoc} */
            @Override
            public void run() {
                searchBox.requestFocusInWindow();
                searchBox.setSelectionStart(0);
                searchBox.setSelectionEnd(searchBox.getText().length());
            }
        });
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

    /** {@inheritDoc}. */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        searchBox.setBackground(ColourManager.getColour("FFFFFF"));
        nextButton.setEnabled(!searchBox.getText().isEmpty());
        prevButton.setEnabled(!searchBox.getText().isEmpty());
    }

    /** {@inheritDoc}. */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        searchBox.setBackground(ColourManager.getColour("FFFFFF"));
        nextButton.setEnabled(!searchBox.getText().isEmpty());
        prevButton.setEnabled(!searchBox.getText().isEmpty());
    }

    /** {@inheritDoc}. */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(final KeyListener l) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                listeners.add(KeyListener.class, l);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(final KeyListener l) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                listeners.remove(KeyListener.class, l);
            }
        });
    }
}
