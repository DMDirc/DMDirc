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
 * The update component interface defines the methods needed to be implemented
 * by updatable components. The components handle determining the current
 * version and installing updated files.
 * 
 * @author chris
 */
public interface UpdateComponent {
    
    /**
     * Retrieves the name of this component.
     * 
     * @return This component's name
     */
    String getName();
    
    /**
     * A user-friendly name displayed for the component.
     * 
     * @return This component's user-friendly name
     */
    String getFriendlyName();
    
    /**
     * A user-friendly version displayed for the component.
     *
     * @return This component's user-friendly version
     * @since 0.6
     */
    String getFriendlyVersion();

    /**
     * Retrieves the currently installed version of this component.
     * 
     * @return This component's current version
     * @since 0.6.3m1
     */
    Version getVersion();
    
    /**
     * Installs the updated version of this component. After the update has
     * been installed, the component is responsible for deleting the specified
     * file.
     * 
     * @param path The full path to the downloaded data
     * @return True if a client restart is needed, false otherwise
     * @throws java.lang.Exception If any error occured
     */
    boolean doInstall(String path) throws Exception;

}
