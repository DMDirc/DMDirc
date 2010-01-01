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

package com.dmdirc.addons.identd;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The IdentdServer watches over the ident port when required
 *
 * @author Shane "Dataforce" Mc Cormack
 */
public final class IdentdServer implements Runnable {

    /** The Thread in use for this server */
    private volatile Thread myThread = null;

    /** The current socket in use for this server */
    private ServerSocket serverSocket;

    /** Arraylist of all the clients we have */
    private final List<IdentClient> clientList = new ArrayList<IdentClient>();

    /** The plugin that owns us. */
    private final IdentdPlugin myPlugin;

    /**
     * Create the IdentdServer.
     */
    public IdentdServer(final IdentdPlugin plugin) {
        super();
        myPlugin = plugin;
    }

    /**
     * Run this IdentdServer.
     */
    @Override
    public void run() {
        final Thread thisThread = Thread.currentThread();
        while (myThread == thisThread) {
            try {
                final Socket clientSocket = serverSocket.accept();
                final IdentClient client = new IdentClient(this, clientSocket, myPlugin);
                addClient(client);
            } catch (IOException e) {
                if (myThread == thisThread) {
                    Logger.userError(ErrorLevel.HIGH, "Accepting client failed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Add an IdentClient to the clientList
     *
     * @param client Client to add
     */
    public void addClient(final IdentClient client) {
        synchronized (clientList) {
            clientList.add(client);
        }
    }

    /**
     * Remove an IdentClient from the clientList
     *
     * @param client Client to remove
     */
    public void delClient(final IdentClient client) {
        synchronized (clientList) {
            for (int i = 0; i < clientList.size(); ++i) {
                if (clientList.get(i) == client) {
                    clientList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Check if the server is currently running
     *
     * @return True if the server is running
     */
    public boolean isRunning() {
        return (myThread != null);
    }

    /**
     * Start the ident server
     */
    public void startServer() {
        if (myThread == null) {
            try {
                final int identPort = IdentityManager.getGlobalConfig().getOptionInt(myPlugin.getDomain(), "advanced.port");
                serverSocket = new ServerSocket(identPort);
                myThread = new Thread(this);
                myThread.start();
            } catch (IOException e) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to start identd server: " + e.getMessage());
                if (e.getMessage().equals("Permission denied")) {
                    final PluginInfo plugin = PluginManager.getPluginManager().getPluginInfoByName("identd");
                    if (plugin != null) {
                        if (PluginManager.getPluginManager().delPlugin(plugin.getRelativeFilename())) {
                            PluginManager.getPluginManager().updateAutoLoad(plugin);
                        }
                    }
                }
            }
        }
    }

    /**
     * Stop the ident server
     */
    public void stopServer() {
        if (myThread != null) {
            final Thread tmpThread = myThread;
            myThread = null;
            if (tmpThread != null) {
                tmpThread.interrupt();
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
            }

            synchronized (clientList) {
                for (int i = 0; i < clientList.size(); ++i) {
                    clientList.get(i).close();
                }
                clientList.clear();
            }
        }
    }

}

