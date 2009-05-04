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

/**
 * DMDirc's automatic updater. The {@link UpdateChecker} periodically sends
 * a list of all enabled {@link UpdateComponent}s to the DMDirc website
 * (using the domain updates.dmdirc.com). The website then replies with a
 * message saying whether each component is out of date, up to date, or not
 * known.
 *
 * For each update that is available, the {@link UpdateChecker} creates
 * a new {@link Update} instance, which is responsible for parsing the full
 * response from the DMDirc website.
 *
 * The status of the {@link UpdateChecker} can be monitored by registering a
 * {@link UpdateCheckerListener} with it. This enables, for example, UI
 * components to notify the user when updates are available. Such components
 * can then remove certain updates (by calling
 * {@link UpdateChecker#removeUpdate(Update)}), and have the
 * {@link UpdateChecker} apply the updates by calling
 * {@link UpdateChecker#applyUpdates()}.
 *
 * Internally, when applying updates, the {@link UpdateChecker} calls the
 * {@link Update#doUpdate()} method on each relevant {@link Update}, which
 * downloads the required files to a temporary location, and then has the
 * relevant {@link UpdateComponent} perform the installation. During this time,
 * the {@link Update} can be monitored by registering an {@link UpdateListener}
 * with it.
 */
package com.dmdirc.updater;
