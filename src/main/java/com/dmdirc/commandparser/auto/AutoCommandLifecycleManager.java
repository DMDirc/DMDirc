/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.commandparser.auto;

import com.dmdirc.util.system.SystemLifecycleComponent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Lifecycle manager for the auto commands system.
 */
@Singleton
public class AutoCommandLifecycleManager implements SystemLifecycleComponent {

    private final AutoCommandManager manager;
    private final AutoCommandStore store;

    @Inject
    public AutoCommandLifecycleManager(
            final AutoCommandManager manager,
            final AutoCommandStore store) {
        this.manager = manager;
        this.store = store;
    }

    @Override
    public void startUp() {
        store.readAutoCommands().forEach(manager::addAutoCommand);
        manager.start();
    }

    @Override
    public void shutDown() {
        manager.stop();
        save();
    }

    @Override
    public void save() {
        store.writeAutoCommands(manager.getAutoCommands());
    }

}
