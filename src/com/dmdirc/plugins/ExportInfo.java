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

package com.dmdirc.plugins;

public class ExportInfo {

    /** Name of class the export is in. */
    final String className;

    /** Name of method the export is in. */
    final String methodName;

    /** The PluginInfo that defined this export. */
    final PluginInfo pluginInfo;

    /**
     * Create a new ExportInfo.
     *
     * @param methodName Name of method the export is in.
     * @param className Name of class the export is in.
     * @param pluginInfo The PluginInfo that defined this export.
     */
    public ExportInfo(final String methodName, final String className, final PluginInfo pluginInfo) {
        this.className = className;
        this.methodName = methodName;
        this.pluginInfo = pluginInfo;
    }

    /**
     * Get the ExportedService for this Export.
     *
     * @return ExportedService object for this export.
     */
    public ExportedService getExportedService() {
        try {
            final Class<?> c = pluginInfo.getPluginClassLoader().loadClass(className, false);
            final Plugin p = className.equals(pluginInfo.getMainClass()) ? pluginInfo.getPluginObject() : null;
            return new ExportedService(c, methodName, p);
        } catch (ClassNotFoundException cnfe) {
            return new ExportedService(null, null);
        }
    }

}
