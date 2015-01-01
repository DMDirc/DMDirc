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

package com.dmdirc.ui.core.profiles;

import com.dmdirc.config.profiles.Profile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable version of {@link Profile}.
 */
public class MutableProfile {

    private String name;
    private String realname;
    private Optional<String> ident;
    private List<String> nicknames;
    private List<String> highlights;

    public MutableProfile(final Profile profile) {
        this(profile.getName(), profile.getRealname(), profile.getIdent(), profile.getNicknames(),
                profile.getHighlights());
    }

    public MutableProfile(final String name, final String realname, final Optional<String> ident,
            final Iterable<String> nicknames) {
        this(name, realname, ident, nicknames, Collections.emptyList());
    }

    public MutableProfile(final String name, final String realname, final Optional<String> ident,
            final Iterable<String> nicknames, final Iterable<String> highlights) {
        this.name = name;
        this.realname = realname;
        this.ident = ident;
        this.nicknames = Lists.newArrayList(nicknames);
        this.highlights = Lists.newArrayList(highlights);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(final String realname) {
        this.realname = realname;
    }

    public Optional<String> getIdent() {
        return ident;
    }

    public void setIdent(final Optional<String> ident) {
        this.ident = ident;
    }

    public List<String> getNicknames() {
        return Lists.newArrayList(nicknames);
    }

    public void setNicknames(final Iterable<String> nicknames) {
        this.nicknames = Lists.newArrayList(nicknames);
    }

    public void setNickname(final int index, final String newNickname) {
        nicknames.set(index, newNickname);
    }

    public void removeNickname(final String nickname) {
        nicknames.remove(nickname);
    }

    public void addNickname(final String nickname) {
        nicknames.add(nickname);
    }

    public List<String> getHighlights() {
        return Lists.newArrayList(highlights);
    }

    public void setHighlights(final Iterable<String> highlights) {
        this.highlights = Lists.newArrayList(highlights);
    }

    public void setHighlight(final int index, final String newHighlight) {
        highlights.set(index, newHighlight);
    }

    public void removeHighlight(final String highlight) {
        highlights.remove(highlight);
    }

    public void addHighlight(final String highlight) {
        highlights.add(highlight);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", name)
                .add("Realname", realname)
                .add("Ident", ident)
                .add("Nicknames", nicknames)
                .add("Highlights", highlights)
                .toString();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final MutableProfile profile = (MutableProfile) object;
        return Objects.equals(name, profile.getName())
                && Objects.equals(realname, profile.getRealname())
                && Objects.equals(ident, profile.getIdent())
                && Objects.equals(nicknames, profile.getNicknames())
                && Objects.equals(highlights, profile.getHighlights());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, realname, ident, nicknames, highlights);
    }
}
