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

package com.dmdirc.ui.swing.components.fancynicklist;

import com.dmdirc.parser.ChannelClientInfo;
import com.dmdirc.ui.swing.NicklistListModel;
import java.util.ArrayList;
import java.util.List;

/**
 * The nicklist model for the fancy nick list.
 *
 * @author chris
 */
public class FancyNicklistModel extends NicklistListModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public Object getElementAt(final int index) {
        int vindex = 0, pindex = 0;
        char lastmode = 'a'; // won't ever be valid

        do {
            String rawmode = ((ChannelClientInfo) super.getElementAt(pindex))
                    .getImportantModePrefix();
            char mode = rawmode.isEmpty() ? ' ' : rawmode.charAt(0);

            if (mode != lastmode) {
                lastmode = mode;

                if (++vindex == index) {
                    return "Mode " + lastmode;
                }
            }

            pindex++;
            vindex++;
        } while (vindex < index);

        return super.getElementAt(pindex);
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        final List<Character> modes = new ArrayList<Character>();

        for (ChannelClientInfo info : nicknames) {
            final Character mode = info.getImportantMode().isEmpty() ? ' '
                    : info.getImportantMode().charAt(0);

            if (!modes.contains(mode)) {
                modes.add(mode);
            }
        }

        return nicknames.size() + modes.size();
    }

}
