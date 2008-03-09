/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.interfaces.UIController;

import java.awt.event.ActionEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

/**
 * Integrate DMDirc with OS X better.
 */
public final class Apple implements InvocationHandler {
	/** ApplicationEvent */
	private interface ApplicationEvent {
		String getFilename();
		boolean isHandled();
		void setHandled(boolean handled);
		Object getSource();
		String toString();
	}
	
	/** The singleton instance of Apple */
	private static Apple me;
	
	/** The "Application" object used to do stuff on OS X */
	private static Object application;
	
	/** The "NSApplication" object used to do cocoa stuff on OS X */
	private static Object nsApplication;
	
	/** Are we listening? */
	private boolean isListener = false;
	
	/** The MenuBar for the application */
	private MenuBar menuBar = null;

	/**
	 * Get the "Apple" instance.
	 *
	 * @return Apple instance.
	 */
	public static Apple getApple() {
		if (me == null) {
			me = new Apple();
		}
		return me;
	}

	/**
	 * Get the "Application" object
	 *
	 * @return Object that on OSX will be an "Application"
	 */
	@SuppressWarnings("unchecked")
	public static Object getApplication() {
		if (application == null && isApple()) {
			try {
				final Class app = Class.forName("com.apple.eawt.Application");
				final Method method = app.getMethod("getApplication", new Class[0]);
				application = method.invoke(null, new Object[0]);
			} catch (Exception e) { /* Do nothing */ }
		}
		return application;
	}
	
	/**
	 * Get the "NSApplication" object
	 *
	 * @return Object that on OSX will be an "NSApplication"
	 */
	@SuppressWarnings("unchecked")
	public static Object getNSApplication() {
		if (nsApplication == null && isApple()) {
			try {
				final Class app = Class.forName("com.apple.cocoa.application.NSApplication");
				final Method method = app.getMethod("sharedApplication", new Class[0]);
				nsApplication = method.invoke(null, new Object[0]);
			} catch (Exception e) { /* Do nothing */ }
		}
		return nsApplication;
	}
	
	/**
	 * Are we on OS X?
	 *
	 * @return true if:
	 *           - The UI Controller is SwingController,
	 *           - We are running on OS X
	 */
	public static boolean isApple() {
		return (Main.getUI() instanceof SwingController && System.getProperty("os.name").startsWith("Mac OS"));
	}

	/**
	 * Set some OS X only UI settings.
	 */
	public void setUISettings() {
		if (!isApple()) { return; }
		
		// Set some Apple OS X related stuff from
		// http://developer.apple.com/documentation/Java/Conceptual/JavaPropVMInfoRef/Articles/JavaSystemProperties.html
		final String aaText = System.getProperty("swing.aatext").equalsIgnoreCase("true") ? "on" : "off";
		if (IdentityManager.getGlobalConfig().getOptionBool("ui", "antialias")) {
			System.setProperty("apple.awt.antialiasing", "on");
		} else {
			System.setProperty("apple.awt.antialiasing", "off");
		}
		System.setProperty("apple.awt.textantialiasing", aaText);
		System.setProperty("apple.awt.showGrowBox", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
		System.setProperty("com.apple.mrj.application.live-resize", "true");
//		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DMDirc: " + Main.VERSION);
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DMDirc");
	}
	
	/**
	 * Request user attention (Bounce the dock).
	 *
	 * @param isCritical If this is false, the dock icon only bounces once,
	 *                   otherwise it will bounce until clicked on.
	 */
	@SuppressWarnings("unchecked")
	public void requestUserAttention(final boolean isCritical) {
		if (!isApple()) { return; }
		
		try {
			final Field type = (isCritical) ? getNSApplication().getClass().getField("UserAttentionRequestCritical") : getNSApplication().getClass().getField("Informational");
			final Method method = getNSApplication().getClass().getMethod("requestUserAttention", new Class[]{Integer.TYPE});
			method.invoke(getNSApplication(), new Object[]{type.get(null)});
		} catch (Exception e) { }
	}
	
	/**
	 * Set this up as a listener for the Apple Events
	 *
	 * @return True if the listener was added, else false.
	 */
	public boolean setListener() {
		if (!isApple() || isListener) { return false; }
		
		try {
			final Class listenerClass = Class.forName("com.apple.eawt.ApplicationListener");
			final Object listener = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{listenerClass}, this);
			
			Method method = getApplication().getClass().getMethod("addApplicationListener", new Class[]{listenerClass});
			method.invoke(getApplication(), listener);
			
			isListener = true;
			
			method = getApplication().getClass().getMethod("setEnabledPreferencesMenu", new Class[]{Boolean.TYPE});
			method.invoke(getApplication(), new Object[]{Boolean.TRUE});
			
			method = getApplication().getClass().getMethod("setEnabledAboutMenu", new Class[]{Boolean.TYPE});
			method.invoke(getApplication(), new Object[]{Boolean.TRUE});
			
			return true;
		} catch (Exception e) { /* Do Nothing */ }
		return false;
	}
	
