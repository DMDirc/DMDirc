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

package com.dmdirc.addons.ui_swing.dialogs.paste;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

import javax.swing.JButton;

/**
 * Focus traversal policy for the paste dialog.
 */
public final class PasteDialogFocusTraversalPolicy extends FocusTraversalPolicy {

    /** Cancel button. */
    private final JButton cancelButton;
    /** Edit button. */
    private final JButton editButton;
    /** Send button. */
    private final JButton sendButton;

    /** 
     * Creates a new instance of PasteDialogFocusTraversalPolicy. 
     *
     * @param cancelButton Cancel button
     * @param editButton Edit button
     * @param sendButton Send button
     */
    public PasteDialogFocusTraversalPolicy(final JButton cancelButton,
            final JButton editButton, final JButton sendButton) {
        super();

        this.cancelButton = cancelButton;
        this.editButton = editButton;
        this.sendButton = sendButton;
    }

    /** {@inheritDoc} */
    public Component getComponentAfter(final Container aContainer,
            final Component aComponent) {
        if (aComponent.equals(cancelButton)) {
            return editButton;
        } else if (aComponent.equals(editButton)) {
            return sendButton;
        } else if (aComponent.equals(sendButton)) {
            return cancelButton;
        } else {
            return cancelButton;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Component getComponentBefore(final Container aContainer,
            final Component aComponent) {
        if (aComponent.equals(cancelButton)) {
            return sendButton;
        } else if (aComponent.equals(editButton)) {
            return cancelButton;
        } else if (aComponent.equals(sendButton)) {
            return editButton;
        } else {
            return sendButton;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Component getFirstComponent(final Container aContainer) {
        return cancelButton;
    }

    /** {@inheritDoc} */
    @Override
    public Component getLastComponent(final Container aContainer) {
        return sendButton;
    }

    /** {@inheritDoc} */
    @Override
    public Component getDefaultComponent(final Container aContainer) {
        return sendButton;
    }
}
