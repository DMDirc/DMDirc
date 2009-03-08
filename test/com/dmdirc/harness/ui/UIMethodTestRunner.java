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

import org.fest.swing.exception.ActionFailedException;
import org.junit.internal.runners.TestMethodRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

public class UIMethodTestRunner extends TestMethodRunner {

    private final Object test;

    public UIMethodTestRunner(Object test, Method method,
                                    RunNotifier notifier,
                                    Description description) {
        super(test, method, notifier, description);
        this.test = test;
    }

    @Override
    protected void runUnprotected() {
        try {
            ((UITestIface) test).setUp();

            boolean retry;
            int retries = 5;

            do {
                retry = false;

                try {
                    executeMethodBody();
                } catch (ActionFailedException e) {
                    if (--retries > 0) {
                        retry = true;
                        ((UITestIface) test).tearDown();
                        ((UITestIface) test).setUp();
                    } else {
                        addFailure(e);
                    }
                } catch (InvocationTargetException e) {
                    addFailure(e.getCause());
                } catch (Throwable e) {
                    addFailure(e);
                }
            } while (retry);

            ((UITestIface) test).tearDown();
        } catch (Throwable ex) {
            addFailure(ex);
        }
    }

    @Override
    public void runProtected() {
        runUnprotected();
    }

}
