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
public class DNSPlugin extends Plugin {
    
    /** The DNSCommand we've registered. */
    private DNSCommand command;
    
    /** Creates a new instance of DNSPlugin. */
    public DNSPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        command = new DNSCommand();
    }
    
    /** {@inheritDoc} */
    public void onDeactivate() {
        CommandManager.unregisterCommand(command);
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.1";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Greboid <greg@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Provides a DNS command and method to the client.";
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "DNS plugin";
    }
    
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
    
    public static String getHostname(final String ip) {
        try {
            return InetAddress.getByName(ip).getHostName();
        } catch (UnknownHostException ex) {
            return "";
        }
    }
    
}
