/*
 * ErrorTest.java
 * JUnit based test
 *
 * Created on 08 April 2007, 13:20
 */

package uk.org.ownage.dmdirc;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import junit.framework.*;
import java.util.Date;
import javax.swing.Icon;
import uk.org.ownage.dmdirc.ui.interfaces.StatusErrorNotifier;

/**
 *
 * @author chris
 */
public class ErrorTest extends TestCase {
    
    private Icon icon;
    private StatusErrorNotifier status;
    
    public ErrorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        icon = new ImageIcon();
        status = new StatusErrorNotifier() {
            public void clickReceived() {
            }
        };
    }
    
    public void testGetIcon() {
        final Error inst = new Error(icon, status);
        assertEquals(icon, inst.getIcon());
    }
    
    public void testGetNotifier() {
        final Error inst = new Error(icon, status);
        assertEquals(status, inst.getNotifier());
    }
    
    public void testGetDate() {
        final Error inst = new Error(icon, status);
        assertTrue(inst.getDate().after(new Date(System.currentTimeMillis() - 1000)));
        assertTrue(inst.getDate().before(new Date(System.currentTimeMillis() + 1000)));
    }
    
}
