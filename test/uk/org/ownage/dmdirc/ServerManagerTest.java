/*
 * ServerManagerTest.java
 * JUnit based test
 *
 * Created on 08 April 2007, 12:55
 */

package uk.org.ownage.dmdirc;

import javax.swing.JInternalFrame;
import junit.framework.*;
import uk.org.ownage.dmdirc.identities.IdentityManager;

/**
 *
 * @author chris
 */
public class ServerManagerTest extends TestCase {
    
    public ServerManagerTest(String testName) {
        super(testName);
    }
    
    public void testGetServerManager() {
        final ServerManager resultA = ServerManager.getServerManager();
        final ServerManager resultB = ServerManager.getServerManager();
        
        assertNotNull(resultA);
        assertTrue(resultA instanceof ServerManager);
        assertEquals(resultA, resultB);
    }
    
    public void testRegisterServer() {
        final Server server = new Server("127.0.0.1", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(1, instance.numServers());
        
        server.close();
    }
    
    public void testUnregisterServer() {
        final Server server = new Server("127.0.0.1", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        server.close();
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(0, instance.numServers());
    }    
    
    public void testGetServerFromFrame() {
        final Server serverA = new Server("127.0.0.1", 6667, "", false, IdentityManager.getProfiles().get(0));
        final Server serverB = new Server("127.0.0.2", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServerFromFrame((JInternalFrame) serverA.getFrame()));
        assertEquals(serverB, sm.getServerFromFrame((JInternalFrame) serverB.getFrame()));
        
        serverA.close();
        serverB.close();
    }
}
