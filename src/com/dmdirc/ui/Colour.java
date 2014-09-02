/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.ui;

/**
 * A colour represented by an RGB triple. This implementation is immutable.
 */
public class Colour {

    /** The colour white. */
    public static final Colour WHITE = new Colour(255, 255, 255);
    /** The colour light gray. */
    public static final Colour LIGHT_GRAY = new Colour(192, 192, 192);
    /** The colour gray. */
    public static final Colour GRAY = new Colour(128, 128, 128);
    /** The colour dark gray. */
    public static final Colour DARK_GRAY = new Colour(64, 64, 64);
    /** The colour black. */
    public static final Colour BLACK = new Colour(0, 0, 0);
    /** The colour red. */
    public static final Colour RED = new Colour(255, 0, 0);
    /** The colour pink. */
    public static final Colour PINK = new Colour(255, 175, 175);
    /** The colour orange. */
    public static final Colour ORANGE = new Colour(255, 200, 0);
    /** The colour yellow. */
    public static final Colour YELLOW = new Colour(255, 255, 0);
    /** The colour green. */
    public static final Colour GREEN = new Colour(0, 255, 0);
    /** The colour magenta. */
    public static final Colour MAGENTA = new Colour(255, 0, 255);
    /** The colour cyan. */
    public static final Colour CYAN = new Colour(0, 255, 255);
    /** The colour blue. */
    public static final Colour BLUE = new Colour(0, 0, 255);
    /** The intensity of the red component of this colour (0-255). */
    private final int red;
    /** The intensity of the green component of this colour (0-255). */
    private final int green;
    /** The intensity of the blue component of this colour (0-255). */
    private final int blue;

    /**
     * Creates a new Colour instance with the given RGB values.
     *
     * @param red   The intensity of the red component of the colour (0-255).
     * @param green The intensity of the green component of the colour (0-255).
     * @param blue  The intensity of the blue component of the colour (0-255).
     */
    public Colour(final int red, final int green, final int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Gets the intensity of the blue component of this colour.
     *
     * @return The intensity on a scale of 0-255.
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Gets the intensity of the green component of this colour.
     *
     * @return The intensity on a scale of 0-255.
     */
    public int getGreen() {
        return green;
    }

    /**
     * Gets the intensity of the red component of this colour.
     *
     * @return The intensity on a scale of 0-255.
     */
    public int getRed() {
        return red;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Colour other = (Colour) obj;

        return this.red == other.getRed() && this.green == other.getGreen()
                && this.blue == other.getBlue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.red;
        hash = 37 * hash + this.green;
        hash = 37 * hash + this.blue;
        return hash;
    }

}
