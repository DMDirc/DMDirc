/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.commandline;

import com.dmdirc.ServerManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * An RMI server that allows other clients to interact with DMDirc.
 * 
 * @author chris
 */
public class RemoteServer implements RemoteInterface {
    
    /** The minimum port to use for RMI binding. */
    private static final int MINPORT = 3634;
    /** The maximum port to use for RMI binding. */
    private static final int MAXPORT = MINPORT + 5;
    /** The interface we're exposing. */
    private static final RemoteServer SERVER = new RemoteServer();
    
    /**
     * Creates a new instance of RemoteServer.
     */
    public RemoteServer() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void connect(final List<URI> addresses) throws RemoteException {
        for (URI address : addresses) {
            ServerManager.getServerManager().connectToAddress(address);
        }
    }    
    
    /**
     * Binds to the RMI registry so that other clients may find this remote
     * server.
     */
    public static void bind() {
        RemoteInterface stub;
        
        try {
            stub = (RemoteInterface) UnicastRemoteObject.exportObject(SERVER, 0);
        } catch (RemoteException ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to export remote interface", ex);
            return;
        }
        
        for (int port = MINPORT; port < MAXPORT; port++) {
            try {
                final Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("DMDirc", stub);
                return;
            } catch (RemoteException ex) {
                continue;
            }
        }
    }
    
    /**
     * Retrieves a reference to an existing RemoteServer, if there is one.
     * Note that this must be called before bind(), unless you want a reference
     * to our own client for some reason.
     * 
     * @return The RemoteServer instance, or null if none was available
     */
    public static RemoteInterface getServer() {
        for (int port = MINPORT; port < MAXPORT; port++) {
            try {
                final Registry registry = LocateRegistry.getRegistry("localhost", port);
                final RemoteInterface iface = (RemoteInterface) registry.lookup("DMDirc");
                
                if (iface == null) {
                    continue;
                } else {
                    return iface;
                }
            } catch (RemoteException ex) {
                continue;
            } catch (NotBoundException ex) {
                continue;
            }
        }
        
        // No RMI server found
        return null;
    }

}
