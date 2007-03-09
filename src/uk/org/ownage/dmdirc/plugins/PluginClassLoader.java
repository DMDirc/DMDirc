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

package uk.org.ownage.dmdirc.plugins;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import uk.org.ownage.dmdirc.logger.Logger;


/**
 * A custom ClassLoader to load and unload plugins.
 */
public final class PluginClassLoader extends ClassLoader {
    
    /**
     * Base directory for the class loader.
     */
    private String baseDir;
    
    /**
     * Constructs new PluginClassLoader.
     *
     * @param directory plugin loader base directory
     */
    public PluginClassLoader(final String directory) {
        this.baseDir = directory;
    }
    
    /**
     * Loads a plugin from disk.
     *
     * @param name plugin class name to load
     * @return plugin class
     * @throws ClassNotFoundException if the class to be loaded could not be
     * found in the base directory for this classloader
     */
    public Class loadClass(final String name) throws ClassNotFoundException {
        Class loadedClass = findLoadedClass(name);
        String fileName;
        if (loadedClass == null) {
            /*
            try {
                loadedClass = findSystemClass(name);
            } catch (ClassNotFoundException e) {
                //Do nothing
            }
             */
            
            if (loadedClass == null) {
                fileName = baseDir + File.separator 
                        + name.replace('.', File.separatorChar) + ".class";
                byte[] data = null;
                
                Logger.debug("Trying to load: " + fileName);
                
                try {
                    data = loadClassData(fileName);
                } catch (IOException e) {
                    Logger.debug("" + e);
                    throw new ClassNotFoundException(e.getMessage());
                }
                
                loadedClass = defineClass(name, data, 0, data.length);
                
                if (loadedClass == null) {
                    Logger.debug("loadedClass == null");
                    throw new ClassNotFoundException("Could not load " + name);
                } else {
                    resolveClass(loadedClass);
                }
            } else {
                Logger.debug("found class");
            }
        } else {
            Logger.debug("class already loaded");
        }
        
        return loadedClass;
    }
    
    /**
     * Loads binary class data from disk.
     *
     * @param fileName file name
     * @return bytecodes
     * @throws IOException if unable to read the specified file
     */
    private byte[] loadClassData(final String fileName) throws IOException {
        final File file = new File(fileName);
        final byte[] buffer = new byte[(int) file.length()];
        
        final FileInputStream in = new FileInputStream(file);
        final DataInputStream dataIn = new DataInputStream(in);
        
        dataIn.readFully(buffer);
        dataIn.close();
        
        return buffer;
    }
}
