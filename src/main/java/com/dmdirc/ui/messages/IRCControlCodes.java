/*
 * Copyright (c) 2006-2016 DMDirc Developers
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

package com.dmdirc.ui.messages;

/**
 * Contains constants for IRC 'control codes' that modify text styles.
 */
public interface IRCControlCodes {

  /** The character used for marking up bold text. */
  char BOLD = 2;
  /** The character used for marking up coloured text. */
  char COLOUR = 3;
  /** The character used for marking up coloured text (using hex). */
  char COLOUR_HEX = 4;
  /** The character used for stopping all formatting. */
  char STOP = 15;
  /** The character used for marking up fixed pitch text. */
  char FIXED = 17;
  /** The character used for negating control codes. */
  char NEGATE = 18;
  /** The character used for marking up italic text. */
  char ITALIC = 29;
  /** The character used for marking up underlined text. */
  char UNDERLINE = 31;

}
