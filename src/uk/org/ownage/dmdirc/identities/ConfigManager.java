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

import java.util.ArrayList;

/**
 * The config manager manages the various config sources for each entity.
 * @author chris
 */
public final class ConfigManager {
    
    /** A list of sources for this config manager. */
    private ArrayList<ConfigSource> sources;
    
    /**
     * Creates a new instance of ConfigManager.
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     */
    public ConfigManager(final String ircd, final String network,
            final String server) {
        this(ircd, network, server, "<Unknown>");
    }
    
    /**
     * Creates a new instance of ConfigManager.
     * @param ircd The name of the ircd for this manager
     * @param network The name of the network for this manager
     * @param server The name of the server for this manager
     * @param channel The name of the channel for this manager
     */
    public ConfigManager(final String ircd, final String network,
            final String server, final String channel) {
        final String chanName = channel + "@" + network;
        sources = IdentityManager.getSources(ircd, network, server, chanName);
    }
    
    /**
     * Retrieves the specified option.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the option
     */
    public String getOption(final String domain, final String option) {
        for (ConfigSource source : sources) {
            if (source.hasOption(domain, option)) {
                return source.getOption(domain, option);
            }
        }
        
        throw new IndexOutOfBoundsException("Config option not found: " + domain + "." + option);
    }
    
    /**
     * Determines if this manager has the specified option.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff the option exists, false otherwise.
     */
    public boolean hasOption(final String domain, final String option) {
        for (ConfigSource source : sources) {
            if (source.hasOption(domain, option)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retrieves a list of sources for this config manager.
     * @return This config manager's sources.
     */
    public ArrayList<ConfigSource> getSources() {
        return sources;
    }
}
