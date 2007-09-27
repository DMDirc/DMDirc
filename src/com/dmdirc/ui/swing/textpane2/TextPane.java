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

package com.dmdirc.ui.swing.textpane2;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

/**
 * IRC Textpane.
 */
public class TextPane extends JComponent {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** UI Class ID. */
    private static final String uiClassID = "TextPaneUI";
    
    /** Listener list. */
    private final EventListenerList textPaneListeners;
    
    /** IRC Document. */
    private IRCDocument document;
    
    /** Creates a new instance of TextPane with a blank document. */
    public TextPane() {
        this(new IRCDocument());
    }
    
    /** 
     * Creates a new instance of TextPane using the specified document. 
     *
     * @param document Document to be displayed
     */
    public TextPane(final IRCDocument document) {
        super();
        
        this.document = document;
        
        textPaneListeners = new EventListenerList();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateUI() {
        setUI((TextPaneUI) UIManager.getUI(this));
    }
    
    /** 
     * Sets the UI to be used for this component.
     *
     * @param ui New TextPaneUI to be used for this component
     */
    public void setUI(final TextPaneUI ui) {
        super.setUI(ui);
    }
    
    /** 
     * Returns the TextPaneUI being used by this TextPane.
     *
     * @return Current UI being used by this component
     */
    public TextPaneUI getUI() {
        return (TextPaneUI) ui;
    }
    
    /**
     * Sets the document being used for this component.
     *
     * @param document IRCDocument to be used by this component
     */
    public void setDocument(final IRCDocument document) {
        this.document = document;
    }
    
    /**
     * Returns the document being used by this component.
     *
     * @return IRCDocument being used by this component
     */
    public IRCDocument getDocument() {
        return document;
    }
    
    /**
     * Adds a TextPaneListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addTextPaneListener(final TextPaneListener listener) {
        synchronized (textPaneListeners) {
            if (listener == null) {
                return;
            }
            textPaneListeners.add(TextPaneListener.class, listener);
        }
    }
    
    /**
     * Removes a TextPaneListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeTextPaneListener(final TextPaneListener listener) {
        textPaneListeners.remove(TextPaneListener.class, listener);
    }
    
    /**
     * Informs listeners when a word has been clicked on.
     *
     * @param text word clicked on
     * @param event Triggering Event
     */
    protected void fireHyperlinkClicked(final String text, final MouseEvent event) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).hyperlinkClicked(text, event);
            }
        }
    }
    
    /**
     * Informs listeners when a word has been clicked on.
     *
     * @param text word clicked on
     * @param event Triggering Event
     */
    protected void fireChannelClicked(final String text, final MouseEvent event) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).channelClicked(text, event);
            }
        }
    }
    
    /**
     * Informs listeners when a nickname has been clicked on.
     *
     * @param text word clicked on
     * @param event Triggering Event
     */
    protected void fireNicknameClicked(final String text, final MouseEvent event) {
        final Object[] listeners = textPaneListeners.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == TextPaneListener.class) {
                ((TextPaneListener) listeners[i + 1]).nickNameClicked(text, event);
            }
        }
    }
    
}
