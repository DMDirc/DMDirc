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

package com.dmdirc.harness.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.BeforeClass;
import org.junit.internal.runners.TestClassMethodsRunner;
import org.junit.internal.runners.TestMethodRunner;
import org.junit.runner.notification.RunNotifier;

public class UIClassTestRunner extends TestClassMethodsRunner {

    public UIClassTestRunner(Class<?> arg0) {
        super(arg0);

        for (Method method : arg0.getMethods()) {
            if (method.getAnnotation(BeforeClass.class) != null) {
                try {
                    method.invoke(null);
                } catch (InvocationTargetException e) {
                    return;
                } catch (Throwable e) {
                    return;
                }
            }
        }
    }

    protected TestMethodRunner createMethodRunner(Object test, Method method, RunNotifier notifier) {
        return new UIMethodTestRunner(test, method, notifier, methodDescription(method));
    }

}
