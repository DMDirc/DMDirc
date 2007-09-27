/*
 * ServerManagerTest.java
 * JUnit 4.x based test
 *
 * Created on 05 June 2007, 23:06
 */

package com.dmdirc;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.dummy.DummyController;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author chris
 */
public class ServerManagerTest extends TestCase {
    
    public ServerManagerTest() {
        Main.setUI(new DummyController());
        IdentityManager.load();
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testGetServerManager() {
        final ServerManager resultA = ServerManager.getServerManager();
        final ServerManager resultB = ServerManager.getServerManager();
        
        assertNotNull(resultA);
        assertTrue(resultA instanceof ServerManager);
        assertEquals(resultA, resultB);
    }
    
    @Test
    public void testRegisterServer() {
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(1, instance.numServers());
        
        server.close();
    }
    
    @Test
    public void testUnregisterServer() {
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        server.close();
        
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(0, instance.numServers());
    }
    
    @Test
    public void testNumServers() {
        final ServerManager instance = ServerManager.getServerManager();
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        final Server server = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        assertEquals(instance.getServers().size(), instance.numServers());
        
        server.close();
        
        assertEquals(instance.getServers().size(), instance.numServers());
    }
    
    @Test
    public void testGetServerFromFrame() {
        final Server serverA = new Server("255.255.255.255", 6667, "", false, IdentityManager.getProfiles().get(0));
        final Server serverB = new Server("255.255.255.254", 6667, "", false, IdentityManager.getProfiles().get(0));
        
        final ServerManager sm = ServerManager.getServerManager();
        
        assertEquals(serverA, sm.getServerFromFrame(serverA.getFrame()));
        assertEquals(serverB, sm.getServerFromFrame(serverB.getFrame()));
        
        serverA.close();
        serverB.close();
    }
    
}