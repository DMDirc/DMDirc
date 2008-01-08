/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.updater.components;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

import java.io.File;
import java.io.IOException;

/**
 * Represents the default identities.
 * 
 * @author chris
 */
public class DefaultsComponent implements UpdateComponent {

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "defaultsettings";
    }
    
    /** {@inheritDoc} */
    @Override
    public String getFriendlyName() {
        return "Default settings";
    }    

    /** {@inheritDoc} */
    @Override    
    public int getVersion() {
        final ConfigManager globalConfig = IdentityManager.getGlobalConfig();
        
        if (!globalConfig.hasOption("identity", "defaultsversion")) {
            return -1;
        } else {
            return globalConfig.getOptionInt("identity", "defaultsversion", -2);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws java.io.IOException On i/o exception when reading zip file
     */
    @Override
    public boolean doInstall(final String path) throws IOException {
        final ZipResourceManager ziprm = ZipResourceManager.getInstance(path);
        
        ziprm.extractResources("", IdentityManager.getDirectory());
        
        IdentityManager.loadUser();
        
        new File(path).delete();
        
        return false;
    }

}
