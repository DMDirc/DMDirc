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

import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.identities.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.dialogs.PasteDialog;
import com.dmdirc.ui.input.InputHandler;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Frame with an input field.
 */
public abstract class InputFrame extends Frame implements
        InternalFrameListener, MouseListener, ActionListener, KeyListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    
    /** The InputHandler for our input field. */
    private InputHandler inputHandler;
    
    /** Input field panel. */
    protected JPanel inputPanel;
    
    /** Frame input field. */
    private JTextField inputField;
    
    /** Popupmenu for this frame. */
    private JPopupMenu inputFieldPopup;
    
    /** popup menu item. */
    private JMenuItem inputPasteMI;
    
    /** popup menu item. */
    private JMenuItem inputCopyMI;
    
    /** popup menu item. */
    private JMenuItem inputCutMI;
    
    /** Robot for the frame. */
    private Robot robot;
    
    /** Away label. */
    private JLabel awayLabel;
    
    /**
     * Creates a new instance of InputFrame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public InputFrame(final FrameContainer owner) {
        super(owner);
        
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.error(ErrorLevel.TRIVIAL, "Error creating robot", ex);
        }
        
        initComponents();
        
        final ConfigManager config = owner.getConfigManager();
        
        getInputField().setBackground(config.getOptionColour("ui", "inputbackgroundcolour",
                config.getOptionColour("ui", "backgroundcolour", Color.WHITE)));
        getInputField().setForeground(config.getOptionColour("ui", "inputforegroundcolour",
                config.getOptionColour("ui", "foregroundcolour", Color.BLACK)));
        getInputField().setCaretColor(config.getOptionColour("ui", "inputforegroundcolour",
                config.getOptionColour("ui", "foregroundcolour", Color.BLACK)));
    }
    
    /** {@inheritDoc} */
    public void open() {
        if (Config.getOptionBool("ui", "awayindicator")) {
            awayLabel.setVisible(getContainer().getServer().isAway());
        }
        super.open();
    }
    
    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setInputField(new JTextField());
        getInputField().setBorder(
                BorderFactory.createCompoundBorder(
                getInputField().getBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        
        getInputField().addKeyListener(this);
        getInputField().addMouseListener(this);
        
        inputFieldPopup = new JPopupMenu();
        
        
        inputPasteMI = new JMenuItem("Paste");
        inputPasteMI.addActionListener(this);
        inputCopyMI = new JMenuItem("Copy");
        inputCopyMI.addActionListener(this);
        inputCutMI = new JMenuItem("Cut");
        inputCutMI.addActionListener(this);
        
        inputFieldPopup.add(inputCutMI);
        inputFieldPopup.add(inputCopyMI);
        inputFieldPopup.add(inputPasteMI);
        inputFieldPopup.setOpaque(true);
        inputFieldPopup.setLightWeightPopupEnabled(true);
        
        
        awayLabel = new JLabel();
        awayLabel.setText("(away)");
        awayLabel.setVisible(false);
        awayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
                SMALL_BORDER));
        
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(awayLabel, BorderLayout.LINE_START);
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        initInputField();
    }
    
    
    private void initInputField() {
        final UndoManager undo = new UndoManager();
        final Document doc = getInputField().getDocument();
        
        // Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        
        // Create an undo action and add it to the text component
        getInputField().getActionMap().put("Undo",
                new AbstractAction("Undo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canUndo()) {
                        undo.undo();
                    }
                } catch (CannotUndoException ex) {
                    Logger.error(ErrorLevel.TRIVIAL, "Unable to undo", ex);
                }
            }
        });
        
        // Bind the undo action to ctl-Z
        getInputField().getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        // Create a redo action and add it to the text component
        getInputField().getActionMap().put("Redo",
                new AbstractAction("Redo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canRedo()) {
                        undo.redo();
                    }
                } catch (CannotRedoException ex) {
                    Logger.error(ErrorLevel.TRIVIAL, "Unable to redo", ex);
                }
            }
        });
        
        // Bind the redo action to ctl-Y
        getInputField().getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }
    
    /**
     * Returns the input handler associated with this frame.
     *
     * @return Input handlers for this frame
     */
    public final InputHandler getInputHandler() {
        return inputHandler;
    }
    
    /**
     * Sets the input handler for this frame.
     *
     * @param newInputHandler input handler to set for this frame
     */
    public final void setInputHandler(final InputHandler newInputHandler) {
        this.inputHandler = newInputHandler;
    }
    
    /**
     * Returns the input field for this frame.
     *
     * @return JTextField input field for the frame.
     */
    public final JTextField getInputField() {
        return inputField;
    }
    
    /**
     * Sets the frames input field.
     *
     * @param newInputField new input field to use
     */
    protected final void setInputField(final JTextField newInputField) {
        this.inputField = newInputField;
    }
    
    /**
     * Returns the away label for this server connection.
     *
     * @return JLabel away label
     */
    public JLabel getAwayLabel() {
        return awayLabel;
    }
    
    /**
     * Sets the away indicator on or off.
     *
     * @param awayState away state
     */
    public void setAwayIndicator(final boolean awayState) {
        if (awayState) {
            inputPanel.add(awayLabel, BorderLayout.LINE_START);
            awayLabel.setVisible(true);
        } else {
            awayLabel.setVisible(false);
        }
    }
    
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameOpened(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosing(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosed(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Makes the internal frame invisible. {@inheritDoc}
     */
    public void internalFrameIconified(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeiconified(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Activates the input field on frame focus. {@inheritDoc}
     */
    public void internalFrameActivated(final InternalFrameEvent event) {
        getInputField().requestFocus();
        super.internalFrameActivated(event);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeactivated(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void keyTyped(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void keyPressed(final KeyEvent event) {
        if (event.getSource() == getTextPane()) {
            if ((Config.getOptionBool("ui", "quickCopy")
            || (event.getModifiers() & KeyEvent.CTRL_MASK) ==  0)) {
                event.setSource(getInputField());
                getInputField().requestFocus();
                if (robot != null && event.getKeyCode() != KeyEvent.VK_UNDEFINED) {
                    robot.keyPress(event.getKeyCode());
                    if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                        robot.keyRelease(event.getKeyCode());
                    }
                }
            }
        } else if ((event.getModifiers() & KeyEvent.CTRL_MASK) != 0
                && event.getKeyCode() == KeyEvent.VK_V) {
            doPaste(event);
        }
        super.keyPressed(event);
    }
    
    /** {@inheritDoc} */
    public void keyReleased(final KeyEvent event) {
        //Ignore.
    }
    
    /**
     * Checks for url's, channels and nicknames. {@inheritDoc}
     */
    public void mouseClicked(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
        super.mouseClicked(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mousePressed(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
        super.mousePressed(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseReleased(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
        super.mouseReleased(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     */
    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() == getInputField()) {
            final Point point = getInputField().getMousePosition();
            
            if (point != null) {
                inputFieldPopup.show(this, (int) point.getX(),
                        (int) point.getY() + getTextPane().getHeight()
                        + SMALL_BORDER);
            }
        }
        super.processMouseEvent(e);
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == inputCopyMI) {
            getInputField().copy();
        } else if (actionEvent.getSource() == inputPasteMI) {
            getInputField().paste();
        } else if (actionEvent.getSource() == inputCutMI) {
            getInputField().cut();
        }
        super.actionPerformed(actionEvent);
    }
    
    
    /**
     * Checks and pastes text.
     *
     * @param event the event that triggered the paste
     */
    private void doPaste(final KeyEvent event) {
        String clipboard = null;
        String[] clipboardLines = new String[]{"", };
        
        try {
            clipboard = getInputField().getText()
            + (String) Toolkit.getDefaultToolkit().getSystemClipboard()
            .getData(DataFlavor.stringFlavor);
            clipboardLines = clipboard.split(System.getProperty("line.separator"));
        } catch (HeadlessException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        } catch (UnsupportedFlavorException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        }
        if (clipboard != null && clipboard.indexOf('\n') >= 0) {
            event.consume();
            final int pasteTrigger = Config.getOptionInt("ui", "pasteProtectionLimit", 1);
            if (getNumLines(clipboard) > pasteTrigger) {
                showPasteDialog(clipboard, clipboardLines);
            } else {
                for (String clipboardLine : clipboardLines) {
                    this.sendLine(clipboardLine);
                }
            }
        }
    }
    
    /**
     * Shows the paste dialog.
     *
     * @param clipboard contents of the clipboard
     * @param clipboardLines clipboard contents split per line
     */
    private void showPasteDialog(final String clipboard,
            final String[] clipboardLines) {
        final String[] options = {"Send", "Edit", "Cancel", };
        final int n = JOptionPane.showOptionDialog(this,
                "<html>Paste would be sent as "
                + getNumLines(clipboard) + " lines.<br>"
                + "Do you want to continue?</html>",
                "Multi-line Paste",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        switch (n) {
            case 0:
                for (String clipboardLine : clipboardLines) {
                    this.sendLine(clipboardLine);
                }
                break;
            case 1:
                new PasteDialog(this, clipboard).setVisible(true);
                break;
            case 2:
                break;
            default:
                break;
        }
    }
}
