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

package com.dmdirc.config;

import com.dmdirc.Main;
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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * An identity is a group of settings that are applied to a connection, server,
 * network or channel. Identities may be automatically applied in certain
 * cases, or the user may manually apply them.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author chris
 */
public class Identity implements Serializable, Comparable<Identity> {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The domain used for identity settings. */
    private static final String DOMAIN = "identity".intern();
    
    /** The target for this identity. */
    protected final ConfigTarget myTarget;
    
    /** The configuration details for this identity. */
    protected final Properties properties;
    
    /** The config change listeners for this source. */
    protected final List<ConfigChangeListener> listeners = new ArrayList<ConfigChangeListener>();
    
    /** The file that this identity is read from. */
    protected File file;
    
    /** Whether this identity needs to be saved. */
    protected boolean needSave;
    
    /**
     * Creates a new identity with the specified properties. Saving is not
     * supported using this method (i.e., it should only be used for defaults).
     *
     * @param properties The properties to use for this identity
     */
    public Identity(final Properties properties) {        
        this(properties, null);
    }
    
    /**
     * Creates a new identity with the specified properties and targets#.
     * Saving is not supported using this method (i.e., it should only be used
     * for defaults).
     *
     * @param properties The properties to use for this identity
     * @param target The target of this identity
     */
    public Identity(final Properties properties, final ConfigTarget target) {
        Logger.doAssertion(properties != null);
        
        this.properties = properties;
        
        if (target == null) {
            myTarget = new ConfigTarget();
            myTarget.setGlobalDefault();
        } else {
            myTarget = target;
        }
    }
    
    /**
     * Creates a new instance of Identity.
     *
     * @param file The file to load this identity from
     * @param forceDefault Whether to force this identity to be loaded as default
     * identity or not
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final File file, final boolean forceDefault) throws IOException,
            InvalidIdentityFileException {        
        this(new FileInputStream(file), forceDefault);
        this.file = file;
    }
    
    /**
     * Creates a new read-only identity.
     *
     * @param stream The input stream to read the identity from
     * @param forceDefault Whether to force this identity to be loaded as default
     * identity or not
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException Input/output exception
     */
    public Identity(final InputStream stream, final boolean forceDefault) throws IOException,
            InvalidIdentityFileException {
        properties = new Properties();
        
        properties.load(stream);
        
        myTarget = new ConfigTarget();
        
        if (!properties.containsKey(DOMAIN + ".name") && !forceDefault) {
            throw new InvalidIdentityFileException("No name specified");
        }
        
        if (hasOption(DOMAIN, "ircd")) {
            myTarget.setIrcd(getOption(DOMAIN, "ircd"));
        } else if (hasOption(DOMAIN, "network")) {
            myTarget.setNetwork(getOption(DOMAIN, "network"));
        } else if (hasOption(DOMAIN, "server")) {
            myTarget.setServer(getOption(DOMAIN, "server"));
        } else if (hasOption(DOMAIN, "channel")) {
            myTarget.setChannel(getOption(DOMAIN, "channel"));
        } else if (hasOption(DOMAIN, "globaldefault")) {
            myTarget.setGlobalDefault();
        } else if (forceDefault && !isProfile()) {
            myTarget.setGlobal();
        } else if (isProfile()) {
            myTarget.setProfile();
        } else {
            throw new InvalidIdentityFileException("No target and no profile");
        }
        
        stream.close();
    }
    
    /**
     * Returns the properties object belonging to this identity.
     *
     * @return This identity's property object
     */
    public Properties getProperties() {
        return properties;
    }
    
    /**
     * Returns the name of this identity.
     *
     * @return The name of this identity
     */
    public String getName() {
        if (hasOption(DOMAIN, "name")) {
            return getOption(DOMAIN, "name");
        } else {
            return "Unnamed";
        }
    }
    
    /**
     * Determines whether this identity can be used as a profile when
     * connecting to a server. Profiles are identities that can supply
     * nick, ident, real name, etc.
     *
     * @return True iff this identity can be used as a profile
     */
    public boolean isProfile() {
        return hasOption("profile", "nickname") && hasOption("profile", "realname");
    }
    
    /**
     * Determines whether this identity has a setting for the specified
     * option in the specified domain.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff this source has the option, false otherwise
     */
    public boolean hasOption(final String domain, final String option) {
        return properties.containsKey(domain + "." + option);
    }
    
    /**
     * Retrieves the specified option from this identity.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the specified option
     */
    public String getOption(final String domain, final String option) {
        return properties.getProperty(domain + "." + option);
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final String value) {
        final String oldValue = getOption(domain, option);
        
        if ((oldValue == null && value != null) || !oldValue.equals(value)) {
            properties.setProperty(domain + "." + option, value);
            needSave = true;
            
            for (ConfigChangeListener listener : new ArrayList<ConfigChangeListener>(listeners)) {
                listener.configChanged(domain, option);
            }
        }
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final int value) {
        setOption(domain, option, String.valueOf(value));
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final boolean value) {
        setOption(domain, option, String.valueOf(value));
    }
    
