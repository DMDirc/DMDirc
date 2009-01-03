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

package com.dmdirc.parser.irc;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Logging using log4j if available.
 */
public class Logging {
	/** Available Log Levels */
	public enum LogLevel {
		TRACE("trace", "isTraceEnabled"),
		DEBUG("debug", "isDebugEnabled"),
		INFO("info", "isInfoEnabled"),
		WARN("warn", "isWarnEnabled"),
		ERROR("error", "isErrorEnabled"),
		FATAL("fatal", "isFatalEnabled");
		
		/** Method name */
		private final String methodName;
		
		/** Check Method name */
		private final String checkMethodName;
		
		/**
		 * Create a new LogLevel
		 *
		 * @param methodName Name of method in log4j to log to
		 * @param checkMethodName Name of method in log4j to sue to check logging
		 */
		private LogLevel(final String methodName, final String checkMethodName) {
			this.methodName = methodName;
			this.checkMethodName = checkMethodName;
		}
		
		/**
		 * Get the Name of method in log4j to log to
		 *
		 * @return Name of method in log4j to log to
		 */
		public String getMethodName() { return methodName; }
		
		/**
		 * Get the Name of the check method in log4j
		 *
		 * @return Name of check method in log4j
		 */
		public String getCheckMethodName() { return checkMethodName; }
	};
	
	/** Singleton Instance of Logging */
	private static Logging me;
	
	/** Is log4j available */
	private final boolean isAvailable;
	
	/** "Log" object if available */
	private Object log = null;
	
	/** Get an instance of Logging */
	public static Logging getLogging() {
		if (me == null) { me = new Logging(); }
		return me;
	}
	
	/** Create a new Logging */
	@SuppressWarnings("unchecked")
	private Logging() {
		boolean classExists = false;
		try {
			Class factory = null;
			// Check for classes
			Class.forName("org.apache.commons.logging.Log");
			factory = Class.forName("org.apache.commons.logging.LogFactory");
		
			classExists = (factory != null);
			if (classExists) {
				final Method getLog = factory.getMethod("getLog", new Class[]{Class.class});
				log = getLog.invoke(null, this.getClass());
			}
		} catch (ClassNotFoundException cnfe) {
		} catch (NoSuchMethodException nsme) {
		} catch (IllegalAccessException iae) {
		} catch (InvocationTargetException ite) {
		}
		
		isAvailable = (classExists && log != null);
	}
	
	/**
	 * Check is a log level is available.
	 *
	 * @param level Level to check
	 */
	public boolean levelEnabled(final LogLevel level) {
		if (isAvailable) {
			try {
				final Method check = log.getClass().getMethod(level.getCheckMethodName(), new Class[0]);
				return (Boolean)check.invoke(log, new Object[0]);
			} catch (NoSuchMethodException nsme) {
			} catch (IllegalAccessException iae) {
			} catch (InvocationTargetException ite) {
			}
		}
		
		return false;
	}
	
	/**
	 * Log a message if log4j is available.
	 *
	 * @param level Level to log at
	 * @param message Message to log
	 */
	public void log(final LogLevel level, final String message) {
		log(level, message, null);
	}
	
	/**
	 * Log a message if log4j is available.
	 *
	 * @param level Level to log at
	 * @param message Message to log
	 * @param throwable Throwable to log alongside message
	 */
	public void log(final LogLevel level, final String message, final Throwable throwable) {
		if (!isAvailable) { return; }
		try {
			if (throwable == null) {
				final Method method = log.getClass().getMethod(level.getMethodName(), new Class[]{String.class});
				method.invoke(log, new Object[]{message});
			} else {
				final Method method = log.getClass().getMethod(level.getMethodName(), new Class[]{String.class, Throwable.class});
				method.invoke(log, new Object[]{message, throwable});
			}
		} catch (NoSuchMethodException nsme) {
		} catch (IllegalAccessException iae) {
		} catch (InvocationTargetException ite) {
		}
	}
}
