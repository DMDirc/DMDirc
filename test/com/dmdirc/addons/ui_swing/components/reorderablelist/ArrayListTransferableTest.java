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

import com.dmdirc.addons.ui_swing.components.reorderablelist.ArrayListTransferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayListTransferableTest {
    
    ArrayList<String> testData;
    
    @Before
    public void setUp() {
        testData = new ArrayList<String>();
        testData.add("abc");
        testData.add("def");
    }

    @Test(expected=UnsupportedFlavorException.class)
    public void getTransferDataInvalid() throws Exception {
        final ArrayListTransferable alt = new ArrayListTransferable(testData);
        alt.getTransferData(null);
    }
    
    @Test
    public void getTransferData() throws Exception {
        final ArrayListTransferable alt = new ArrayListTransferable(testData);
        
        assertSame(testData, alt.getTransferData(alt.getTransferDataFlavors()[0]));
    }    

    @Test
    public void isDataFlavorSupported1() {
        final ArrayListTransferable alt = new ArrayListTransferable(testData);
        
        assertTrue(alt.isDataFlavorSupported(alt.getTransferDataFlavors()[0]));
    }
    
    @Test
    public void isDataFlavorSupported2() {
        final ArrayListTransferable alt = new ArrayListTransferable(testData);
        
        assertFalse(alt.isDataFlavorSupported(null));
    }

}