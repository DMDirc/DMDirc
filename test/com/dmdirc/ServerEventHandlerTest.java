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

package com.dmdirc;

import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.DMDircEvent;
import com.dmdirc.events.ServerCtcpEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.User;
import com.dmdirc.parser.common.CallbackManager;
import com.dmdirc.parser.common.ParserError;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ErrorInfoListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateActionListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerEventHandlerTest {

    @Mock private Server server;
    @Mock private GroupChatManagerImpl groupChatManager;
    @Mock private Parser parser;
    @Mock private CallbackManager callbackManager;
    @Mock private DMDircMBassador eventBus;
    @Mock private ChannelInfo channelInfo;
    @Mock private User user;
    @Mock private UserFactory userFactory;
    @Mock private Query query;
    @Mock private Date date;
    @Mock private ClientInfo clientInfo;

    @Before
    public void setUp() {
        when(server.getParser()).thenReturn(Optional.of(parser));
        when(server.getState()).thenReturn(ServerState.CONNECTED);
        when(parser.getCallbackManager()).thenReturn(callbackManager);
        when(server.getConnection()).thenReturn(Optional.ofNullable(server));
        when(userFactory.getUser(any(Connection.class), eq(clientInfo))).thenReturn(user);
        when(server.getUser(anyString())).thenReturn(user);
        when(server.getLocalUser()).thenReturn(Optional.of(user));
        final ServerEventHandler handler = new ServerEventHandler(server, groupChatManager,
                eventBus);
        handler.registerCallbacks();

        when(user.getNickname()).thenReturn("ho");
        when(user.getUsername()).thenReturn(Optional.of("st"));
        when(user.getHostname()).thenReturn(Optional.of("name"));
    }

    @Test
    public void testSelfJoinAddsChannel() {
        final ChannelSelfJoinListener listener = getCallback(ChannelSelfJoinListener.class);
        listener.onChannelSelfJoin(parser, date, channelInfo);
        verify(groupChatManager).addChannel(channelInfo);
    }

    @Test
    public void testOnPrivateMessageCreatesQuery() {
        when(server.hasQuery("ho!st@name")).thenReturn(false);
        when(server.getQuery("ho!st@name")).thenReturn(query);

        final PrivateMessageListener listener = getCallback(PrivateMessageListener.class);
        listener.onPrivateMessage(parser, date, "message", "ho!st@name");
        verify(server).getQuery("ho!st@name");
    }

    @Test
    public void testOnPrivateMessageRelaysMessage() {
        when(server.hasQuery("ho!st@name")).thenReturn(false);
        when(server.getQuery("ho!st@name")).thenReturn(query);

        final PrivateMessageListener listener = getCallback(PrivateMessageListener.class);
        listener.onPrivateMessage(parser, date, "message", "ho!st@name");
        verify(query).onPrivateMessage(parser, date, "message", "ho!st@name");
    }

    @Test
    public void testOnPrivateActionCreatesQuery() {
        when(server.hasQuery("ho!st@name")).thenReturn(false);
        when(server.getQuery("ho!st@name")).thenReturn(query);

        final PrivateActionListener listener = getCallback(PrivateActionListener.class);
        listener.onPrivateAction(parser, date, "message", "ho!st@name");
        verify(server).getQuery("ho!st@name");
    }

    @Test
    public void testOnPrivateActionRelaysMessage() {
        when(server.hasQuery("ho!st@name")).thenReturn(false);
        when(server.getQuery("ho!st@name")).thenReturn(query);

        final PrivateActionListener listener = getCallback(PrivateActionListener.class);
        listener.onPrivateAction(parser, date, "message", "ho!st@name");
        verify(query).onPrivateAction(parser, date, "message", "ho!st@name");
    }

    @Test
    public void testOnErrorInfoEmitsUserErrorEvent() {
        final ParserError error = mock(ParserError.class);
        when(parser.getServerInformationLines()).thenReturn(Lists.newArrayList("iline1", "iline2"));
        when(error.getLastLine()).thenReturn("lastline");
        when(server.getAddress()).thenReturn("serveraddress");
        when(error.getData()).thenReturn("DATA");
        when(error.isUserError()).thenReturn(true);

        final ErrorInfoListener listener = getCallback(ErrorInfoListener.class);
        listener.onErrorInfo(parser, date, error);

        final UserErrorEvent event = getAsyncEvent(UserErrorEvent.class);
        assertEquals("DATA", event.getMessage());
    }

    @Test
    public void testOnErrorInfoEmitsAppErrorEvent() {
        final ParserError error = mock(ParserError.class);
        when(parser.getServerInformationLines()).thenReturn(Lists.newArrayList("iline1", "iline2"));
        when(error.getLastLine()).thenReturn("lastline");
        when(server.getAddress()).thenReturn("serveraddress");
        when(error.getData()).thenReturn("DATA");
        when(error.isUserError()).thenReturn(false);

        final ErrorInfoListener listener = getCallback(ErrorInfoListener.class);
        listener.onErrorInfo(parser, date, error);

        final AppErrorEvent event = getAsyncEvent(AppErrorEvent.class);
        assertEquals("DATA", event.getMessage());
    }

    @Test
    public void testOnErrorInfoIncludesConnectionDetails() {
        final ParserError error = mock(ParserError.class);
        when(parser.getServerInformationLines()).thenReturn(Lists.newArrayList("iline1", "iline2"));
        when(error.getLastLine()).thenReturn("lastline");
        when(server.getAddress()).thenReturn("serveraddress");
        when(error.getData()).thenReturn("DATA");
        when(error.isUserError()).thenReturn(false);

        final ErrorInfoListener listener = getCallback(ErrorInfoListener.class);
        listener.onErrorInfo(parser, date, error);

        final AppErrorEvent event = getAsyncEvent(AppErrorEvent.class);
        assertNotNull(event.getThrowable());
        assertTrue(event.getThrowable().getMessage().contains("lastline"));
        assertTrue(event.getThrowable().getMessage().contains("iline1"));
        assertTrue(event.getThrowable().getMessage().contains("iline2"));
        assertTrue(event.getThrowable().getMessage().contains("serveraddress"));
    }

    @Test
    public void testOnErrorInfoIncludesException() {
        final ParserError error = mock(ParserError.class);
        final Exception exception = mock(Exception.class);
        when(parser.getServerInformationLines()).thenReturn(Lists.newArrayList("iline1", "iline2"));
        when(error.getLastLine()).thenReturn("lastline");
        when(server.getAddress()).thenReturn("serveraddress");
        when(error.getData()).thenReturn("DATA");
        when(error.isException()).thenReturn(true);
        when(error.getException()).thenReturn(exception);

        final ErrorInfoListener listener = getCallback(ErrorInfoListener.class);
        listener.onErrorInfo(parser, date, error);

        final AppErrorEvent event = getAsyncEvent(AppErrorEvent.class);
        assertNotNull(event.getThrowable());
        assertEquals(exception, event.getThrowable());
    }

    @Test
    public void testOnPrivateCTCPRaisesEvent() {
        when(server.getUser("ho!st@name")).thenReturn(user);

        final PrivateCtcpListener listener = getCallback(PrivateCtcpListener.class);
        listener.onPrivateCTCP(parser, date, "type", "message", "ho!st@name");

        final ServerCtcpEvent event = getEvent(ServerCtcpEvent.class);
        assertEquals("type", event.getType());
        assertEquals("message", event.getContent());
        assertEquals(user, event.getUser());
    }

    @Test
    public void testOnPrivateCTCPSendsReplyIfEventUnhandled() {
        when(server.getUser("ho!st@name")).thenReturn(user);

        final PrivateCtcpListener listener = getCallback(PrivateCtcpListener.class);
        listener.onPrivateCTCP(parser, date, "type", "message", "ho!st@name");

        verify(server).sendCTCPReply("ho", "type", "message");
    }

    private <T extends CallbackInterface> T getCallback(final Class<T> type) {
        final ArgumentCaptor<T> captor = ArgumentCaptor.forClass(type);
        verify(callbackManager).addCallback(eq(type), captor.capture());
        assertNotNull(captor.getValue());
        return captor.getValue();
    }

    private <T extends DMDircEvent> T getAsyncEvent(final Class<T> type) {
        final ArgumentCaptor<T> captor = ArgumentCaptor.forClass(type);
        verify(eventBus).publishAsync(captor.capture());
        assertNotNull(captor.getValue());
        return captor.getValue();
    }

    private <T extends DMDircEvent> T getEvent(final Class<T> type) {
        final ArgumentCaptor<T> captor = ArgumentCaptor.forClass(type);
        verify(eventBus).publish(captor.capture());
        assertNotNull(captor.getValue());
        return captor.getValue();
    }

}