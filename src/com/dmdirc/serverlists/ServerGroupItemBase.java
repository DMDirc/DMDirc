/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.serverlists;

import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;

/**
 * Abstract base class for {@link ServerGroupItem}s.
 *
 * @author chris
 * @since 0.6.4
 */
public abstract class ServerGroupItemBase implements ServerGroupItem {

    /** Whether or not this item has been modified. */
    private boolean modified;

    /** The name of the item. */
    private String name;

    /** The name of the profile to use. */
    private String profile;

    /** {@inheritDoc} */
    @Override
    public boolean isModified() {
        return modified;
    }

    /** {@inheritDoc} */
    @Override
    public void setModified(final boolean isModified) {
        this.modified = isModified;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(final String name) {
        setModified(true);
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String getPath() {
        if (getParent() != null) {
            return getParent().getPath() + " → " + getName();
        }
        return getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getProfile() {
        return profile;
    }

    /** {@inheritDoc} */
    @Override
    public void setProfile(final String profile) {
        setModified(true);
        this.profile = profile;
    }

    /**
     * Returns the parent group of this item, or <code>null</code> if the
     * item is a root group.
     *
     * @return This item's parent group
     */
    protected abstract ServerGroup getParent();

    /**
     * Returns the {@link Identity} which corresponds to this server's desired
     * profile.
     *
     * @return This server's profile identity
     */
    protected Identity getProfileIdentity() {
        if (profile != null) {
            for (Identity identity : IdentityManager.getCustomIdentities("profile")) {
                if (profile.equals(identity.getName())) {
                    return identity;
                }
            }
        }

        if (getParent() == null) {
            return IdentityManager.getCustomIdentities("profile").get(0);
        } else {
            return getParent().getProfileIdentity();
        }
    }

}
