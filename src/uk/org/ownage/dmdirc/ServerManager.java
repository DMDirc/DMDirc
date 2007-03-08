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

package uk.org.ownage.dmdirc;

import java.util.Vector;
import javax.swing.JInternalFrame;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * The ServerManager maintains a list of all servers, and provides methods to
 * search or iterate over them.
 * @author chris
 */
public class ServerManager {
    
    /**
     * Singleton instance of ServerManager.
     */
    private static ServerManager me = null;
    
    /**
     * Indicates that the server manager is in the process of closing all
     * servers. Used to prevent concurrent access to the servers property.
     */
    private boolean closing = false;
    
    /**
     * All servers that currently exist.
     */
    private Vector<Server> servers;
    
    /** Creates a new instance of ServerManager. */
    public ServerManager() {
        servers = new Vector<Server>(0, 1);
    }
    
    /**
     * Returns the singleton instance of ServerManager.
     * @return Instance of ServerManager
     */
    public static ServerManager getServerManager() {
        if (me == null) {
            me = new ServerManager();
        }
        return me;
    }
    
    /**
     * Registers a new server with the manager.
     * @param server The server to be registered
     */
    public void registerServer(Server server) {
        servers.add(server);
        MainFrame.getMainFrame().getFrameManager().addServer(server);
    }
    
    /**
     * Unregisters a server from the manager. The request is ignored if the
     * ServerManager is in the process of closing all servers.
     * @param server The server to be unregistered
     */
    public void unregisterServer(Server server) {
        if (!closing) {
            servers.remove(server);
        }
        MainFrame.getMainFrame().getFrameManager().delServer(server);
    }
    
    /**
     * Makes all servers disconnected with the specified quit message.
     * @param message The quit message to send to the IRC servers
     */
    public void disconnectAll(String message) {
        for (Server server : servers) {
            server.disconnect(message);
        }
    }
    
    /**
     * Closes all servers with the specified quit message.
     * @param message The quit message to send to the IRC servers
     */
    public void closeAll(String message) {
        closing = true;
        for (Server server : servers) {
            server.close(message);
        }
        closing = false;
        servers.clear();
    }
    
    /**
     * Returns the number of servers that are registered with the manager.
     * @return number of registered servers
     */
    public int numServers() {
        return servers.size();
    }
    
    /**
     * Returns the server instance that owns the specified internal frame.
     * @param active The internal frame to check
     * @return The server associated with the internal frame
     */
    public Server getServerFromFrame(JInternalFrame active) {
        for (Server server : servers) {
            if (server.ownsFrame(active)) {
                return server;
            }
        }
        return null;
    }
    
}
