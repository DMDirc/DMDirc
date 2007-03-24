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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * An identity is a group of settings that are applied to a connection, server,
 * network or channel. Identities may be automatically applied in certain
 * cases, or the user may manually apply them.
 * @author chris
 */
public final class Identity implements ConfigSource {
    
    /** The name of this identity. */
    private final String name;
    
    /** The target for this identity. */
    private final ConfigTarget myTarget = new ConfigTarget();
    
    /** The configuration details for this identity. */
    private final Properties properties;
    
    /**
     * Creates a new instance of Identity.
     * @param file The file to load this identity from.
     * @throws IOException Input error when reading the file
     * @throws uk.org.ownage.dmdirc.identities.InvalidIdentityFileException 
     * Missing required properties
     */
    public Identity(final File file) throws IOException,
            InvalidIdentityFileException {
        
        properties = new Properties();
        
        if (file.exists()) {
            properties.load(new FileInputStream(file));
            
            if (!properties.containsKey("identity.name")) {
                throw new InvalidIdentityFileException("No name specified");
            }
            
            name = getOption("identity", "name");
            
            if (hasOption("identity", "ircd")) {
                myTarget.setIrcd(getOption("identity", "ircd"));
            } else if (hasOption("identity", "network")) {
                myTarget.setNetwork(getOption("identity", "network"));
            } else if (hasOption("identity", "server")) {
                myTarget.setServer(getOption("identity", "server"));
            } else if (hasOption("identity", "channel")) {
                myTarget.setChannel(getOption("identity", "channel"));
            } else if (!isProfile()) {
                throw new InvalidIdentityFileException("No target and no profile");
            }
            
        } else {
            throw new FileNotFoundException(file.toString());
        }
    }
    
    /**
     * Returns the name of this identity.
     * @return The name of this identity
     */
    public String getName() {
        return name;
    }
    
    /**
     * Determines whether this identity can be used as a profile when
     * connecting to a server. Profiles are identities that can supply
     * nick, ident, real name, etc.
     * @return True iff this identity can be used as a profile
     */
    public boolean isProfile() {
        return hasOption("profile", "nickname") && hasOption("profile", "realname");
    }
    
    /**
     * Determines whether this identity has a setting for the specified
     * option in the specified domain.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff this source has the option, false otherwise
     */
    public boolean hasOption(final String domain, final String option) {
        return properties.containsKey(domain + "." + option);
    }
    
    /**
     * Retrieves the specified option from this identity.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the specified option
     */
    public String getOption(final String domain, final String option) {
        return (String) properties.get(domain + "." + option);
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final String value) {
        properties.setProperty(domain + "." + option, value);
    }

    /**
     * Retrieves this identity's target.
     * @return The target of this identity
     */
    public ConfigTarget getTarget() {
        return myTarget;
    }

    /**
     * Compares this identity to another config source to determine which
     * is more specific.
     * @param target The ConfigSource to compare to
     * @return -1 if this config source is less specific, 0 if they're equal,
     * +1 if this config is more specific.
     */
    public int compareTo(final ConfigSource target) {
        return target.getTarget().compareTo(myTarget);
    }
    
}
