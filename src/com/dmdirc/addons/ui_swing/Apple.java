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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.components.MenuBar;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.event.ActionEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.UIManager;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.util.ArrayList;

/**
 * Integrate DMDirc with OS X better.
 */
public final class Apple implements InvocationHandler, ActionListener {

    /**
     * Dummy interface for ApplicationEvent from the Apple UI on non-Apple platforms.
     * http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/com/apple/eawt/ApplicationEvent.html
     */
    public interface ApplicationEvent {

        /**
         * Provides the filename associated with a particular AppleEvent.
         *
         * @return The filename associated with a particular AppleEvent.
         */
        String getFilename();

        /**
         * Whether or not this event is handled.
         *
         * @return True if the event is handled, false otherwise
         */
        boolean isHandled();

        /**
         * Sets the handled state of this event.
         *
         * @param handled The new 'handled' state for this event.
         */
        void setHandled(boolean handled);

        /**
         * Retrieves the source of this event.
         *
         * @return This event's source
         */
        Object getSource();

        /**
         * Get a string representation of this object.
         *
         * @return A string representation of this object.
         */
        @Override
        String toString();
    }

    /** The singleton instance of Apple. */
    private static Apple me;
    /** The "Application" object used to do stuff on OS X. */
    private static Object application;
    /** The "NSApplication" object used to do cocoa stuff on OS X. */
    private static Object nsApplication;
    /** Are we listening? */
    private boolean isListener = false;
    /** The MenuBar for the application. */
    private MenuBar menuBar = null;
    /** Has the CLIENT_OPENED action been called? */
    private volatile boolean clientOpened = false;
    /** Store any addresses that are opened before CLIENT_OPENED. */
    private final ArrayList<URI> addresses = new ArrayList<URI>();

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
     * Create the Apple class.
     * <p>This attempts to:</p>
     *
     * <ul>
     *  <li>load the JNI library</li>
     *  <li>register the callback</li>
     *  <li>register a CLIENT_OPENED listener</li>
     * </ul>
     */
    private Apple() {
        if (isApple()) {
            try {
                System.loadLibrary("DMDirc-Apple");
                registerOpenURLCallback();
                ActionManager.addListener(this, CoreActionType.CLIENT_OPENED);
            } catch (UnsatisfiedLinkError ule) {
                Logger.userError(ErrorLevel.MEDIUM,
                        "Unable to load JNI library.", ule);
            }
        }
    }

    /**
     * Get the "Application" object
     *
     * @return Object that on OSX will be an "Application"
     */
    public static Object getApplication() {
        if (application == null && isApple()) {
            try {
                final Class<?> app = Class.forName("com.apple.eawt.Application");
                final Method method = app.getMethod("getApplication",
                        new Class[0]);
                application = method.invoke(null, new Object[0]);
            } catch (ClassNotFoundException ex) { // Probably not on OS X, do nothing.
            } catch (NoSuchMethodException ex) { // Probably not on OS X, do nothing.
            } catch (SecurityException ex) { // Probably not on OS X, do nothing.
            } catch (IllegalAccessException ex) { // Probably not on OS X, do nothing.
            } catch (IllegalArgumentException ex) { // Probably not on OS X, do nothing.
            } catch (InvocationTargetException ex) { // Probably not on OS X, do nothing.
            }
        }
        return application;
    }

    /**
     * Get the "NSApplication" object
     *
     * @return Object that on OSX will be an "NSApplication"
     */
    public static Object getNSApplication() {
        if (nsApplication == null && isApple()) {
            try {
                final Class<?> app = Class.forName(
                        "com.apple.cocoa.application.NSApplication");
                final Method method = app.getMethod("sharedApplication",
                        new Class[0]);
                nsApplication = method.invoke(null, new Object[0]);
            } catch (ClassNotFoundException ex) { // Probably not on OS X, do nothing.
            } catch (NoSuchMethodException ex) { // Probably not on OS X, do nothing.
            } catch (IllegalAccessException ex) { // Probably not on OS X, do nothing.
            } catch (InvocationTargetException ex) { // Probably not on OS X, do nothing.
            }
        }
        return nsApplication;
    }

    /**
     * Are we on OS X?
     *
     * @return true if we are running on OS X
     */
    public static boolean isApple() {
        return (System.getProperty("mrj.version") != null);
    }

    /**
     * Are we using the OS X look and feel?
     *
     * @return true if we are using the OS X look and feel
     */
    public static boolean isAppleUI() {
        return isApple() && UIManager.getLookAndFeel().getClass().getName().
                equals("apple.laf.AquaLookAndFeel");
    }

    /**
     * Set some OS X only UI settings.
     */
    public void setUISettings() {
        if (!isApple()) {
            return;
        }

        // Set some Apple OS X related stuff from http://tinyurl.com/6xwuld
        final String aaText = IdentityManager.getGlobalConfig().getOptionBool(
                "ui", "antialias") ? "on" : "off";

        System.setProperty("apple.awt.antialiasing", aaText);
        System.setProperty("apple.awt.textantialiasing", aaText);
        System.setProperty("apple.awt.showGrowBox", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                "DMDirc");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        System.setProperty("com.apple.mrj.application.live-resize", "true");
    }