	/** {@inheritDoc} */
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (!isApple()) { return null; }
		
		try {
			final ApplicationEvent event = (ApplicationEvent) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ApplicationEvent.class}, new InvocationHandler() {
				public Object invoke(Object p, Method m, Object[] a) throws Throwable {
					return args[0].getClass().getMethod(m.getName(), m.getParameterTypes()).invoke(args[0], a);
				}
			});
			Method thisMethod = this.getClass().getMethod(method.getName(), new Class[]{ApplicationEvent.class});
			return thisMethod.invoke(this, event);
		} catch (NoSuchMethodException e) {
			if (method.getName().equals("equals") && args.length == 1) {
				return Boolean.valueOf(proxy == args[0]);
			}
		}
		return null;
	}
	
	/**
	 * Set the MenuBar
	 *
	 * @param menuBar MenuBar to use to send events to,
	 */
	public void setMenuBar(final MenuBar menuBar) {
		this.menuBar = menuBar;
	}
	
	/**
	 * Handle an event using the menuBar
	 *
	 * @param name The name of the event according to the menubar
	 * @param event The ApplicationEvent we are handingle
	 */
	public void handleMenuBarEvent(final String name, final ApplicationEvent event) {
		if (!isApple() || menuBar == null) { return; }
		final ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name);
		
		menuBar.actionPerformed(actionEvent);
		event.setHandled(true);
	}
	
	/**
	 * This is called when Quit is selected from the Application menu
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handleQuit(final ApplicationEvent event) {
		handleMenuBarEvent("Exit", event);
	}
	
	/**
	 * This is called when About is selected from the Application menu
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handleAbout(final ApplicationEvent event) {
		handleMenuBarEvent("About", event);
	}
	
	/**
	 * This is called when Preferences is selected from the Application menu
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handlePreferences(final ApplicationEvent event) {
		handleMenuBarEvent("Preferences", event);
	}
	
	/**
	 * This is called when the Application is opened
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handleOpenApplication(final ApplicationEvent event) {
		javax.swing.JOptionPane.showMessageDialog(null, "Open '"+event.getFilename()+"' ?", "OA", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * This is called when the application is asked to open a file
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handleOpenFile(final ApplicationEvent event) {
		javax.swing.JOptionPane.showMessageDialog(null, "Open '"+event.getFilename()+"' ?", "OF", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * This is called when asked to print
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handlePrintFile(final ApplicationEvent event) {
		javax.swing.JOptionPane.showMessageDialog(null, "Open '"+event.getFilename()+"' ?", "PF", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * This is called when the application is reopened
	 *
	 * @param event an ApplicationEvent object
	 */
	public void handleReopenApplication(final ApplicationEvent event) {
		javax.swing.JOptionPane.showMessageDialog(null, "Open '"+event.getFilename()+"' ?", "ROA", javax.swing.JOptionPane.ERROR_MESSAGE);
	}
}
