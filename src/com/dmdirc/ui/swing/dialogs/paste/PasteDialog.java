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

package com.dmdirc.ui.swing.dialogs.paste;

import com.dmdirc.Main;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.InputTextFrame;
import com.dmdirc.ui.swing.components.StandardDialog;
import com.dmdirc.ui.swing.components.SwingInputHandler;
import com.dmdirc.ui.swing.components.TextAreaInputField;
import com.dmdirc.ui.swing.components.TextLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

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
    private TextLabel infoLabel;
    /** Text area scrollpane. */
    private JScrollPane scrollPane;
    /** Text area. */
    private TextAreaInputField textField;
    /** Parent frame. */
    private final InputTextFrame parent;
    /** Edit button. */
    private JButton editButton;

    /**
     * Creates a new instance of PreferencesDialog.
     * 
     * @param newParent The frame that owns this dialog
     * @param text text to show in the paste dialog
     */
    public PasteDialog(final InputTextFrame newParent, final String text) {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        this.parent = newParent;

        initComponents(text);
        initListeners();

        setFocusTraversalPolicy(new PasteDialogFocusTraversalPolicy(
                getCancelButton(), editButton, getOkButton()));

        setFocusable(true);
        getOkButton().requestFocus();
        getOkButton().setSelected(true);

        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
    }

    /**
     * Initialises GUI components.
     * 
     * @param text text to show in the dialog
     */
    private void initComponents(final String text) {
        scrollPane = new JScrollPane();
        textField = new TextAreaInputField(text);
        editButton = new JButton("Edit");
        infoLabel = new TextLabel();

        UIUtilities.addUndoManager(textField);

        orderButtons(new JButton(), new JButton());
        getOkButton().setText("Send");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Multi-line paste");
        setResizable(false);

        infoLabel.setText("This will be sent as " + parent.getContainer().getNumLines(textField.getText()) + " lines. Are you sure you want to continue?");

        textField.setColumns(50);
        textField.setRows(10);

        new SwingInputHandler(textField, parent.getCommandParser(), parent).setTypes(false, false, true, false);

        scrollPane.setViewportView(textField);
        scrollPane.setVisible(false);

        getContentPane().setLayout(new MigLayout("fill, hidemode 3, pack"));
        getContentPane().add(infoLabel, "wrap, growx, pushx, span 3");
        getContentPane().add(scrollPane, "wrap, grow, push, span 3");
        getContentPane().add(getLeftButton(), "right, sg button");
        getContentPane().add(editButton, "right, sg button");
        getContentPane().add(getRightButton(), "right, sg button");
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

                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        final JButton button = (JButton) getFocusTraversalPolicy().
                                getComponentAfter(PasteDialog.this, getFocusOwner());
                        button.requestFocus();
                        button.setSelected(true);
                    }
                });

        getRootPane().getActionMap().put("leftArrowAction",
                new AbstractAction("leftArrowAction") {

                    private static final long serialVersionUID = 1;

                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        final JButton button = (JButton) getFocusTraversalPolicy().
                                getComponentBefore(PasteDialog.this, getFocusOwner());
                        button.requestFocus();
                        button.setSelected(true);
                    }
                });

        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "rightArrowAction");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "leftArrowAction");

        textField.getActionMap().put("ctrlEnterAction",
                new AbstractAction("ctrlEnterAction") {

                    private static final long serialVersionUID = 1;

                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        getOkButton().doClick();
                    }
                });
        textField.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "ctrlEnterAction");
    }

    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            if (!textField.getText().isEmpty()) {
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
            infoLabel.setText("This will be sent as " + parent.getContainer().getNumLines(textField.getText()) + " lines.");
            pack();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void validate() {
        super.validate();
        
        setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
    }
    
    /** {@inheritDoc} */
    @Override
    public void keyTyped(final KeyEvent e) {
        infoLabel.setText("This will be sent as " + parent.getContainer().getNumLines(textField.getText()) + " lines.");
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed(final KeyEvent e) {
        //Ignore.
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(final KeyEvent e) {
        //Ignore.
    }
}