    /**
     * Sets the specified option in this identity to the specified value.
     *
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    public void setOption(final String domain, final String option,
            final List<String> value) {
        final StringBuilder temp = new StringBuilder();
        for (String part : value) {
            temp.append('\n');
            temp.append(part);
        }
        setOption(domain, option, temp.length() > 0 ? temp.substring(1) : temp.toString());
    }
    
    /**
     * Unsets a specified option.
     *
     * @param domain domain of the option
     * @param option name of the option
     */
    public void unsetOption(final String domain, final String option) {
        properties.remove(domain + "." + option);
        needSave = true;
        
        for (ConfigChangeListener listener : new ArrayList<ConfigChangeListener>(listeners)) {
            listener.configChanged(domain, option);
        }
    }
    
    /**
     * Returns a list of options avaiable in this identity.
     *
     *  @return Option list
     */
    public List<String> getOptions() {
        final List<String> res = new ArrayList<String>();
        
        for (Object key : properties.keySet()) {
            res.add((String) key);
        }
        
        return res;
    }
    
    /**
     * Saves this identity to disk if it has been updated.
     */
    public void save() {
        if (needSave && file != null) {
            if (myTarget.getType() == ConfigTarget.TYPE.GLOBAL) {
                // If we're the global config, unset useless settings that are
                // covered by global defaults.
                
                final ConfigManager globalConfig = new ConfigManager("", "", "");
                globalConfig.removeIdentity(this);
                
                for (Object key : new HashSet<Object>(properties.keySet())) {
                    final String domain = ((String) key).substring(0, ((String) key).indexOf('.'));
                    final String option = ((String) key).substring(1 + ((String) key).indexOf('.'));
                    final String global = globalConfig.getOption(domain, option, null);
                    if (properties.getProperty((String) key).equals(global) || "temp".equals(domain)) {
                        properties.remove(key);
                    }
                }
            }
            
            try {
                final OutputStream stream = new FileOutputStream(file);
                properties.store(stream, null);
                stream.close();
                
                needSave = false;
            } catch (FileNotFoundException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to save identity file: " + ex.getMessage());
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to save identity file: " + ex.getMessage());
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
     *
     * @return The target of this identity
     */
    public ConfigTarget getTarget() {
        return myTarget;
    }
    
    /**
     * Adds a new config change listener for this identity.
     *
     * @param listener The listener to be added
     */
    public void addListener(final ConfigChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the specific config change listener from this identity.
     *
     * @param listener The listener to be removed
     */
    public void removeListener(final ConfigChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Returns a string representation of this object (its name).
     *
     * @return A string representation of this object
     */
    @Override
    public String toString() {
        return getName();
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getName().hashCode() + getTarget().hashCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Identity
                && getName().equals(((Identity) obj).getName())
                && getTarget() == ((Identity) obj).getTarget()) {
            return true;
        }
        return false;
    }
    
    /**
     * Compares this identity to another config source to determine which
     * is more specific.
     *
     * @param target The Identity to compare to
     * @return -1 if this config source is less specific, 0 if they're equal,
     * +1 if this config is more specific.
     */
    public int compareTo(final Identity target) {
        return target.getTarget().compareTo(myTarget);
    }
    
    /**
     * Creates a new identity containing the specified properties.
     *
     * @param properties The properties to be included in the identity
     * @return A new identity containing the specified properties
     */
    protected static Identity createIdentity(final Properties properties) {
        if (!properties.containsKey(DOMAIN + ".name")
                || properties.getProperty(DOMAIN + ".name").isEmpty()) {
            Logger.appError(ErrorLevel.LOW, "createIdentity caleld with invalid identity",
                    new InvalidIdentityFileException("identity.name is not set"));
            return null;
        }
        
        final String fs = System.getProperty("file.separator");
        final String location = Main.getConfigDir() + "identities" + fs;
        final String name = properties.getProperty(DOMAIN + ".name");
        
        final File file = new File(location + name);
        
        if (!file.exists()) {
            final FileWriter writer;
            
            try {
                writer = new FileWriter(location + name);
                properties.store(writer, "");
                writer.close();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to write new identity file: " + ex.getMessage());
                return null;
            }
        }
        
        try {
            final Identity identity = new Identity(file, false);
            IdentityManager.addIdentity(identity);
            
            return identity;
        } catch (MalformedURLException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file: " + ex.getMessage());
            return null;
        } catch (InvalidIdentityFileException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file: " + ex.getMessage());
            return null;
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to open new identity file: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * Generates an empty identity for the specified target.
     *
     * @param target The target for the new identity
     * @return An empty identity for the specified target
     */
    public static Identity buildIdentity(final ConfigTarget target) {
        final Properties properties = new Properties();
        properties.setProperty(DOMAIN + ".name", target.getData());
        properties.setProperty(DOMAIN + "." + target.getTypeName(), target.getData());
        
        return createIdentity(properties);
    }
    
    /**
     * Generates an empty profile witht he specified name. Note the name is used
     * as a file name, so should be sanitised.
     *
     * @param name The name of the profile to create
     * @return A new profile with the specified name
     */
    public static Identity buildProfile(final String name) {
        final Properties properties = new Properties();
        properties.setProperty(DOMAIN + ".name", name);
        properties.setProperty("profile.nickname", "DMDircUser");
        properties.setProperty("profile.realname", "DMDircUser");
        
        return createIdentity(properties);
    }
    
}
