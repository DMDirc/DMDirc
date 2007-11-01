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

    public DocumentContent() {
        try {
            offsetCache.put(0, 0);

            insertString(0, "\n");
        } catch (BadLocationException ex) {
            // Something's fucked. Oh well.
        }
    }

    @Override
    public Position createPosition(final int offset) throws BadLocationException {
        return new Position() {
            public int getOffset() {
                return offset;
            }
        };
    }

    @Override
    public int length() {
        semaphore.acquireUninterruptibly();
        int res = endOffset;
        semaphore.release();
        return res + 1;
    }

    @Override
    public UndoableEdit insertString(final int where, final String str) throws BadLocationException {
        semaphore.acquireUninterruptibly();
        System.out.println("insertString(" + where + ", " + str + "). Endoffset = " + endOffset);
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
            System.out.println("BLE!");
            semaphore.release();
            throw new BadLocationException("Insering strings anywhere but the end = bad!", where);
        }
        
        System.out.println("Done\n");
        
        return null;
    }

    @Override
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getString(final int where, final int len) throws BadLocationException {
        semaphore.acquireUninterruptibly();
        System.out.println("getString(" + where + "," + len + ")");
        if (where + len > endOffset || where < 0 || len < 0) {
            System.out.println("BLE!");
            semaphore.release();
            throw new BadLocationException("Invalid location/length",
                    where < 0 ? where : where + len);
        }

        final StringBuilder res = new StringBuilder(len);
        int offset = findOffset(where);
                
        do {
            String part = data.get(offset);
            int start = endOffsets.get(offset) - part.length();
            int loc = where;
        
            int beginning = Math.max(loc - start, 0);
            int end = beginning + len - res.length();
            
            res.append(part.substring(beginning, Math.min(part.length(), end)));
            
            offset++;
        } while (res.length() < len);
        
        semaphore.release();
        
        System.out.println("Done. Response length = " + res.length() + "\n");
        
        return res.toString();
    }

    private int findOffset(final int where) {
        System.out.println(" findOffset(" + where + ")");
        int res = offsetCache.get(where / OFFSET_INDEX);

        while (endOffsets.get(res) <= where) {
            res++;
        }

        System.out.println(" = " + res);
        return res;
    }

    @Override
    public void getChars(int where, int len, Segment txt) throws BadLocationException {
        txt.array = getString(where, len).toCharArray();
        txt.offset = 0;
        txt.count = txt.array.length;
        
        System.out.println("getChars(" + where + ", " + len + ") - count = ");
    }
}