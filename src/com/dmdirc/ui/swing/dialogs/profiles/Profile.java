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

package com.dmdirc.ui.swing.dialogs.profiles;

import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;

import java.util.ArrayList;
import java.util.List;

/** Profile wrapper class. */
public class Profile {

    /** Name. */
    private String name;
    /** Nickname. */
    private String nickname;
    /** Real name. */
    private String realname;
    /** Ident. */
    private String ident;
    /** Alternate nicknames. */
    private List<String> altNicknames;
    /** Does this profile need saving? */
    private boolean modified;

    /** Creates a new profile. */
    public Profile() {
        this("");
    }
    
    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     */
    public Profile(final String name) {
        this(name, "");
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     */
    public Profile(final String name, final String nickname) {
        this(name, nickname, "");
    }

    /**
     *
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     */
    public Profile(final String name, final String nickname,
            final String realname) {
        this(name, nickname, realname, "");
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     * @param ident Profile's ident
     */
    public Profile(final String name, final String nickname,
            final String realname, final String ident) {
        this(name, nickname, realname, ident,
                new ArrayList<String>());
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     * @param ident Profile's ident
     * @param altNicknames Profile's alternate nicknames list
     */
    public Profile(final String name, final String nickname,
            final String realname, final String ident,
            final List<String> altNicknames) {
        this(name, nickname, realname, ident, altNicknames, true);
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     * @param ident Profile's ident
     * @param altNicknames Profile's alternate nicknames list
     * @param modified Has this profile been modified
     */
    public Profile(final String name, final String nickname,
            final String realname, final String ident,
            final List<String> altNicknames, final boolean modified) {
        this.name = name;
        this.nickname = nickname;
        this.realname = realname;
        this.ident = ident;
        this.altNicknames = altNicknames;
        this.modified = modified;
    }

    /**
     * Returns the name of this profile.
     *
     * @return Profile's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this profile.
     *
     * @param name Profile's new name
     */
    public void setName(final String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            setModified(true);
        }
    }

    /**
     * Gets the main nickname for this profile.
     *
     * @return Profile's nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the main nickname for this profile.
     *
     * @param nickname Profile's new nickname
     */
    public void setNickname(final String nickname) {
        if (!this.nickname.equals(nickname)) {
            this.nickname = nickname;
            setModified(true);
        }
    }

    /**
     * Gets the realname for this profile.
     *
     * @return Profile's realname
     */
    public String getRealname() {
        return realname;
    }

    /**
     * Sets the realname for this profile.
     *
     * @param realname Profile's new realname
     */
    public void setRealname(final String realname) {
        if (!this.realname.equals(realname)) {
            this.realname = realname;
            setModified(true);
        }
    }

    /**
     * Gets the ident for this profile.
     *
     * @return Profile's ident
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Sets the ident for this profile.
     *
     * @param ident Profile's new ident
     */
    public void setIdent(final String ident) {
        if (this.ident == null || !this.ident.equals(ident)) {
            this.ident = ident;
            setModified(true);
        }
    }

    /**
     * Gets the alternate nicknames list for this profile.
     *
     * @return Profile's alternate nicknames list
     */
    public List<String> getAltNicknames() {
        return altNicknames;
    }

    /**
     * Sets the alternate nicknames list for this profile.
     *
     * @param altNicknames Profile's new alternate nicknames list
     */
    public void setAltNicknames(final List<String> altNicknames) {
        if (!this.altNicknames.equals(altNicknames)) {
            this.altNicknames = altNicknames;
            setModified(true);
        }
    }

    /**
     * Adds an alternate nickname to this profile.
     *
     * @param altNickname A new alternate nickname for the profile
     */
    public void addAltNickname(final String altNickname) {
        if (!altNicknames.contains(altNickname)) {
            altNicknames.add(altNickname);
            setModified(true);
        }
    }

    /**
     * Adds an alternate nickname to this profile.
     *
     * @param altNickname A new alternate nickname for the profile
     * @param position Position for the new alternate nickname
     */
    public void addAltNickname(final String altNickname, final int position) {
        if (!altNicknames.contains(altNickname)) {
            altNicknames.add(position, altNickname);
            setModified(true);
        }
    }

    /**
     * Deletes an alternate nickname from this profile.
     *
     * @param altNickname An existing alternate nickname for the profile
     */
    public void delAltNickname(final String altNickname) {
        if (altNicknames.remove(altNickname)) {
            setModified(true);
        }
    }

    /**
     * Has this profile been modified?
     *
     * @return true iif the profile has been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Sets whether the profile has been modified.
     *
     * @param modified Modified state for the profile
     */
    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    /** Saves this profile. */
    public void save() {
        if (modified) {
            final String profileString = "profile";
            final List<Identity> identities = IdentityManager.getProfiles();
            Identity profile = null;

            for (Identity identity : identities) {
                if (identity.getName().equals(name)) {
                    profile = identity;
                    break;
                }
            }

            if (profile == null) {
                profile = Identity.buildProfile(name);
            }

            if (!profile.getName().equals(name)) {
                profile.setOption("identity", "name", name);
            }
            profile.setOption(profileString, "nickname", nickname);
            profile.setOption(profileString, "realname", realname);
            profile.setOption(profileString, "ident", ident);
            profile.setOption(profileString, "altnicks", altNicknames);
        }
    }
    
    /** Deletes the profile. */
    public void delete() {
            final List<Identity> identities = IdentityManager.getProfiles();
            Identity profile = null;

            for (Identity identity : identities) {
                if (identity.getName().equals(name)) {
                    profile = identity;
                    break;
                }
            }

            if (profile == null) {
                return;
            }
            profile.delete();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Profile other = (Profile) obj;
        
        if (!this.name.equals(other.name) &&
                (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (!this.nickname.equals(other.nickname) &&
                (this.nickname == null || !this.nickname.equals(other.nickname))) {
            return false;
        }
        if (!this.realname.equals(other.realname) &&
                (this.realname == null || !this.realname.equals(other.realname))) {
            return false;
        }
        if ((this.ident == null && other.ident != null) && 
                !this.ident.equals(other.ident) &&
                (this.ident == null || !this.ident.equals(other.ident))) {
            return false;
        }
        if (this.altNicknames != other.altNicknames &&
                (this.altNicknames == null ||
                !this.altNicknames.equals(other.altNicknames))) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash =  79 * hash +
                (this.nickname != null ? this.nickname.hashCode() : 0);
        hash =  79 * hash +
                (this.realname != null ? this.realname.hashCode() : 0);
        hash = 79 * hash + (this.ident != null ? this.ident.hashCode() : 0);
        hash =  79 * hash +
                (this.altNicknames != null ? this.altNicknames.hashCode() : 0);

        return hash;
    }

    /** {@inheritDoc} */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[Profile: name='" + name + "', nickname='" + nickname +
                "', realname='" + realname + "', ident='" + ident +
                "', altNicknames='" + altNicknames + "', modified='" +
                modified + "']";
    }
}
