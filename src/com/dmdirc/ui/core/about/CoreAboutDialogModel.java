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

package com.dmdirc.ui.core.about;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.commandline.CommandLineOptionsModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.DateUtils;

import com.google.common.collect.Lists;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class CoreAboutDialogModel implements AboutDialogModel {

    private final AggregateConfigProvider globalConfig;
    private final Path baseDirectory;
    private final ClientInfo clientInfo;
    private String about;
    private List<Developer> mainDevelopers;
    private List<Developer> otherDevelopers;
    private List<InfoItem> info;
    private List<LicensedComponent> licences;

    @Inject
    public CoreAboutDialogModel(@GlobalConfig final AggregateConfigProvider globalConfig,
            @Directory(CommandLineOptionsModule.DirectoryType.BASE) final Path baseDirectory,
            final ClientInfo clientInfo) {
        this.globalConfig = globalConfig;
        this.baseDirectory = baseDirectory;
        this.clientInfo = clientInfo;
        about = "";
        mainDevelopers = new ArrayList<>();
        otherDevelopers = new ArrayList<>();
        info = new ArrayList<>();
        licences = new ArrayList<>();
    }

    public void load() {
        about = "<html><center>"
                + "<h1 style=\"margin-bottom: 0px;\">DMDirc</h1>"
                + "<span style=\"font-style: italic;\">The intelligent IRC client.</span>"
                + "<p>Easy to use, cross-platform IRC client.</p>"
                + "<p><a href=\"http://www.dmdirc.com\">www.dmdirc.com</a></p>"
                + "</center></html>";
        mainDevelopers = createMainDevelopers();
        otherDevelopers = createOtherDevelopers();
        info = createInfoItems();
        licences = new ArrayList<>();
    }

    private List<Developer> createMainDevelopers() {
        return Lists.newArrayList(
                Developer.create("Chris 'MD87' Smith", "https://www.md87.co.uk"),
                Developer.create("Gregory 'Greboid' Holmes", "https://www.greboid.com"),
                Developer.create("Shane 'Dataforce' Mc Cormack", "http://home.dataforce.org.uk")
        );
    }

    private List<Developer> createOtherDevelopers() {
        return Lists.newArrayList(
                Developer.create("Simon 'Demented-Idiot' Mott", "http://simonmott.co.uk/")
        );
    }

    private List<InfoItem> createInfoItems() {
        return Lists.newArrayList(
                InfoItem.create("DMDirc version",
                        clientInfo.getVersionInformation()),
                InfoItem.create("Mode Aliases version",
                        globalConfig.getOption("identity", "modealiasversion")),
                InfoItem.create("OS Version",
                        clientInfo.getOperatingSystemInformation() ),
                InfoItem.create("Profile directory",
                        baseDirectory.toString()),
                InfoItem.create("Java version",
                        clientInfo.getJavaInformation()),
                InfoItem.create("Java Default charset",
                        Charset.defaultCharset().displayName()),
                InfoItem.create("Client Uptime",
                        DateUtils.formatDuration((int) clientInfo.getUptime() / 1000))
        );
    }

    @Override
    public String getAbout() {
        return about;
    }

    @Override
    public List<Developer> getMainDevelopers() {
        return Collections.unmodifiableList(mainDevelopers);
    }

    @Override
    public List<Developer> getOtherDevelopers() {
        return Collections.unmodifiableList(otherDevelopers);
    }

    @Override
    public List<InfoItem> getInfo() {
        return Collections.unmodifiableList(info);
    }

    @Override
    public List<LicensedComponent> getLicensedComponents() {
        return Collections.unmodifiableList(licences);
    }
}
