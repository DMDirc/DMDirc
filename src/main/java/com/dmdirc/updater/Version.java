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

package com.dmdirc.updater;

import javax.annotation.Nonnull;

/**
 * Describes a version of a component, either as an integer or as a String which corresponds to the
 * output of `git-describe --tags`.
 *
 * @since 0.6.3m1
 */
public class Version implements Comparable<Version> {

    protected final int intVersion;
    protected final String strVersion;

    /**
     * Creates an invalid version.
     */
    public Version() {
        this.intVersion = Integer.MIN_VALUE;
        this.strVersion = null;
    }

    /**
     * Creates a new integer version.
     *
     * @param version Version to create
     */
    public Version(final int version) {
        this.intVersion = version;
        this.strVersion = null;
    }

    /**
     * Creates a new git version.
     *
     * @param version Git version
     */
    public Version(final String version) {
        if (version.matches("^[0-9]+$")) {
            this.intVersion = Integer.parseInt(version);
            this.strVersion = null;
        } else if (version.matches(
                "^[0-9]+(\\.[0-9]+)*((a|b|rc|m)[0-9]+)*(\\-[0-9]+\\-g[a-z0-9]{7})?(-SNAPSHOT)?$")) {
            this.intVersion = Integer.MIN_VALUE;
            this.strVersion = version.replaceAll("-SNAPSHOT$", "");
        } else {
            this.intVersion = Integer.MIN_VALUE;
            this.strVersion = null;
        }
    }

    @Override
    public int compareTo(@Nonnull final Version o) {
        if (o.intVersion > Integer.MIN_VALUE && intVersion > Integer.MIN_VALUE) {
            return intVersion - o.intVersion;
        } else if (o.strVersion == null && strVersion == null) {
            return 0;
        } else if (o.strVersion == null && strVersion != null) {
            return 1;
        } else if (o.strVersion != null && strVersion == null) {
            return -1;
        } else {
            final String[] myParts = strVersion.split("-");
            final String[] thParts = o.strVersion.split("-");

            final String[] myFirstParts = myParts[0].split("\\.|(?=a|b|rc|m)");
            final String[] thFirstParts = thParts[0].split("\\.|(?=a|b|rc|m)");

            for (int i = 0; i < Math.max(myFirstParts.length, thFirstParts.length); i++) {
                final boolean myExists = myFirstParts.length > i;
                final boolean thExists = thFirstParts.length > i;

                final boolean myIsInt = myExists && myFirstParts[i].matches("[0-9]+");
                final boolean thIsInt = thExists && thFirstParts[i].matches("[0-9]+");

                final int myInt = myIsInt ? Integer.parseInt(myFirstParts[i]) : 0;
                final int thInt = thIsInt ? Integer.parseInt(thFirstParts[i]) : 0;

                // Please consult handwritten truth table in the care of
                // Chris for an explanation of what the hell is going on here.
                // If there's a bug in this code it should probably be
                // rewritten.
                if (!myExists && !thExists
                        || myIsInt && thIsInt && myInt == thInt
                        || myExists && thExists && !myIsInt && !thIsInt
                        && myFirstParts[i].equals(thFirstParts[i])) {
                    continue;
                } else if (!thExists && myIsInt
                        || thExists && !thIsInt && (!myExists || myIsInt)) {
                    return 1;
                } else if (thIsInt && !myExists
                        || myExists && !myIsInt && (!thExists || thIsInt)) {
                    return -1;
                } else if (thIsInt && myIsInt) {
                    return myInt - thInt;
                } else {
                    final String[] myLetterParts = myFirstParts[i].split("(?=[0-9])", 2);
                    final String[] thLetterParts = thFirstParts[i].split("(?=[0-9])", 2);

                    if (myLetterParts[0].equals(thLetterParts[0])) {
                        return Integer.parseInt(myLetterParts[1])
                                - Integer.parseInt(thLetterParts[1]);
                    } else if ("m".equals(myLetterParts[0])
                            || "rc".equals(thLetterParts[0])
                            || "a".equals(myLetterParts[0]) && "b".equals(thLetterParts[0])) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }

            final int myInt = myParts.length > 1 ? Integer.parseInt(myParts[1]) : 0;
            final int thInt = thParts.length > 1 ? Integer.parseInt(thParts[1]) : 0;

            return myInt - thInt;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Version && compareTo((Version) obj) == 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + intVersion;
        hash = 17 * hash + (strVersion == null ? 0 : strVersion.hashCode());
        return hash;
    }

    /**
     * Determines whether or not this represents a valid version.
     *
     * @return True if the version is valid, false otherwise
     */
    public boolean isValid() {
        return intVersion > Integer.MIN_VALUE || strVersion != null;
    }

    @Override
    public String toString() {
        return strVersion == null ? String.valueOf(intVersion) : strVersion;
    }

}
