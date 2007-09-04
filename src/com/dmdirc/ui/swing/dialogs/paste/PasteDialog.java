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

package com.dmdirc.ui.swing.dialogs.paste;

import com.dmdirc.Main;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.InputFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Allows the user to modify global client preferences.
 */
public final class PasteDialog extends StandardDialog implements ActionListener,
        KeyListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 4;
    
    /** Number of lines Label. */
    private JTextArea infoLabel;
    
    /** Text area scrollpane. */
    private JScrollPane scrollPane;
    
    /** Text area. */
    private JTextArea textField;
    
    /** Parent frame. */
    private final InputFrame parent;
    
    /** Edit button. */
    private JButton editButton;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param newParent The frame that owns this dialog
     * @param text text to show in the paste dialog
     */
    public PasteDialog(final InputFrame newParent, final String text) {
        super(((MainFrame) Main.getUI().getMainWindow()), false);
        
        this.parent = newParent;
        
        initComponents(text);
        initListeners();
        
        setFocusTraversalPolicy(new PasteDialogFocusTraversalPolicy(
                getCancelButton(), editButton, getOkButton()));
        
        setFocusable(true);
        getOkButton().requestFocus();
        getOkButton().setSelected(true);
        
        setLocationRelativeTo(((MainFrame) Main.getUI().getMainWindow()));
    }
    
    /**
     * Initialises GUI components.
     * @param text text to show in the dialog
     */
    private void initComponents(final String text) {
        final GridBagConstraints constraints = new GridBagConstraints();
        scrollPane = new JScrollPane();
        textField = new JTextArea(text);
        editButton = new JButton("Edit");
        infoLabel = new JTextArea();
        
        initInputField();
        
        editButton.setPreferredSize(new Dimension(100, 25));
        editButton.setMinimumSize(new Dimension(100, 25));
        
        orderButtons(new JButton(), new JButton());
        getOkButton().setText("Send");
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Multi-line paste");
        setResizable(false);
        
        infoLabel.setText("This will be sent as "
                + parent.getContainer().getNumLines(textField.getText())
                + " lines. Are you sure you want to continue?");
        infoLabel.setEditable(false);
        infoLabel.setWrapStyleWord(true);
        infoLabel.setLineWrap(true);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(this.getBackground());
        
        textField.setBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        textField.setColumns(50);
        textField.setRows(10);
        scrollPane.setViewportView(textField);
        scrollPane.setVisible(false);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        getContentPane().add(infoLabel, constraints);
        
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.gridy = 2;
        getContentPane().add(scrollPane, constraints);
        
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(Box.createHorizontalGlue(), constraints);
        
        constraints.insets.set(LARGE_BORDER, 0, LARGE_BORDER, LARGE_BORDER);
        constraints.weightx = 0.0;
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(getLeftButton(), constraints);
        
        constraints.gridx = 2;
        getContentPane().add(editButton, constraints);
        
        constraints.gridx = 3;
        getContentPane().add(getRightButton(), constraints);
        
        pack();
    }
    
    /**
     * Initialises the input field.
     */
    private void initInputField() {
        final UndoManager undo = new UndoManager();
        final Document doc = textField.getDocument();
        
        // Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(final UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        
        // Create an undo action and add it to the text component
        textField.getActionMap().put("Undo",
                new AbstractAction("Undo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(final ActionEvent evt) {
                try {
                    if (undo.canUndo()) {
                        undo.undo();
                    }
                } catch (CannotUndoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to undo");
                }
            }
        });
        
        // Bind the undo action to ctl-Z
        textField.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        // Create a redo action and add it to the text component
        textField.getActionMap().put("Redo",
                new AbstractAction("Redo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(final ActionEvent evt) {
                try {
                    if (undo.canRedo()) {
                        undo.redo();
                    }
                } catch (CannotRedoException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to redo");
                }
            }
        });
        
        // Bind the redo action to ctl-Y
        textField.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }
    
    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        editButton.addActionListener(this);
        textField.addKeyListener(this);
        
        getRootPane().getActionMap().put("rightArrowAction",
                new AbstractAction("rightArrowAction") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(final ActionEvent evt) {
                final JButton button = (JButton) getFocusTraversalPolicy().
                        getComponentAfter(PasteDialog.this, getFocusOwner());
                button.requestFocus();
                button.setSelected(true);
            }
        }
        );
        
        getRootPane().getActionMap().put("leftArrowAction",
                new AbstractAction("leftArrowAction") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(final ActionEvent evt) {
                final JButton button = (JButton) getFocusTraversalPolicy().
                        getComponentBefore(PasteDialog.this, getFocusOwner());
                button.requestFocus();
                button.setSelected(true);
            }
        }
        );
        
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "rightArrowAction");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "leftArrowAction");
    }
    
    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            if (textField.getText().length() > 0) {
                final String[] lines = textField.getText().split("\n");
                for (String line : lines) {
                    parent.getContainer().sendLine(line);
                    parent.getInputHandler().addToBuffer(line);
                }
            }
            dispose();
        } else if (editButton.equals(actionEvent.getSource())) {
            editButton.setEnabled(false);
            setResizable(true);
            scrollPane.setVisible(true);
            infoLabel.setText("This will be sent as "
                    + parent.getContainer().getNumLines(textField.getText())
                    + " lines.");
            pack();
            setLocationRelativeTo(((MainFrame) Main.getUI().getMainWindow()));
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }
    
    /** {@inheritDoc} */
    public void keyTyped(final KeyEvent e) {
        infoLabel.setText("This will be sent as "
                + parent.getContainer().getNumLines(textField.getText())
                + " lines.");
    }
    
    /** {@inheritDoc} */
    public void keyPressed(final KeyEvent e) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void keyReleased(final KeyEvent e) {
        //Ignore.
    }
}
