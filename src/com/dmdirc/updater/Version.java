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

package com.dmdirc.updater;

/**
 * Describes a version of a component, either as an integer or as a String which
 * corresponds to the output of `git-describe --tags`.
 *
 * @since 0.6.3
 * @author chris
 */
public class Version implements Comparable<Version> {

    protected final int intVersion;
    protected final String strVersion;

    public Version(final int version) {
        this.intVersion = version;
        this.strVersion = null;
    }

    public Version(final String version) {
        if (version.matches("^[0-9]+$")) {
            this.intVersion = Integer.parseInt(version);
            this.strVersion = null;
        } else if (version.matches("^[0-9]+(\\.[0-9]+)*(\\-[0-9]+\\-g[a-z0-9]{7})?$")) {
            this.intVersion = Integer.MIN_VALUE;
            this.strVersion = version;
        } else {
            this.intVersion = Integer.MIN_VALUE;
            this.strVersion = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final Version o) {
        if (o.intVersion > Integer.MIN_VALUE && this.intVersion > Integer.MIN_VALUE) {
            return this.intVersion - o.intVersion;
        } else if (o.strVersion == null || this.strVersion == null) {
            return 0;
        } else {
            final String myParts[] = this.strVersion.split("-");
            final String thParts[] = o.strVersion.split("-");

            final String myFirstParts[] = myParts[0].split("\\.");
            final String thFirstParts[] = thParts[0].split("\\.");

            for (int i = 0; i < Math.max(myFirstParts.length, thFirstParts.length); i++) {
                final int myInt = myFirstParts.length > i ? Integer.parseInt(myFirstParts[i]) : 0;
                final int thInt = thFirstParts.length > i ? Integer.parseInt(thFirstParts[i]) : 0;

                if (myInt != thInt) {
                    return myInt - thInt;
                }
            }

            final int myInt = myParts.length > 1 ? Integer.parseInt(myParts[1]) : 0;
            final int thInt = thParts.length > 1 ? Integer.parseInt(thParts[1]) : 0;

            return myInt - thInt;
        }
    }

    /**
     * Determines whether or not this represents a valid version.
     *
     * @return True if the version is valid, false otherwise
     */
    public boolean isValid() {
        return intVersion > Integer.MIN_VALUE || strVersion != null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return strVersion == null ? String.valueOf(intVersion) : strVersion;
    }

}
