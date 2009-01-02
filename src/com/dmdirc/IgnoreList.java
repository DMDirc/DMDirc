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

package com.dmdirc;

import com.dmdirc.parser.irc.RegexStringList;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps around a RegexStringList to allow "simple" expressions to be used
 * instead of more complex regular expressions.
 *
 * @author chris
 */
public class IgnoreList extends RegexStringList {

    /**
     * Creates a new instance of IgnoreList.
     */
    public IgnoreList() {
        super();
    }

    /**
     * Creates a new instance of IgnoreList containing the specified items.
     *
     * @param items The items to be added
     */
    public IgnoreList(final List<String> items) {
        super(items);
    }

    /**
     * Adds the specified simple pattern to this ignore list.
     *
     * @param pattern The simple pattern to be added
     */
    public void addSimple(final String pattern) {
        add(simpleToRegex(pattern));
    }

    /**
     * Determines if this list can be converted to a simple list.
     *
     * @return True if this list can be converted, false otherwise.
     */
    public boolean canConvert() {
        try {
            getSimpleList();
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    /**
     * Retrieves a list of regular expressions in this ignore list.
     *
     * @return All expressions in this ignore list
     */
    public List<String> getRegexList() {
        return new ArrayList<String>(ignoreInfo);
    }

    /**
     * Retrieves a list of simple expressions in this ignore list.
     *
     * @return All expressions in this ignore list, converted to simple expressions
     * @throws UnsupportedOperationException if an expression can't be converted
     */
    public List<String> getSimpleList() throws UnsupportedOperationException {
        final List<String> res = new ArrayList<String>();

        for (String regex : ignoreInfo) {
            res.add(regexToSimple(regex));
        }

        return res;
    }

    /**
     * Converts a regular expression into a simple expression.
     *
     * @param regex The regular expression to be converted
     * @return A simple expression corresponding to the regex
     * @throws UnsupportedOperationException if the regex cannot be converted
     */
    protected static String regexToSimple(final String regex)
            throws UnsupportedOperationException {
        final StringBuilder res = new StringBuilder(regex.length());
        boolean escaped = false;
        boolean inchar = false;

        for (char part : regex.toCharArray()) {
            if (inchar) {
                inchar = false;

                if (part == '*') {
                    res.append(part);
                    continue;
                } else {
                    res.append('?');
                }
            }

            if (escaped) {
                if (part == '?' || part == '*') {
                    throw new UnsupportedOperationException("Cannot convert to"
                            + " simple expression: ? or * is escaped.");
                }

                res.append(part);
                escaped = false;
            }  else if (part == '\\') {
                escaped = true;
            } else if (part == '.') {
                inchar = true;
            } else if (part == '.' || part == '^' || part == '$' || part == '['
                    || part == ']' || part == '\\' || part == '(' || part == ')'
                    || part == '{' || part == '}' || part == '|' || part == '+'
                    || part == '*' || part == '?') {
                throw new UnsupportedOperationException("Cannot convert to"
                        + " simple expression: unescaped special char: " + part);
            } else {
                res.append(part);
            }
        }

        if (escaped) {
            throw new UnsupportedOperationException("Cannot convert to "
                    + "simple expression: trailing backslash");
        } else if (inchar) {
            res.append('?');
        }

        return res.toString();
    }

    /**
     * Converts a simple expression to a regular expression.
     *
     * @param regex The simple expression to be converted
     * @return A corresponding regular expression
     */
    @SuppressWarnings("fallthrough")
    protected static String simpleToRegex(final String regex) {
        final StringBuilder res = new StringBuilder(regex.length());

        for (char part : regex.toCharArray()) {
            switch (part) {
            case '.': case '^': case '$': case '[': case ']': case '\\':
            case '(': case ')': case '{': case '}': case '|': case '+':
                res.append('\\');
                res.append(part);
                break;
            case '?':
                res.append('.');
                break;
            case '*':
                res.append('.');
            default:
                res.append(part);
                break;
            }
        }

        return res.toString();
    }

}
