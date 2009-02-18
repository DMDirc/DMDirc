/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.themes;

import com.dmdirc.config.Identity;
import com.dmdirc.config.InvalidIdentityFileException;

import java.io.IOException;
import java.io.InputStream;

/**
 * An identity that only claims to know about settings under the UI domain,
 * for use with themes.
 *
 * @author Chris
 */
public class ThemeIdentity extends Identity {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The theme that owns this identity. */
    private final Theme theme;
    
    /**
     * Creates a new instance of ThemeIdentity.
     *
     * @param stream The input stream to read the identity from.
     * @param theme The theme that owns this identity
     * @throws InvalidIdentityFileException Missing required properties
     * @throws IOException Input/output exception
     */
    public ThemeIdentity(final InputStream stream, final Theme theme) throws IOException,
            InvalidIdentityFileException {
        super(stream, true);
        
        myTarget.setTheme();
        this.theme = theme;
    }
    
    /** {@inheritDoc} */
    @Override @Deprecated
    public boolean hasOption(final String domain, final String option) {
        if (domain.equalsIgnoreCase("ui") || domain.equalsIgnoreCase("identity")
                || domain.equalsIgnoreCase("icon")  || domain.equalsIgnoreCase("theme")
                || domain.equalsIgnoreCase("formatter") || domain.equalsIgnoreCase("colour")) {
            return super.hasOption(domain, option);
        } else {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getOption(final String domain, final String option) {
        final String result = super.getOption(domain, option);
        
        if (result == null) {
            return result;
        } else {
            return result.replaceAll("\\$theme", theme.getFileName(false));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Theme UI config: " + super.getName();
    }
    
}
