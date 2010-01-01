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

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The IdentClient responds to an ident request.
 *
 * @author Shane "Dataforce" Mc Cormack
 */
public final class IdentClient implements Runnable {

    /** The IdentdServer that owns this Client. */
    private final IdentdServer myServer;

    /** The Socket that we are in charge of. */
    private final Socket mySocket;

    /** The Thread in use for this client. */
    private volatile Thread myThread;

    /** The plugin that owns us. */
    private final IdentdPlugin myPlugin;

    /**
     * Create the IdentClient.
     *
     * @param server The server that owns this
     * @param socket The socket we are handing
     */
    public IdentClient(final IdentdServer server, final Socket socket, final IdentdPlugin plugin) {
        myServer = server;
        mySocket = socket;
        myPlugin = plugin;

        myThread = new Thread(this);
        myThread.start();
    }

    /**
     * Process this connection.
     */
    @Override
    public void run() {
        final Thread thisThread = Thread.currentThread();
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(mySocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            final String inputLine;
            if ((inputLine = in.readLine()) != null) {
                out.println(getIdentResponse(inputLine, IdentityManager.getGlobalConfig()));
            }
        } catch (IOException e) {
            if (thisThread == myThread) {
                Logger.userError(ErrorLevel.HIGH, "ClientSocket Error: " + e.getMessage());
            }
        } finally {
            try {
                out.close();
                in.close();
                mySocket.close();
            } catch (IOException e) {
            }
        }
        myServer.delClient(this);
    }

    /**
     * Get the ident response for a given line.
     * Complies with rfc1413 (http://www.faqs.org/rfcs/rfc1413.html)
     *
     * @param input Line to generate response for
     * @param config The config manager to use for settings
     * @return the ident response for the given line
     */
    protected String getIdentResponse(final String input, final ConfigManager config) {
        final String unescapedInput = unescapeString(input);
        final String[] bits = unescapedInput.replaceAll("\\s+", "").split(",", 2);
        if (bits.length < 2) {
            return String.format("%s : ERROR : X-INVALID-INPUT", escapeString(unescapedInput));
        }
        final int myPort;
        final int theirPort;
        try {
            myPort = Integer.parseInt(bits[0].trim());
            theirPort = Integer.parseInt(bits[1].trim());
        } catch (NumberFormatException e) {
            return String.format("%s , %s : ERROR : X-INVALID-INPUT", escapeString(bits[0]), escapeString(bits[1]));
        }

        if (myPort > 65535 || myPort < 1 || theirPort > 65535 || theirPort < 1) {
            return String.format("%d , %d : ERROR : INVALID-PORT", myPort, theirPort);
        }

        final Server server = getServerByPort(myPort);
        if (!config.getOptionBool(myPlugin.getDomain(), "advanced.alwaysOn") && (server == null || config.getOptionBool(myPlugin.getDomain(), "advanced.isNoUser"))) {
            return String.format("%d , %d : ERROR : NO-USER", myPort, theirPort);
        }

        if (config.getOptionBool(myPlugin.getDomain(), "advanced.isHiddenUser")) {
            return String.format("%d , %d : ERROR : HIDDEN-USER", myPort, theirPort);
        }

        final String osName = System.getProperty("os.name").toLowerCase();
        final String os;
        final String username;

        final String customSystem = config.getOption(myPlugin.getDomain(), "advanced.customSystem");
        if (config.getOptionBool(myPlugin.getDomain(), "advanced.useCustomSystem") && customSystem != null && customSystem.length() > 0 && customSystem.length() < 513) {
            os = customSystem;
        } else {
            // Tad excessive maybe, but complete!
            // Based on: http://mindprod.com/jgloss/properties.html
            // and the SYSTEM NAMES section of rfc1340 (http://www.faqs.org/rfcs/rfc1340.html)
            if (osName.startsWith("windows")) {
                os = "WIN32";
            } else if (osName.startsWith("mac")) {
                os = "MACOS";
            } else if (osName.startsWith("linux")) {
                os = "UNIX";
            } else if (osName.indexOf("bsd") > -1) {
                os = "UNIX-BSD";
            } else if ("os/2".equals(osName)) {
                os = "OS/2";
            } else if (osName.indexOf("unix") > -1) {
                os = "UNIX";
            } else if ("irix".equals(osName)) {
                os = "IRIX";
            } else {
                os = "UNKNOWN";
            }
        }

        final String customName = config.getOption(myPlugin.getDomain(), "general.customName");
        if (config.getOptionBool(myPlugin.getDomain(), "general.useCustomName") && customName != null && customName.length() > 0 && customName.length() < 513) {
            username = customName;
        } else if (server != null && config.getOptionBool(myPlugin.getDomain(), "general.useNickname")) {
            username = server.getParser().getLocalClient().getNickname();
        } else if (server != null && config.getOptionBool(myPlugin.getDomain(), "general.useUsername")) {
            username = server.getParser().getLocalClient().getUsername();
        } else {
            username = System.getProperty("user.name");
        }

        return String.format("%d , %d : USERID : %s : %s", myPort, theirPort, escapeString(os), escapeString(username));
    }

    /**
     * Escape special chars.
     *
     * @param str String to escape
     * @return Escaped string.
     */
    public static String escapeString(final String str) {
        return str.replaceAll("\\\\", "\\\\\\\\").replaceAll(":", "\\\\:").replaceAll(",", "\\\\,").replaceAll(" ", "\\\\ ");
    }

    /**
     * Unescape special chars.
     *
     * @param str String to escape
     * @return Escaped string.
     */
    public static String unescapeString(final String str) {
        return str.replaceAll("\\\\:", ":").replaceAll("\\\\ ", " ").replaceAll("\\\\,", ",").replaceAll("\\\\\\\\", "\\\\");
    }

    /**
     * Close this IdentClient.
     */
    public void close() {
        if (myThread != null) {
            final Thread tmpThread = myThread;
            myThread = null;
            if (tmpThread != null) {
                tmpThread.interrupt();
            }
            try {
                mySocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Retrieves the server that is bound to the specified local port.
     *
     * @param port Port to check for
     * @return The server instance listening on the given port
     */
    protected static Server getServerByPort(final int port) {
        for (Server server : ServerManager.getServerManager().getServers()) {
            if (server.getParser().getLocalPort() == port) {
                return server;
            }
        }
        return null;
    }

}