    /**
     * Request user attention (Bounce the dock).
     *
     * @param isCritical If this is false, the dock icon only bounces once,
     *                   otherwise it will bounce until clicked on.
     */
    public void requestUserAttention(final boolean isCritical) {
        if (!isApple()) {
            return;
        }

        try {
            final Field type = isCritical ? getNSApplication().getClass().
                    getField("UserAttentionRequestCritical") : getNSApplication().
                    getClass().getField("Informational");
            final Method method = getNSApplication().getClass().getMethod(
                    "requestUserAttention", new Class[]{Integer.TYPE});
            method.invoke(getNSApplication(), new Object[]{type.get(null)});
        } catch (NoSuchFieldException ex) { // Probably not on OS X, do nothing.
        } catch (NoSuchMethodException ex) { // Probably not on OS X, do nothing.
        } catch (IllegalAccessException ex) { // Probably not on OS X, do nothing.
        } catch (InvocationTargetException ex) { // Probably not on OS X, do nothing.
        }
    }

    /**
     * Set this up as a listener for the Apple Events
     *
     * @return True if the listener was added, else false.
     */
    public boolean setListener() {
        if (!isApple() || isListener) {
            return false;
        }

        try {
            final Class listenerClass = Class.forName(
                    "com.apple.eawt.ApplicationListener");
            final Object listener = Proxy.newProxyInstance(getClass().
                    getClassLoader(), new Class[]{listenerClass}, this);

            Method method = getApplication().getClass().getMethod(
                    "addApplicationListener", new Class[]{listenerClass});
            method.invoke(getApplication(), listener);

            isListener = true;

            method = getApplication().getClass().getMethod(
                    "setEnabledPreferencesMenu", new Class[]{Boolean.TYPE});
            method.invoke(getApplication(), new Object[]{Boolean.TRUE});

            method =
                    getApplication().getClass().getMethod("setEnabledAboutMenu",
                    new Class[]{Boolean.TYPE});
            method.invoke(getApplication(), new Object[]{Boolean.TRUE});
            return true;
        } catch (ClassNotFoundException ex) { // Probably not on OS X, do nothing.
        } catch (NoSuchMethodException ex) { // Probably not on OS X, do nothing.
        } catch (IllegalAccessException ex) { // Probably not on OS X, do nothing.
        } catch (InvocationTargetException ex) { // Probably not on OS X, do nothing.
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws Throwable Throws stuff on errors
     */
    @Override
    public Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Throwable {
        if (!isApple()) {
            return null;
        }

        try {
            final ApplicationEvent event = (ApplicationEvent) Proxy.
                    newProxyInstance(getClass().getClassLoader(), new Class[]{
                        ApplicationEvent.class}, new InvocationHandler() {

                /** {@inheritDoc} */
                @Override
                public Object invoke(final Object p, final Method m,
                        final Object[] a) throws Throwable {
                    return args[0].getClass().getMethod(m.getName(), m.
                            getParameterTypes()).invoke(args[0], a);
                }
            });
            Method thisMethod = this.getClass().getMethod(method.getName(),
                    new Class[]{ApplicationEvent.class});
            return thisMethod.invoke(this, event);
        } catch (NoSuchMethodException e) {
            if (method.getName().equals("equals") && args.length == 1) {
                return Boolean.valueOf(proxy == args[0]);
            }
        }

        return null;
    }

    /**
     * Set the MenuBar.
     * This will unset all menu mnemonics aswell if on the OSX ui.
     *
     * @param menuBar MenuBar to use to send events to,
     */
    public void setMenuBar(final MenuBar menuBar) {
        this.menuBar = menuBar;
        if (isAppleUI()) {
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                final JMenu menu = menuBar.getMenu(i);
                if (menu != null) {
                    menu.setMnemonic(0);
                    for (int j = 0; j < menu.getItemCount(); j++) {
                        final JMenuItem menuItem = menu.getItem(j);
                        if (menuItem != null) {
                            menuItem.setMnemonic(0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle an event using the menuBar
     *
     * @param name The name of the event according to the menubar
     * @param event The ApplicationEvent we are handingle
     */
    public void handleMenuBarEvent(final String name,
            final ApplicationEvent event) {
        if (!isApple() || menuBar == null) {
            return;
        }
        final ActionEvent actionEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, name);

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
    }

    /**
     * This is called when the application is asked to open a file
     *
     * @param event an ApplicationEvent object
     */
    public void handleOpenFile(final ApplicationEvent event) {
    }

    /**
     * This is called when asked to print
     *
     * @param event an ApplicationEvent object
     */
    public void handlePrintFile(final ApplicationEvent event) {
    }

    /**
     * This is called when the application is reopened
     *
     * @param event an ApplicationEvent object
     */
    public void handleReopenApplication(final ApplicationEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.CLIENT_OPENED) {
            synchronized (addresses) {
                clientOpened = true;
                for (URI addr : addresses) {
                    ServerManager.getServerManager().connectToAddress(addr);
                }
                addresses.clear();
            }
        }
    }

    /**
     * Callback from JNI library.
     * If called before the client has finished opening, the URL will be added to
     * a list that will be connected to once the CLIENT_OPENED action is called.
     * Otherwise we connect right away.
     *
     * @param url The irc url to connect to.
     */
    public void handleOpenURL(final String url) {
        if (isApple()) {
            try {
                synchronized (addresses) {
                    final URI addr = new URI(url);
                    if (!clientOpened) {
                        addresses.add(addr);
                    } else {
                        // When the JNI callback is called there is no
                        // ContextClassLoader set, which causes an NPE in
                        // IconManager if no servers have been connected to yet.
                        if (Thread.currentThread().getContextClassLoader() ==
                                null) {
                            Thread.currentThread().setContextClassLoader(ClassLoader.
                                    getSystemClassLoader());
                        }
                        ServerManager.getServerManager().connectToAddress(addr);
                    }
                }
            } catch (URISyntaxException iae) {
            }
        }
    }

    /**
     * Register the getURL Callback.
     *
     * @return 0 on success, 1 on failure.
     */
    private synchronized final native int registerOpenURLCallback();
}
