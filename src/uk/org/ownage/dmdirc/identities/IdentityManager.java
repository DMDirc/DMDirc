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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

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
        
        try {
            
            final URL url = cldr.getResource("uk/org/ownage/dmdirc/identities/defaults/");
            final File[] files = new File(url.toURI()).listFiles();
            
            for (int i = 0; i < files.length; i++) {
                try {
                    if (!files[i].isDirectory()) {
                        addIdentity(new Identity(files[i]));
                    }
                } catch (InvalidIdentityFileException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.ERROR, ex);
                }
            }
            
        } catch (URISyntaxException ex) {
            Logger.error(ErrorLevel.ERROR, ex);
        }
        
        globalConfig = new GlobalConfig();
    }
    
    /**
     * Adds the specific identity to this manager.
     * @param identity The identity to be added
     */
    public static void addIdentity(final Identity identity) {
        if (identities == null) {
            identities = new ArrayList<Identity>();
        }
        
        identities.add(identity);
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
                default:
                    comp = "<Unknown>";
                    break;
            }
            
            if (comp.equalsIgnoreCase(identity.getTarget().getData())) {
                sources.add(identity);
            }
        }
        
        Collections.sort(sources);
        
        return sources;
    }
    
}
