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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

/**
 * An AbstractDocument.Content implementation that uses linked lists to enable
 * rapid addition of text to the end of the document.
 * 
 * @author chris
 */
public class DocumentContent implements AbstractDocument.Content {

    private static final int OFFSET_INDEX = 5000;

    private int endOffset = 0;

    private final List<String> data = new LinkedList<String>();

    private final List<Integer> endOffsets = new LinkedList<Integer>();

    private final Map<Integer, Integer> offsetCache = new HashMap<Integer, Integer>();

    private GapContent gc = new GapContent();

    public DocumentContent() {
        try {
            offsetCache.put(0, 0);

            insertString(0, "");
        } catch (BadLocationException ex) {
            // Something's fucked. Oh well.
        }
    }

    /** {@inheritDoc} */
    @Override
    public Position createPosition(final int offset) throws BadLocationException {
        return new Position() {

            private final Position p = gc.createPosition(offset);
            private final int meo = endOffset;
            private final boolean move = offset == endOffset + 1 || (offset != 0 && offset == endOffset);

            @Override
            public int getOffset() {
                int myOffset = move ? 
                    ((offset != 0 && offset == meo) ? endOffset : endOffset + 1) : offset;
                
                if (p.getOffset() != myOffset) {
                    System.out.println("offset wrong! Mine = " + myOffset
                            + " gc's = " + p.getOffset() + " offset = " + offset
                            + " move = " + move + " meo = " + meo);
                }
                
                return myOffset;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        int res = endOffset + 1;

        if (res != gc.length()) {
            throw new UnsupportedOperationException("Length. Me = " + res + " gc = " + gc.length());
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public UndoableEdit insertString(final int where, final String str) throws BadLocationException {
        gc.insertString(where, str);
        if (where == endOffset) {
            final int newOffset = endOffset + str.length();

            data.add(str);
            endOffsets.add(newOffset);

            int offset = endOffset / OFFSET_INDEX;

            while (newOffset / OFFSET_INDEX > offset) {
                System.out.println("Adding offset: " + (offset + 1) + " => " + (data.size() - 1));
                offsetCache.put(++offset, data.size() - 1);
            }

            endOffset = newOffset;
        } else {
            System.out.println("insertString BLE! wjere = " + where + " endOffset = " + endOffset);
            throw new BadLocationException("Insering strings anywhere but the end = bad!", where);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getString(final int where, final int len) throws BadLocationException {
        String gcr = gc.getString(where, len);

        if (where + len > endOffset + 1 || where < 0 || len < 0) {
            System.out.println("getString BLE!, Where = " + where + " len = "
                    + len + " endOffset = " + endOffset + " gcr = " + dump(gcr));
            throw new BadLocationException("Invalid location/length",
                    where < 0 ? where : where + len);
        }

        final StringBuilder res = new StringBuilder(len);
        int offset = findOffset(where);
        int loc = where;

        do {
            String part = data.get(offset);
            
            if (offset == data.size() - 1) {
                part = part + "\n";
            }
            
            int start = endOffsets.get(offset) - part.length();
            start = offset == data.size() - 1 ? start + 1 : start;

            int beginning = Math.max(loc - start, 0);
            int end = beginning + len - res.length();

            if (beginning < part.length()) {
                try {
                    res.append(part.substring(beginning, Math.min(part.length(), end)));
                } catch (StringIndexOutOfBoundsException ex) {
                    throw new UnsupportedOperationException("Start = "
                            + beginning + " End = " + end + " Len = " + len
                            + " Alen = " + res.length(), ex);
                }
            }

            offset++;
        } while (res.length() < len && offset < data.size());

        if (!res.toString().equals(gcr)) {
            throw new UnsupportedOperationException("getString: Where = "
                    + where + " Length = " + len + " EndOffset = " + endOffset
                    + " Me = " + dump(res.toString()) + " (" + res.length()
                    + ") Gap = " + dump(gcr) + " (" + gcr.length() + ")");
        }

        return res.toString();
    }

    private String dump(final String subject) {
        final StringBuilder res = new StringBuilder();

        for (char ch : subject.toCharArray()) {
            res.append((int) ch);
            res.append('[');
            res.append(ch == '\n' ? "\\n" : ch);
            res.append(']');
            res.append(' ');
        }

        return res.toString();
    }

    private int findOffset(final int where) {
        int off = where / OFFSET_INDEX;
        while (!offsetCache.containsKey(off)) {
            System.out.println("offsetCache doesn't contain offset " + off + " (where = " + where + ")");
            off--;
        }
        
        int res = offsetCache.get(off);

        while (endOffsets.get(res) < where) {
            res++;
        }
        
        return res;
    }

    /** {@inheritDoc} */
    @Override
    public void getChars(int where, int len, Segment txt) throws BadLocationException {
        txt.array = getString(where, len).toCharArray();
        txt.offset = 0;
        txt.count = txt.array.length;

        final Segment txt2 = new Segment();
        gc.getChars(where, len, txt2);

        if (!new String(txt.array).substring(txt.offset, txt.offset + txt.count)
                .equals(new String(txt2.array).substring(txt2.offset, txt2.offset + txt2.count))) {
            throw new UnsupportedOperationException("getChars: Me = "
                    + dump(new String(txt.array).substring(txt.offset, txt.offset + txt.count))
                    + " Gap = "
                    + dump(new String(txt2.array).substring(txt2.offset, txt2.offset + txt2.count)));
        }
    }
}