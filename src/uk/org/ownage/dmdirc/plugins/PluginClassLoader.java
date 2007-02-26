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


/**
 * A custom ClassLoader to load and unload plugins.
 */
class PluginClassLoader extends ClassLoader {
    private String baseDir = null;
    
    /**
     * Constructs new PluginClassLoader
     *
     * @param baseDir plugin base dir
     */
    public PluginClassLoader(String baseDir) {
	this.baseDir = baseDir;
    }
    
    /**
     * Loads a plugin from disk.
     *
     * @param name plugin class name to load
     * @return plugin class
     */
    public Class loadClass(String name) throws ClassNotFoundException {
	Class loadedClass = findLoadedClass(name);
	if ( loadedClass == null ) {
	    try {
		loadedClass = findSystemClass(name);
	    } catch ( Exception e ) {
		// do nothing
	    }
	    
	    if ( loadedClass == null ) {
		String fileName = baseDir + File.separator +
			name.replace('.', File.separatorChar) + ".class";
		byte[] data = null;
		
		System.out.println("Trying to load: " + fileName);
		
		try {
		    data = loadClassData(fileName);
		} catch( IOException e ) {
		    System.out.println(e);
		    throw new ClassNotFoundException(e.getMessage());
		}
		
		loadedClass = defineClass(name,data, 0, data.length);
		
		if ( loadedClass == null ) {
		    System.out.println("loadedClass == null");
		    throw new ClassNotFoundException("Could not load " + name);
		} else {
		    resolveClass(loadedClass);
		}
	    } else {
		//Found loaded system class
	    }
	} else {
	    //Found loaded class
	}
	
	return loadedClass;
    }
    
    /**
     * Loads binary class data from disk.
     *
     * @param fileName file name
     * @return bytecodes
     */
    private byte[] loadClassData(String fileName) throws IOException {
	File file = new File(fileName);
	byte buffer[] = new byte[(int)file.length()];
	
	FileInputStream in = new FileInputStream(file);
	DataInputStream dataIn = new DataInputStream(in);
	
	dataIn.readFully(buffer);
	dataIn.close();
	
	return buffer;
    }
}












