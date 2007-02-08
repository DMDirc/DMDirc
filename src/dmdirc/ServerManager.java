/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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

package dmdirc;

import java.util.Vector;

/**
 * The ServerManager maintains a list of all servers, and provides methods to 
 * search or iterate over them.
 * @author chris
 */
public class ServerManager {
    
    /**
     * Singleton instance of ServerManager
     */
    private static ServerManager me = null;
    
    /**
     * All servers that currently exist
     */
    private Vector servers;
    
    /**
     * Returns the singleton instance of ServerManager
     * @return Instance of ServerManager
     */
    public static ServerManager getServerManager() {
        if (me == null) {
            me = new ServerManager();
        }
        return me;
    }
    
    /** Creates a new instance of ServerManager */
    public ServerManager() {
        servers = new Vector(0, 1);
    }
    
    /**
     * Registers a new server with the manager
     * @param server The server to be registered
     */
    public void registerServer (Server server) {
        servers.add(server);
    }
    
    /**
     * Unregisters a server from the manager
     * @param server The server to be unregistered
     */
    public void unregisterServer (Server server) {
        servers.remove(server);
    }
    
    public int numServers () {
        return servers.size();
    }
    
}
