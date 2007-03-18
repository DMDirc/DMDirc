/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc.identities;

/**
 * A Config source is an object that can provide configuration details.
 * @author chris
 */
public interface ConfigSource extends Comparable {
    
    /**
     * Determines whether this config source has a setting for the specified
     * option in the specified domain.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return True iff this source has the option, false otherwise
     */
    boolean hasOption(String domain, String option);
    
    /**
     * Retrieves the specified option from this config source.
     * @param domain The domain of the option
     * @param option The name of the option
     * @return The value of the specified option
     */
    String getOption(String domain, String option);
    
    /**
     * Sets the specified option in this source to the specified value.
     * @param domain The domain of the option
     * @param option The name of the option
     * @param value The new value for the option
     */
    void setOption(String domain, String option, String value);
    
    /**
     * Retrieves the target of this config source.
     * @return This source's target
     */
    ConfigTarget getTarget();
    
}
