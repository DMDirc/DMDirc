/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandline.CommandLineParser;
import com.dmdirc.events.ClientInfoRequestEvent;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.DateUtils;
import com.dmdirc.util.io.FileUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreAboutDialogModel implements AboutDialogModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreAboutDialogModel.class);
    private final Path baseDirectory;
    private final ClientInfo clientInfo;
    private final DMDircMBassador eventBus;
    private final PluginManager pluginManager;
    private final CommandLineParser commandLineParser;
    private String about;
    private List<Developer> mainDevelopers;
    private List<Developer> otherDevelopers;
    private List<InfoItem> info;
    private List<LicensedComponent> licences;

    @Inject
    public CoreAboutDialogModel(@Directory(DirectoryType.BASE) final Path baseDirectory,
            final ClientInfo clientInfo, final DMDircMBassador eventBus,
            final PluginManager pluginManager, final CommandLineParser commandLineParser) {
        this.baseDirectory = baseDirectory;
        this.clientInfo = clientInfo;
        this.eventBus = eventBus;
        this.pluginManager = pluginManager;
        this.commandLineParser = commandLineParser;
        about = "";
        mainDevelopers = new ArrayList<>();
        otherDevelopers = new ArrayList<>();
        info = new ArrayList<>();
        licences = new ArrayList<>();
    }

    @Override
    public void load() {
        about = "<html><center><h1 style=\"margin-bottom: 0px;\">DMDirc</h1>" +
                "<span style=\"font-style: italic;\">The intelligent IRC client.</span>" +
                "<p>Easy to use, cross-platform IRC client.</p>" +
                "<p><a href=\"https://www.dmdirc.com\">www.dmdirc.com</a></p></center></html>";
        mainDevelopers = createMainDevelopers();
        otherDevelopers = createOtherDevelopers();
        info = createInfoItems();
        final ClientInfoRequestEvent event = new ClientInfoRequestEvent();
        eventBus.publish(event);
        info.addAll(event.getNewInfoItems());
        licences = createLicensedComponents();
    }

    private List<Developer> createMainDevelopers() {
        return Lists.newArrayList(Developer.create("Chris 'MD87' Smith", "https://www.md87.co.uk"),
                Developer.create("Gregory 'Greboid' Holmes", "https://www.greboid.com"),
                Developer.create("Shane 'Dataforce' Mc Cormack", "http://home.dataforce.org.uk"));
    }

    private List<Developer> createOtherDevelopers() {
        return Lists.newArrayList(
                Developer.create("Simon 'Demented-Idiot' Mott", "http://simonmott.co.uk/"));
    }

    private List<InfoItem> createInfoItems() {
        return Lists
                .newArrayList(InfoItem.create("DMDirc version", clientInfo.getVersionInformation()),
                        InfoItem.create("OS Version", clientInfo.getOperatingSystemInformation()),
                        InfoItem.create("Profile directory", baseDirectory.toString()),
                        InfoItem.create("Java version", clientInfo.getJavaInformation()),
                        InfoItem.create("Java Default charset",
                                Charset.defaultCharset().displayName()),
                        InfoItem.create("Client Uptime",
                                DateUtils.formatDuration((int) clientInfo.getUptime() / 1000)),
                        InfoItem.create("Launcher Version",
                                commandLineParser.getLauncherVersion().orElse("Unknown")));
    }

    private List<LicensedComponent> createLicensedComponents() {
        final List<LicensedComponent> components = new ArrayList<>();
        try {
            components.add(LicensedComponent.create("DMDirc", getLicences(FileUtils
                            .getPathForResource(getClass().getResource("/com/dmdirc/licences")))));
            pluginManager.getPluginInfos().forEach(p -> {
                        final List<Licence> componentLicences = getLicences(
                                p.getPath("/META-INF/licences/"));
                        if (!componentLicences.isEmpty()) {
                            components.add(LicensedComponent
                                    .create(p.getMetaData().getFriendlyName(), componentLicences));
                        }
                    }
            );
        } catch (URISyntaxException ex) {
            LOGGER.warn("Unable to create component", ex);
        }
        return components;
    }

    private List<Licence> getLicences(final Path path) {
        final List<Licence> componentLicences = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            stream.forEach(p -> {
                try {
                    final String filename = p.getFileName().toString();
                    final String[] parts = filename.split(" - ");
                    componentLicences.add(Licence.create(parts[0], parts[1],
                            Joiner.on('\n').join(Files.readAllLines(p))));
                } catch (IOException ex) {
                    LOGGER.warn("Error reading licence", ex);
                }
            });
        } catch (IOException ex) {
            LOGGER.warn("Error reading licence directory", ex);
        }
        return componentLicences;
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
