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

import com.dmdirc.ui.IconManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.InputField;
import com.dmdirc.ui.interfaces.InputValidationListener;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.ListenerList;

import com.dmdirc.util.ReturnableThread;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import net.miginfocom.swing.MigLayout;

/** Swing input field. */
public class SwingInputField extends JComponent implements InputField,
        KeyListener, InputValidationListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Colour picker. */
    private ColourPickerDialog colourPicker;
    /** Input field text field. */
    private final JTextField textField;
    /** Line wrap indicator. */
    private final JLabel wrapIndicator;
    /** Error indicator. */
    private final JLabel errorIndicator;
    /** Listener list. */
    private final ListenerList listeners;
    /** Parent Window. */
    private final Window parentWindow;

    /**
     * Instantiates a new swing input field.
     *
     * @param parentWindow Parent window
     */
    public SwingInputField(final Window parentWindow) {
        super();

        this.parentWindow = parentWindow;

        listeners = new ListenerList();

        textField = new JTextField();
        textField.setFocusTraversalKeysEnabled(false);
        textField.addKeyListener(this);
        wrapIndicator =
                new JLabel(IconManager.getIconManager().getIcon("linewrap"));
        wrapIndicator.setVisible(false);
        errorIndicator =
                new JLabel(IconManager.getIconManager().getIcon("input-error"));
        errorIndicator.setVisible(false);

        setLayout(new MigLayout("ins 0, hidemode 3"));

        add(textField, "growx, pushx");
        add(wrapIndicator, "");
        add(errorIndicator, "");

        setActionMap(textField.getActionMap());
        setInputMap(SwingInputField.WHEN_FOCUSED,
                textField.getInputMap(SwingInputField.WHEN_FOCUSED));
        setInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                textField.getInputMap(SwingInputField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        setInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW,
                textField.getInputMap(SwingInputField.WHEN_IN_FOCUSED_WINDOW));
    }

    /** {@inheritDoc} */
    @Override
    public void requestFocus() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.requestFocus();
            }
        });
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean requestFocusInWindow() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Boolean>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.requestFocusInWindow());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (IdentityManager.getGlobalConfig().getOptionBool("general",
                        "showcolourdialog")) {
                    colourPicker = new ColourPickerDialog(irc, hex, parentWindow);
                    colourPicker.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent actionEvent) {
                            try {
                                textField.getDocument().
                                        insertString(textField.getCaretPosition(),
                                        actionEvent.getActionCommand(), null);
                            } catch (BadLocationException ex) {
                            //Ignore, wont happen
                            }
                            colourPicker.dispose();
                            colourPicker = null;
                        }
                    });
                    colourPicker.setLocation((int) textField.getLocationOnScreen().
                            getX(),
                            (int) textField.getLocationOnScreen().getY() -
                            colourPicker.getHeight());
                    colourPicker.setVisible(true);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void hideColourPicker() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (colourPicker != null) {
                    colourPicker.dispose();
                    colourPicker = null;
                }
            }
        });
    }

    /**
     * Returns the textfield for this inputfield.
     * 
     * @return JTextField
     */
    public JTextField getTextField() {
        return UIUtilities.invokeAndWait(new ReturnableThread<JTextField>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addActionListener(final ActionListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.addActionListener(listener);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(final KeyListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                listeners.add(KeyListener.class, listener);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void removeActionListener(final ActionListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.removeActionListener(listener);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(final KeyListener listener) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                listeners.remove(KeyListener.class, listener);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getSelectedText() {
        return UIUtilities.invokeAndWait(new ReturnableThread<String>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.getSelectedText());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionEnd() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.getSelectionEnd());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionStart() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.getSelectionStart());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getText() {
        return UIUtilities.invokeAndWait(new ReturnableThread<String>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.getText());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final String text) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setText(text);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.getCaretPosition());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(final int position) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setCaretPosition(position);
            }
        });
    }

    /**
     * Replaces the selection with the specified text.
     * 
     * @param clipboard Text to replace selection with
     */
    public void replaceSelection(final String clipboard) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.replaceSelection(clipboard);
            }
        });
    }

    /**
     * Sets the caret colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    public void setCaretColor(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setCaretColor(optionColour);
            }
        });
    }

    /**
     * Sets the foreground colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setForeground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setForeground(optionColour);
            }
        });
    }

    /**
     * Sets the background colour to the specified coloour.
     * 
     * @param optionColour Colour for the caret
     */
    @Override
    public void setBackground(final Color optionColour) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                textField.setBackground(optionColour);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasFocus() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Boolean>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.hasFocus());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFocusOwner() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Boolean>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(textField.isFocusOwner());
            }
        });
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {
        for (KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyTyped(e);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        for (KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyPressed(e);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {
        for (KeyListener listener : listeners.get(KeyListener.class)) {
            listener.keyReleased(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void illegalCommand(final String reason) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                errorIndicator.setVisible(true);
                errorIndicator.setToolTipText(reason);
                wrapIndicator.setVisible(false);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void legalCommand() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                errorIndicator.setVisible(false);
                errorIndicator.setToolTipText(null);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void wrappedText(final int count) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                wrapIndicator.setVisible(count > 1);
                wrapIndicator.setToolTipText(count + " lines");
                errorIndicator.setVisible(false);
            }
        });
    }
}
