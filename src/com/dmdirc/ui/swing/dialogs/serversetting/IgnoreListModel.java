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
package com.dmdirc.ui.swing.dialogs.serversetting;

import com.dmdirc.IgnoreList;
import javax.swing.AbstractListModel;

/**
 *
 * @author chris
 */
public class IgnoreListModel extends AbstractListModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    private final IgnoreList ignoreList;

    private boolean isSimple;

    public IgnoreListModel(IgnoreList ignoreList) {
        this.ignoreList = ignoreList;
        isSimple = ignoreList.canConvert();
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return ignoreList.count();
    }

    /** {@inheritDoc} */
    @Override
    public Object getElementAt(final int index) {
        if (isSimple) {
            return ignoreList.getSimpleList().get(index);
        } else {
            return ignoreList.getRegexList().get(index);
        }
    }

    public void setIsSimple(boolean isSimple) {
        this.isSimple = isSimple;

        fireContentsChanged(this, 0, getSize());
    }
    
    public void notifyUpdated() {
        fireContentsChanged(this, 0, getSize());
    }

}
