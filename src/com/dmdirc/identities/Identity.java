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

package com.dmdirc.identities;

import com.dmdirc.Config;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * An identity is a group of settings that are applied to a connection, server,
 * network or channel. Identities may be automatically applied in certain
 * cases, or the user may manually apply them.
 * @author chris
 */
public final class Identity implements ConfigSource {
        
    /** The target for this identity. */
    private final ConfigTarget myTarget = new ConfigTarget();
    
    /** The configuration details for this identity. */
    private final Properties properties;
    
    /** The file that this identity is read from. */
    private File file;
    
    /** Whether this identity needs to be saved. */
    private boolean needSave;
    
    /**
     * Creates a new instance of Identity.
     * @param file The file to load this identity from.
     * @throws com.dmdirc.identities.InvalidIdentityFileException
     * Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final File file) throws IOException,
            InvalidIdentityFileException {
        this(new FileInputStream(file));
        this.file = file;
    }
    
    /**
     * Creates a new read-only identity.
     * @param stream The input stream to read the identity from
     * @throws com.dmdirc.identities.InvalidIdentityFileException
     * Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final InputStream stream) throws IOException,
            InvalidIdentityFileException {
        properties = new Properties();
        
        properties.load(stream);
        
        if (!properties.containsKey("identity.name")) {
            throw new InvalidIdentityFileException("No name specified");
        }
               
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
        
        stream.close();
    }
    
    /**
     * Returns the properties object belonging to this identity.
     * @return This identity's property object
     */
    public Properties getProperties() {
        return properties;
    }
    
    /**
     * Returns the name of this identity.
     * @return The name of this identity
     */
    public String getName() {
        return properties.getProperty("identity.name");
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
        return properties.getProperty(domain + "." + option);
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
        needSave = true;
    }
    
    /**
     * Unsets a specified option.
     * @param domain domain of the option
     * @param option name of the option
     */
    public void unsetOption(final String domain, final String option) {
        properties.remove(domain + "." + option);
        needSave = true;
    }
    
    /**
     * Saves this identity to disk if it has been updated.
     */
    public void save() {
        if (needSave && file != null) {
            try {
                final OutputStream stream = new FileOutputStream(file);
                properties.store(stream, null);
                stream.close();
                
                needSave = false;
            } catch (FileNotFoundException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to save identity file");
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to save identity file");
            }
        }
    }
    
    /**
     * Deletes this identity from disk.
     */
    public void delete() {
        if (file != null) {
            file.delete();
        }
        
        IdentityManager.removeIdentity(this);
    }
    
    /**
     * Retrieves this identity's target.
     * @return The target of this identity
     */
    public ConfigTarget getTarget() {
        return myTarget;
    }
    
    /**
     * Returns a string representation of this object (its name).
     * @return A string representation of this object
     */
    public String toString() {
        return getName();
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
    
    /**
     * Creates a new identity containing the specified properties.
     * @param properties The properties to be included in the identity
     * @return A new identity containing the specified properties
     */
    private static Identity createIdentity(final Properties properties) {
        final String fs = System.getProperty("file.separator");
        final String location = Config.getConfigDir() + "identities" + fs;
        final String name = properties.getProperty("identity.name");
        
        final File file = new File(location + name);
        
        if (!file.exists()) {
            final FileWriter writer;
            
            try {
                writer = new FileWriter(location + name);
                properties.store(writer, "");
                writer.close();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to write new identity file");
                return null;
            }
        }
        
        try {
            final Identity identity = new Identity(file);
            IdentityManager.addIdentity(identity);
            
            return identity;
        } catch (MalformedURLException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file");
            return null;
        } catch (InvalidIdentityFileException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file");
            return null;
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file");
            return null;
        }
    }
    
    /**
     * Generates an empty identity for the specified target.
     * @param target The target for the new identity
     * @return An empty identity for the specified target
     */
    public static Identity buildIdentity(final ConfigTarget target) {
        final Properties properties = new Properties();
        properties.setProperty("identity.name", target.getData());
        properties.setProperty("identity." + target.getTypeName(), target.getData());
        
        return createIdentity(properties);
    }
    
    /**
     * Generates an empty profile witht he specified name. Note the name is used
     * as a file name, so should be sanitised.
     * @param name The name of the profile to create
     * @return A new profile with the specified name
     */
    public static Identity buildProfile(final String name) {
        final Properties properties = new Properties();
        properties.setProperty("identity.name", name);
        properties.setProperty("profile.nickname", "DMDircUser");
        properties.setProperty("profile.realname", "DMDircUser");
        
        return createIdentity(properties);
    }
    
}
