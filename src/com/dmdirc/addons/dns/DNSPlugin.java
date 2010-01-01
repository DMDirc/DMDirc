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

package com.dmdirc.addons.dns;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.Plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.List;

/**
 * DNS plugin.
 */
public final class DNSPlugin extends Plugin {
    
    /** The DNSCommand we've registered. */
    private DNSCommand command;
    
    /** Creates a new instance of DNSPlugin. */
    public DNSPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        command = new DNSCommand();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }
    
    /**
     * Returns the IP(s) for a hostname.
     *
     * @param hostname Hostname to resolve.
     *
     * @return Resolved IP(s)
     */
    public static String getIPs(final String hostname) {
        List<String> results = new ArrayList<String>();
        
        try {
            final InetAddress[] ips = InetAddress.getAllByName(hostname);
            
            for (InetAddress ip : ips) {
                results.add(ip.getHostAddress());
            }
            
        } catch (UnknownHostException ex) {
            results = new ArrayList<String>();
        }
        
        return results.toString();
    }
    
    /**
     * Returns the hostname for an ip.
     *
     * @param ip IP to resolve
     *
     * @return Resolved hostname
     */
    public static String getHostname(final String ip) {
        try {
            return InetAddress.getByName(ip).getHostName();
        } catch (UnknownHostException ex) {
            return "";
        }
    }
    
}
