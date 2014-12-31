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

package com.dmdirc.ui.core.newserver;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.config.profiles.ProfileManager;
import com.dmdirc.events.ProfileAddedEvent;
import com.dmdirc.events.ProfileDeletedEvent;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.ui.NewServerDialogModel;
import com.dmdirc.interfaces.ui.NewServerDialogModelListener;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.IntegerPortValidator;
import com.dmdirc.util.validators.ListNotEmptyValidator;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.ServerNameValidator;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a new server dialog model.
 */
public class CoreNewServerDialogModel implements NewServerDialogModel {

    private final ListenerList listeners;
    private final AggregateConfigProvider globalConfig;
    private final ConfigProvider userConfig;
    private final ConnectionManager connectionManager;
    private final ProfileManager profileManager;
    private final List<Profile> profiles;
    private final DMDircMBassador eventBus;
    private Optional<Profile> selectedProfile;
    private Optional<String> hostname;
    private Optional<Integer> port;
    private Optional<String> password;
    private boolean ssl;
    private boolean saveAsDefault;

    @Inject
    public CoreNewServerDialogModel(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @UserConfig final ConfigProvider userConfig,
            final ProfileManager profileManager,
            final ConnectionManager connectionManager,
            final DMDircMBassador eventBus) {
        this.globalConfig = globalConfig;
        this.userConfig = userConfig;
        this.profileManager = profileManager;
        this.connectionManager = connectionManager;
        this.eventBus = eventBus;
        listeners = new ListenerList();
        profiles = new ArrayList<>(5);
        selectedProfile = Optional.empty();
        hostname = Optional.empty();
        port = Optional.empty();
        password = Optional.empty();
        ssl = false;
        saveAsDefault = false;
    }

    @Override
    public void loadModel() {
        eventBus.subscribe(this);
        profiles.addAll(profileManager.getProfiles());
        hostname = Optional.ofNullable(globalConfig.getOption("newserver", "hostname"));
        port = Optional.ofNullable(globalConfig.getOptionInt("newserver", "port"));
        password = Optional.ofNullable(globalConfig.getOption("newserver", "password"));
        ssl = globalConfig.getOptionBool("newserver", "ssl");
        saveAsDefault = false;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public List<Profile> getProfileList() {
        return ImmutableList.copyOf(profiles);
    }

    @Override
    public Optional<Profile> getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public void setSelectedProfile(final Optional<Profile> selectedProfile) {
        checkNotNull(selectedProfile);
        final Optional<Profile> oldSelectedProfile = this.selectedProfile;
        this.selectedProfile = selectedProfile;
        listeners.getCallable(NewServerDialogModelListener.class).selectedProfileChanged(
                oldSelectedProfile, selectedProfile);
    }

    @Override
    public boolean isProfileListValid() {
        return !getProfileListValidator().validate(getProfileList()).isFailure();
    }

    @Override
    public Validator<List<Profile>> getProfileListValidator() {
        return new ListNotEmptyValidator<>();
    }

    @Override
    public Optional<String> getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(final Optional<String> hostname) {
        checkNotNull(hostname);
        this.hostname = hostname;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isHostnameValid() {
        return getHostname().isPresent() && !getHostnameValidator().validate(getHostname().get()).
                isFailure();
    }

    @Override
    public Validator<String> getHostnameValidator() {
        return new ServerNameValidator();
    }

    @Override
    public Optional<Integer> getPort() {
        return port;
    }

    @Override
    public void setPort(final Optional<Integer> port) {
        checkNotNull(port);
        this.port = port;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isPortValid() {
        return getPort().isPresent() && !getPortValidator().validate(getPort().get()).isFailure();
    }

    @Override
    public Validator<Integer> getPortValidator() {
        return new IntegerPortValidator();
    }

    @Override
    public Optional<String> getPassword() {
        return password;
    }

    @Override
    public void setPassword(final Optional<String> password) {
        checkNotNull(password);
        this.password = password;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean isPasswordValid() {
        return !getPassword().isPresent() || !getPasswordValidator().validate(getPassword().get()).
                isFailure();
    }

    @Override
    public Validator<String> getPasswordValidator() {
        return new PermissiveValidator<>();
    }

    @Override
    public boolean getSSL() {
        return ssl;
    }

    @Override
    public void setSSL(final boolean ssl) {
        this.ssl = ssl;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public boolean getSaveAsDefault() {
        return saveAsDefault;
    }

    @Override
    public void setSaveAsDefault(final boolean saveAsDefault) {
        this.saveAsDefault = saveAsDefault;
        listeners.getCallable(NewServerDialogModelListener.class).serverDetailsChanged(hostname,
                port, password, ssl, saveAsDefault);
    }

    @Override
    public void save() {
        if (saveAsDefault) {
            userConfig.
                    setOption("newserver", "hostname", hostname.isPresent() ? hostname.get() : "");
            userConfig.setOption("newserver", "port", port.isPresent() ? port.get() : 6667);
            userConfig.setOption("newserver", "password",
                    password.isPresent() ? password.get() : "");
            userConfig.setOption("newserver", "ssl", ssl);
        }
        try {
            if (selectedProfile.isPresent()) {
                connectionManager.connectToAddress(getServerURI(), selectedProfile.get());
            } else {
                connectionManager.connectToAddress(getServerURI());
            }
        } catch (URISyntaxException ex) {
            //This is tested in isSaveAllowed, shouldn't happen here.
        }
    }

    @Override
    public boolean isSaveAllowed() {
        try {
            getServerURI();
        } catch (URISyntaxException ex) {
            return false;
        }
        return isHostnameValid() && isPortValid() && isPasswordValid() && isProfileListValid();
    }

    @Override
    public void addListener(final NewServerDialogModelListener listener) {
        checkNotNull(listener);
        listeners.add(NewServerDialogModelListener.class, listener);
    }

    @Override
    public void removeListener(final NewServerDialogModelListener listener) {
        checkNotNull(listener);
        listeners.remove(NewServerDialogModelListener.class, listener);
    }

    @Handler
    public void profileAdded(final ProfileAddedEvent event) {
        profiles.add(event.getProfile());
        listeners.getCallable(NewServerDialogModelListener.class).profileListChanged(
                ImmutableList.copyOf(profiles));
    }

    @Handler
    public void profileDeleted(final ProfileDeletedEvent event) {
        if (Optional.of(event.getProfile()).equals(selectedProfile)) {
            selectedProfile = Optional.empty();
            listeners.getCallable(NewServerDialogModelListener.class).selectedProfileChanged(
                    Optional.of(event.getProfile()), selectedProfile);
        }
        profiles.remove(event.getProfile());
        listeners.getCallable(NewServerDialogModelListener.class).profileListChanged(
                ImmutableList.copyOf(profiles));
    }

    /**
     * Gets the URI for the details in the dialog.
     *
     * @return Returns the URI the details represent
     *
     * @throws URISyntaxException    If the resulting URI is invalid
     */
    private URI getServerURI() throws URISyntaxException {
        return new URI("irc" + (ssl ? "s" : ""),
                password.orElse(""),
                hostname.orElse(""),
                port.orElse(6667),
                null, null, null);
    }

}
