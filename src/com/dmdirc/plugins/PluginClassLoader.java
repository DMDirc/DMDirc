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
 *
 * SVN: $Id$
 */
package com.dmdirc.plugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import java.util.Hashtable;

public class PluginClassLoader extends ClassLoader {
	/** Directory where plugins are stored. */
	String myDir;
	
	/** Name of the package I am loading. */
	String myPackage = "";
	
	/**
	 * Create a new PluginClassLoader.
	 *
	 * @param directory Directory where plugins are stored.
	 */
	public PluginClassLoader(String directory) {
		myDir = directory;
	}
	
	/**
	 * Load the plugin with the given className
	 *
	 * @param name Class Name of plugin
	 * @return plugin class
	 * @throws ClassNotFoundException if the class to be loaded could not be found.
	 */
	public Class< ? > loadClass(final String name) throws ClassNotFoundException {
		Class< ? > loadedClass = null;

		// Check to make sure we only load things in our own package!
		try {
			if (myPackage.length() == 0) {
				int i = name.lastIndexOf('.');
				if (i != -1) { myPackage = name.substring(0, i); }
				else { return getParent().loadClass(name); }
			}
			if (!name.startsWith(myPackage)) { return getParent().loadClass(name); }
		} catch (NoClassDefFoundError e) {
			System.out.println("Error loading '"+name+"' (wanted by '"+myDir+"') -> "+e.getMessage());
			throw new ClassNotFoundException("Error loading '"+name+"' (wanted by '"+myDir+"') -> "+e.getMessage(), e);
		}
		
		// We are ment to be loading this one!
		final String fileName = myDir + File.separator + name.replace(myDir+".", "").replace('.', File.separatorChar) + ".class";
		byte[] data = null;

		try {
			data = loadClassData(fileName);
		} catch (IOException e) {
			throw new ClassNotFoundException(e.getMessage());
		}
		
		try {
			loadedClass = defineClass(name, data, 0, data.length);
		} catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		
		if (loadedClass == null) {
			throw new ClassNotFoundException("Could not load " + name);
		} else {
			resolveClass(loadedClass);
		}
		
		return loadedClass;
	}
	
	/**
	 * Load the class from the .class file
	 *
	 * @param filename Filename to load from
	 * @throws IOException when the file doesn't exist or can't be read
 	 */
	public byte[] loadClassData(String filename) throws IOException {
		final File file = new File(filename);
		final byte[] fileBuffer = new byte[(int)file.length()];
		final DataInputStream fileInput = new DataInputStream(new FileInputStream(file));
		
		fileInput.readFully(fileBuffer);
		fileInput.close();
		
		return fileBuffer;
	}
}
