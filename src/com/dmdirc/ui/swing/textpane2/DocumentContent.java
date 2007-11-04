/*
 * DocumentContent.java
 *
 * Created on 27-Oct-2007, 00:31:23
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dmdirc.ui.swing.textpane2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author chris
 */
public class DocumentContent implements AbstractDocument.Content {

    private static final int OFFSET_INDEX = 5000;

    private final Semaphore semaphore = new Semaphore(1, true);

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

    @Override
    public Position createPosition(final int offset) throws BadLocationException {
        return new Position() {

            private final Position p = gc.createPosition(offset);
            private final int meo = endOffset;
            private final boolean move = offset == endOffset + 1 || (offset != 0 && offset == endOffset);

            public int getOffset() {
                semaphore.acquireUninterruptibly();
                int myOffset = move ? 
                    ((offset != 0 && offset == meo) ? endOffset : endOffset + 1) : offset;
                
                if (p.getOffset() != myOffset) {
                    System.out.println("offset wrong! Mine = " + myOffset
                            + " gc's = " + p.getOffset() + " offset = " + offset
                            + " move = " + move + " meo = " + meo);
                }
                
                semaphore.release();
                return myOffset;
            }
        };
    }

    @Override
    public int length() {
        semaphore.acquireUninterruptibly();
        int res = endOffset + 1;
        semaphore.release();

        if (res != gc.length()) {
            throw new UnsupportedOperationException("Length. Me = " + res + " gc = " + gc.length());
        }

        return res;
    }

    @Override
    public UndoableEdit insertString(final int where, final String str) throws BadLocationException {
        System.out.println("insertString " + str.length());
        semaphore.acquireUninterruptibly();

        gc.insertString(where, str);
        if (where == endOffset) {
            final int newOffset = endOffset + str.length();

            data.add(str);
            endOffsets.add(newOffset);

            int offset = 1 + (endOffset % OFFSET_INDEX);

            while (newOffset % OFFSET_INDEX > offset) {
                offsetCache.put(offset++, data.size() - 1);
            }

            endOffset = newOffset;
            semaphore.release();
        } else {
            System.out.println("insertString BLE! wjere = " + where + " endOffset = " + endOffset);
            semaphore.release();
            throw new BadLocationException("Insering strings anywhere but the end = bad!", where);
        }

        //System.out.println("Done\n");
        return null;
    }

    @Override
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getString(final int where, final int len) throws BadLocationException {
        String gcr = gc.getString(where, len);

        semaphore.acquireUninterruptibly();
        if (where + len > endOffset + 1 || where < 0 || len < 0) {
            System.out.println("getString BLE!, Where = " + where + " len = "
                    + len + " endOffset = " + endOffset + " gcr = " + dump(gcr));
            semaphore.release();
            throw new BadLocationException("Invalid location/length",
                    where < 0 ? where : where + len);
        }

        //System.out.println("\n-------------------------------------------------");
        //System.out.println("Where = " + where + " len = " + len + " endOffset = "
        //        + endOffset + " gcr = " + dump(gcr));

        final StringBuilder res = new StringBuilder(len);
        int offset = 0; //findOffset(where);
        int loc = endOffset == 1 ? where : where;

        do {
            String part = data.get(offset);
            
            if (offset == data.size() - 1) {
                part = part + "\n";
            }
            
            int start = endOffsets.get(offset) - part.length();
            start = offset == data.size() - 1 ? start + 1 : start;

            int beginning = Math.max(loc - start, 0);
            int end = beginning + len - res.length();

            //System.out.println("part = " + dump(part) + " start = " + start
            //        + " beginning = " + beginning + " end = " + end);
            if (beginning < part.length()) {
                try {
                    res.append(part.substring(beginning, Math.min(part.length(), end)));
                    //System.out.println("res = " + dump(res.toString())+"\n");
                } catch (StringIndexOutOfBoundsException ex) {
                    throw new UnsupportedOperationException("Start = "
                            + beginning + " End = " + end + " Len = " + len
                            + " Alen = " + res.length(), ex);
                }
            }

            offset++;
        } while (res.length() < len && offset < data.size());

        semaphore.release();

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
        int res = offsetCache.get(where / OFFSET_INDEX);

        while (endOffsets.get(res) < where) {
            res++;
        }

        return res;
    }

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