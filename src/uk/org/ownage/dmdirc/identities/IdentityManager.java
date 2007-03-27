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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 * The identity manager manages all known identities, providing easy methods
 * to access them.
 * @author chris
 */
public final class IdentityManager {
    
    /**
     * The identities that have been loaded into this manager.
     */
    private static ArrayList<Identity> identities;
    
    /**
     * The GlobalConfig instance to use for new ConfigManagers.
     * We only need one instance of GlobalConfig, so keep it cached here.
     */
    private static GlobalConfig globalConfig;
    
    /** Creates a new instance of IdentityManager. */
    private IdentityManager() {
    }
    
    /**
     * Loads all identity files.
     */
    public static void load() {
        final ClassLoader cldr = IdentityManager.class.getClassLoader();
        
        final String base = "uk/org/ownage/dmdirc/identities/defaults/";
        
        final String[] urls = {"asuka", "bahamut", "hyperion", "defaultprofile"};
        
        identities = new ArrayList<Identity>();
        
        // Load the defaults
        for (String url : urls) {
            try {
                final InputStream res = cldr.getResourceAsStream(base + url);
                if (res == null) {
                    Logger.error(ErrorLevel.WARNING, "Unable to load default identity: " + url);
                } else {
                    addIdentity(new Identity(res));
                }
            } catch (InvalidIdentityFileException ex) {
                Logger.error(ErrorLevel.WARNING, ex);
            } catch (IOException ex) {
                Logger.error(ErrorLevel.ERROR, ex);
            }
        }
        
        // And load the user's identities
        final String fs = System.getProperty("file.separator");
        final String location = Config.getConfigDir() + "identities" + fs;
        final File dir = new File(location);
        
        if (dir == null || dir.listFiles() == null) {
            Logger.error(ErrorLevel.WARNING, "Unable to load user identity files");
        } else {
            for (File file : dir.listFiles()) {
                try {
                    addIdentity(new Identity(file.toURI().toURL().openStream()));
                } catch (InvalidIdentityFileException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.ERROR, ex);
                }
            }
        }
        
        globalConfig = new GlobalConfig();
    }
    
    /**
     * Adds the specific identity to this manager.
     * @param identity The identity to be added
     */
    public static void addIdentity(final Identity identity) {
        identities.add(identity);
    }
    
    /**
     * Retrieves a list of identities that serve as profiles.
     * @return A list of profiles
     */
    public static ArrayList<ConfigSource> getProfiles() {
        final ArrayList<ConfigSource> profiles = new ArrayList<ConfigSource>();
        
        if (identities == null) {
            load();
        }
        
        for (Identity identity : identities) {
            if (identity.isProfile()) {
                profiles.add(identity);
            }
        }
        
        return profiles;
    }
    
    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified target.
     * @param ircd The server's ircd
     * @param network The name of the network
     * @param server The server's name
     * @param channel The channel name (in the form channel@network)
     * @return A list of all matching config sources
     */
    public static ArrayList<ConfigSource> getSources(final String ircd,
            final String network, final String server, final String channel) {
        
        final ArrayList<ConfigSource> sources = new ArrayList<ConfigSource>();
        
        String comp = "";
        
        for (ConfigSource identity : identities) {
            switch (identity.getTarget().getType()) {
                case ConfigTarget.TYPE_IRCD:
                    comp = ircd;
                    break;
                case ConfigTarget.TYPE_NETWORK:
                    comp = network;
                    break;
                case ConfigTarget.TYPE_SERVER:
                    comp = server;
                    break;
                case ConfigTarget.TYPE_CHANNEL:
                    comp = channel;
                    break;
                default:
                    comp = "<Unknown>";
                    break;
            }
            
            if (comp.equalsIgnoreCase(identity.getTarget().getData())) {
                sources.add(identity);
            }
        }
        
        sources.add(globalConfig);
        
        Collections.sort(sources);
        
        return sources;
    }
    
    /**
     * Retrieves a list of all config sources that should be applied to the
     * specified target.
     * @param ircd The server's ircd
     * @param network The name of the network
     * @param server The server's name
     * @return A list of all matching config sources
     */
    public static ArrayList<ConfigSource> getSources(final String ircd,
            final String network, final String server) {
        return getSources(ircd, network, server, "<Unknown>");
    }
    
}
