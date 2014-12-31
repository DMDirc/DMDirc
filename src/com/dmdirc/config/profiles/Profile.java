/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.config.profiles;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes a profile, used to describe the settings associated with the local client on a
 * connection.
 */
public class Profile {

    /** Profile Name, must be a sanitised filename. */
    private final String name;
    /** Real name. */
    private final String realname;
    /** Ident. */
    private final Optional<String> ident;
    /** Nicknames. */
    private final List<String> nicknames;

    public Profile(final String name, final String realname, final Optional<String> ident,
            final Iterable<String> nicknames) {
        this.name = name;
        this.realname = realname;
        this.ident = ident;
        this.nicknames = Lists.newArrayList(nicknames);
    }

    public String getName() {
        return name;
    }

    public String getRealname() {
        return realname;
    }

    public Optional<String> getIdent() {
        return ident;
    }

    public List<String> getNicknames() {
        return Collections.unmodifiableList(nicknames);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("realname", realname)
                .add("ident", ident)
                .add("nicknames", nicknames)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Profile profile = (Profile) o;
        return Objects.equals(name, profile.getName())
                && Objects.equals(realname, profile.getRealname())
                && Objects.equals(ident, profile.getIdent())
                && Objects.equals(nicknames, profile.getNicknames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, realname, ident, nicknames);
    }
}
