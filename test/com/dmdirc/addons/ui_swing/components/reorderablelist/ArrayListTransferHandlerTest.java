/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.components.reorderablelist;

import com.dmdirc.addons.ui_swing.components.reorderablelist.ArrayListTransferHandler;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ArrayListTransferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.TransferHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayListTransferHandlerTest {


    @Test
    public void testCreateTransferrable() throws IOException, UnsupportedFlavorException {
        final ArrayList<String> test = new ArrayList<String>();
        test.add("abc");
        test.add("def");
        test.add(null);
        
        final JList list = new JList(test.toArray());
        list.setSelectedIndex(0);
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final Transferable transferable = handler.createTransferable(list);
        
        assertNotNull(transferable);
        final Object data = transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
        
        assertTrue(data instanceof ArrayList);
        assertEquals(1, ((ArrayList) data).size());
        assertEquals("abc", ((ArrayList) data).get(0));
    }
    
    @Test
    public void testCreateTransferrable2() throws IOException, UnsupportedFlavorException {
        final ArrayList<String> test = new ArrayList<String>();
        test.add("abc");
        test.add("def");
        test.add(null);
        
        final JList list = new JList(test.toArray());
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final Transferable transferable = handler.createTransferable(list);
        
        assertNull(transferable);
    }    
    
    @Test
    public void testCreateTransferrable3() throws IOException, UnsupportedFlavorException {
        final ArrayList<Object> test = new ArrayList<Object>();
        test.add(new Object() {

            @Override
            public String toString() {
                return null;
            }
            
        });
        test.add("def");
        test.add(null);
        
        final JList list = new JList(test.toArray());
        list.setSelectedIndex(0);
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final Transferable transferable = handler.createTransferable(list);
        
        assertNotNull(transferable);
        final Object data = transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
        
        assertTrue(data instanceof ArrayList);
        assertEquals(1, ((ArrayList) data).size());
        assertEquals("", ((ArrayList) data).get(0));
    }    
    
    @Test
    public void testCreateTransferrable4() throws IOException, UnsupportedFlavorException {
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final Transferable transferable = handler.createTransferable(null);
        
        assertNull(transferable);
    }
    
    @Test
    public void testCanImport() {
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final ArrayListTransferable transferable = new ArrayListTransferable(null);
        final JList list = new JList(new DefaultListModel());
        
        for (DataFlavor df : transferable.getTransferDataFlavors()) {
            assertTrue(handler.canImport(list, new DataFlavor[]{df}));
            assertFalse(handler.canImport(null, new DataFlavor[]{df}));
        }
        
        assertFalse(handler.canImport(list, new DataFlavor[]{}));
    }
    
    @Test
    public void testImportData() {
        final ArrayList<String> test = new ArrayList<String>();
        test.add("abc");
        test.add("def");
        test.add(null);
        
        final JList list = new JList(new DefaultListModel());
        ((DefaultListModel) list.getModel()).addElement("123");
        ((DefaultListModel) list.getModel()).addElement("456");
        list.setSelectedIndex(0);
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final ArrayListTransferable alt = new ArrayListTransferable(test);
        
        handler.createTransferable(new JList(new DefaultListModel()));
        
        assertTrue(handler.importData(list, alt));
        assertEquals(5, list.getModel().getSize());
        assertEquals("123", list.getModel().getElementAt(0));
        assertEquals("abc", list.getModel().getElementAt(1));
        assertEquals(null, list.getModel().getElementAt(3));
        assertEquals("456", list.getModel().getElementAt(4));
    }
    
    @Test
    public void testImportData2() {
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final ArrayListTransferable alt = new ArrayListTransferable(null);
        assertFalse(handler.importData(null, alt));
    }
    
    @Test
    public void testExportDone() {
        final ArrayList<String> test = new ArrayList<String>();
        test.add("123");
        test.add("456");
        
        final JList list = new JList(new DefaultListModel());
        ((DefaultListModel) list.getModel()).addElement("123");
        ((DefaultListModel) list.getModel()).addElement("456");
        list.setSelectedIndex(0);
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final ArrayListTransferable alt = new ArrayListTransferable(test);
        
        handler.createTransferable(list);
        
        final JList list2 = new JList(new DefaultListModel());
        list2.setSelectedIndex(0);
        assertTrue(handler.importData(list2, alt));
        
        handler.exportDone(list, alt, TransferHandler.MOVE);
        assertEquals(1, list.getModel().getSize());
        assertEquals("456", list.getModel().getElementAt(0));
    }
    
    @Test
    public void testExportDone2() {
        final ArrayList<String> test = new ArrayList<String>();
        test.add("123");
        test.add("456");
        
        final JList list = new JList(new DefaultListModel());
        ((DefaultListModel) list.getModel()).addElement("123");
        ((DefaultListModel) list.getModel()).addElement("456");
        list.setSelectedIndex(0);
        
        final ArrayListTransferHandler handler = new ArrayListTransferHandler();
        final ArrayListTransferable alt = new ArrayListTransferable(test);
        
        handler.createTransferable(list);
        
        final JList list2 = new JList(new DefaultListModel());
        list2.setSelectedIndex(0);
        assertTrue(handler.importData(list2, alt));
        
        handler.exportDone(list, alt, TransferHandler.COPY);
        assertEquals(2, list.getModel().getSize());
        assertEquals("123", list.getModel().getElementAt(0));
        assertEquals("456", list.getModel().getElementAt(1));
    }        

}