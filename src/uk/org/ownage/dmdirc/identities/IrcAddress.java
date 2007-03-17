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

package uk.org.ownage.dmdirc.identities;

import java.util.regex.Pattern;

/**
 * An IRC Address can identify a server, ircd, network or channel (or any
 * combination thereof). Addresses take the format
 * irc://ircd:server@network/channel, which retains compatibility with
 * existing implementations that use irc://server/channel.
 * @author chris
 */
public final class IrcAddress implements Comparable {
    
    /**
     * The regular expression used for matching addresses.
     */
    private static Pattern pattern;
    
    /**
     * The default value for unknown parts of the address.
     */
    private static final String DEFAULT = "*";
    
    /**
     * The name of the ircd specified by this address.
     */
    private String ircd = DEFAULT;
    
    /**
     * The name of the server specified by this address.
     */
    private String server = DEFAULT;
    
    /**
     * The name of the network specified by this address.
     */
    private String network = DEFAULT;
    
    /**
     * The name of the channel specified by this address.
     */
    private String channel = DEFAULT;
    
    /**
     * Creates a new instance of IrcAddress, with the specified address.
     * @param address The address for this instance
     */
    public IrcAddress(final String address) {
    }
    
    /**
     * Creates a new instance of IrcAddress, with the specified server
     * details.
     * @param myIrcd The name of the IRCd that's in use
     * @param myServer The full name of the server
     * @param myNetwork The name of the network
     */
    public IrcAddress(final String myIrcd, final String myServer,
            final String myNetwork) {
        
        this(myIrcd, myServer, myNetwork, DEFAULT);
    }
    
    /**
     * Creates a new instance of IrcAddress, with the specified server
     * and channel details.
     * @param myIrcd The name of the IRCd that's in use
     * @param myServer The full name of the server
     * @param myNetwork The name of the network
     * @param myChannel The name of the channel
     */
    public IrcAddress(final String myIrcd, final String myServer,
            final String myNetwork, final String myChannel) {
        
        ircd = myIrcd;
        server = myServer;
        network = myNetwork;
        channel = myChannel;
    }

    /**
     * Compares this config source to another.
     * @param target The object to compare to.
     * @return -1 if this object is less than the other, +1 if this object is
     * greater, 0 if they're equal.
     */
    public int compareTo(final Object target) {
        return 1;
    }
    
}
