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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Object to allow interaction with Exported methods
 */
public class ExportedService {
	/** Method we will be executing today! */
	final Method myMethod;
	
	/** Object we will be executing this method on. */
	final Object myObject;
	
	/**
	 * Create a new ExportedService object.
	 *
	 * @param myClass class method is in.
	 * @param methodName Name of method
	 */
	public ExportedService(final Class myClass, final String methodName) {
		this(myClass, methodName, null);
	}
	
	/**
	 * Create a new ExportedService object.
	 *
	 * @param myClass class method is in.
	 * @param methodName Name of method
	 * @param object Object to execute this method on.
	 */
	public ExportedService(final Class<?> myClass, final String methodName, final Object object) {
		myObject = object;
		if (myClass == null) {
			myMethod = null;
		} else {
			final Method[] methods = myClass.getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().equals(methodName)) {
					myMethod = m;
					return;
				}
			}
			myMethod = null;
		}
	}
	
	/**
	 * Execute the method.
	 *
	 * @param args Arguments to pass to method
	 * @return result of executing the method
	 */
	public Object execute(final Object... args) {
		if (myMethod == null) { return null; }
		
		try {
			return myMethod.invoke(myObject, args);
		} catch (IllegalAccessException iae) {
			return null;
		} catch (IllegalArgumentException iae) {
			return null;
		} catch (InvocationTargetException ite) {
			return null;
		}
	}
}
