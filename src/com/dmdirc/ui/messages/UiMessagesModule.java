/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.ui.messages;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;

import java.nio.file.Path;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType.BASE;

/**
 * Dagger module for message related objects.
 */
@Module(library = true, complete = false)
public class UiMessagesModule {

    @Provides
    @Singleton
    public EventTemplateProvider getTemplateProvider(@Directory(BASE) final Path directory,
            final DMDircMBassador eventBus) {
        final YamlEventTemplateProvider provider =
                new YamlEventTemplateProvider(directory.resolve("format.yml"), eventBus);
        provider.load();
        return provider;
    }

}