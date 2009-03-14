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

package com.dmdirc.parser.irc.callbacks;

import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.ParserError;
import com.dmdirc.parser.irc.callbacks.interfaces.ICallbackInterface;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CallbackObject.
 * Superclass for all callback types.
 *
 * @author            Shane Mc Cormack
 */
public class CallbackObject {

    /** The type of callback that this object is operating with. */
    protected final Class<? extends ICallbackInterface> type;

	/** Arraylist for storing callback information related to the callback. */
	protected final List<ICallbackInterface> callbackInfo = new ArrayList<ICallbackInterface>();
	
	/** Reference to the IRCParser that owns this callback. */
	protected IRCParser myParser;
	/** Reference to the CallbackManager in charge of this callback. */
	protected CallbackManager myManager;
	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
     * @param type The type of callback to use
     * @since 0.6.3
	 */
	protected CallbackObject(final IRCParser parser, final CallbackManager manager,
            final Class<? extends ICallbackInterface> type) {
		this.myParser = parser;
		this.myManager = manager;
        this.type = type;
	}
	
	/**
	 * Add a callback pointer to the appropriate ArrayList.
	 *
	 * @param eMethod OBject to callback to.
	 */
	protected final void addCallback(final ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { return; }
		}
		callbackInfo.add(eMethod);
	}
	
	/**
	 * Delete a callback pointer from the appropriate ArrayList.
	 *
	 * @param eMethod Object that was being called back to.
	 */
	protected final void delCallback(final ICallbackInterface eMethod) {
		for (int i = 0; i < callbackInfo.size(); i++) {
			if (eMethod.equals(callbackInfo.get(i))) { callbackInfo.remove(i); break; }
		}
	}
	
	/**
	 * Call the OnErrorInfo callback.
	 *
	 * @param errorInfo ParserError object to pass as error.
	 * @return true if error call succeeded, false otherwise
	 */
	protected final boolean callErrorInfo(final ParserError errorInfo) {
		return myManager.getCallbackType("OnErrorInfo").call(errorInfo);
	}
	
	/**
	 * Add a new callback.
	 *
	 * @param eMethod Object to callback to.
	 */
	public void add(final ICallbackInterface eMethod) { addCallback(eMethod); }
	
	/**
	 * Remove a callback.
	 *
	 * @param eMethod Object to remove callback to.
	 */
	public void del(final ICallbackInterface eMethod) { delCallback(eMethod); }
	
	/**
	 * Get the name for this callback.
	 *
	 * @return Name of callback
	 */
	public String getName() {
		return "On" + type.getSimpleName().substring(1); // Trim the 'I'
	}
	
	/**
	 * Get the name for this callback in lowercase.
	 *
	 * @return Name of callback, in lowercase
	 */
	public final String getLowerName() { return this.getName().toLowerCase(); }

    /**
     * Actually calls this callback. The specified arguments must match those
     * specified in the callback's interface, or an error will be raised.
     *
     * @param args The arguments to pass to the callback implementation
     * @return True if a method was called, false otherwise
     */
    public boolean call(final Object ... args) {
		boolean bResult = false;

        final Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = myParser;

        if (myParser.getCreateFake()) {
            createFakeArgs(newArgs);
        }

		for (ICallbackInterface iface : new ArrayList<ICallbackInterface>(callbackInfo)) {
			try {
                type.getMethods()[0].invoke(iface, newArgs);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.ERROR_ERROR,
                        "Exception in callback ("+e.getMessage()+")", myParser.getLastLine());
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
    }

    /**
     * Replaces all null entries in the specified array with fake values,
     * if the corresponding parameter of this callback's type is marked with
     * the {@link FakableArgument} annotation. The fake classes are constructed
     * by using parameters designated {@link FakableSource}.
     *
     * @param args The arguments to be faked
     */
    protected void createFakeArgs(final Object[] args) {
        int i = 0;

        for (Annotation[] anns : type.getMethods()[0].getParameterAnnotations()) {
            for (Annotation ann : anns) {
                if (ann.annotationType().equals(FakableArgument.class)
                        && args[i] == null) {
                    args[i] = getFakeArg(args, type.getMethods()[0].getParameterTypes()[i]);
                }
            }

            i++;
        }
    }

    /**
     * Tries to create fake argument of the specified target class, by using
     * {@link FakableSource} denoted parameters from the specified arg array.
     *
     * If an argument is missing, the method attempts to create a fake instance
     * by recursing into this method again. Note that this could cause an
     * infinite recursion in cases of cyclic dependencies. If recursion fails,
     * the constructor is skipped.
     *
     * If the created object has a <code>setFake(boolean)</code> method, it
     * is automatically invoked with an argument of <code>true</code>.
     *
     * @param args The arguments array to use for sources
     * @param target The class that should be constructed
     * @return An instance of the target class, or null on failure
     */
    protected Object getFakeArg(final Object[] args, final Class<?> target) {
        final Map<Class, Object> sources = new HashMap<Class, Object>();
        int i = 0;

        for (Annotation[] anns : type.getMethods()[0].getParameterAnnotations()) {
            for (Annotation ann : anns) {
                if (ann.annotationType().equals(FakableSource.class)) {
                    sources.put(type.getMethods()[0].getParameterTypes()[i], args[i]);
                }
            }

            i++;
        }

        for (Constructor<?> ctor : target.getConstructors()) {
            Object[] params = new Object[ctor.getParameterTypes().length];

            i = 0;
            Object param;
            boolean failed = false;

            for (Class<?> needed : ctor.getParameterTypes()) {
                if (sources.containsKey(needed)) {
                    params[i] = sources.get(needed);
                } else if ((param = getFakeArg(args, needed)) != null) {
                    params[i] = param;
                } else {
                    failed = true;
                }

                i++;
            }

            if (!failed) {
                try {
                    final Object instance = ctor.newInstance(params);

                    for (Method method : target.getMethods()) {
                        if (method.getName().equals("setFake")
                                && method.getParameterTypes().length == 1
                                && method.getParameterTypes()[0].equals(Boolean.TYPE)) {

                            method.invoke(instance, true);
                        }
                    }

                    return instance;
                } catch (InstantiationException ex) {
                    // Do nothing
                } catch (IllegalAccessException ex) {
                    // Do nothing
                } catch (IllegalArgumentException ex) {
                    // Do nothing
                } catch (InvocationTargetException ex) {
                    // Do nothing
                }
            }
        }

        return null;
    }

}
