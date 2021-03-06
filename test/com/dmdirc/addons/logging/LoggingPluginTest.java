/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.logging;

import com.dmdirc.Channel;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.Query;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.harness.TestLoggingPlugin;
import com.dmdirc.addons.ui_dummy.DummyController;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.ConfigFile;

import java.util.Map;

import static org.mockito.Mockito.*;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoggingPluginTest {
    
    private static Server server;
    private static Channel channel;
    private static Query query;
    private static TestLoggingPlugin lp;

    @BeforeClass
    public static void setUp() throws Exception {
        IdentityManager.load();


        ClientInfo clientinfo = mock(ClientInfo.class);
        when(clientinfo.toString()).thenReturn("foo!bar@baz");
        
        Parser parser = mock(Parser.class);
        when(parser.getClient(anyString())).thenReturn(clientinfo);

        server = mock(Server.class);
        when(server.toString()).thenReturn("server");
        when(server.getParser()).thenReturn(parser);
        
        ChannelInfo info = mock(ChannelInfo.class);
        when(info.toString()).thenReturn("#test");

        channel = mock(Channel.class);
        when(channel.getServer()).thenReturn(server);
        when(channel.getChannelInfo()).thenReturn(info);
        query = mock(Query.class);
        when(query.getServer()).thenReturn(server);
        when(query.toString()).thenReturn("query");
        when(query.getHost()).thenReturn("foo!bar@baz");

        final ConfigFile file = new ConfigFile(LoggingPlugin
                .class.getResourceAsStream("plugin.config"));
        file.read();

        for (Map.Entry<String, String> entry : file.getKeyDomain("defaults").entrySet()) {
            IdentityManager.getAddonIdentity().setOption("temp-plugin-logging",
                    entry.getKey(), entry.getValue());
        }

        lp = new TestLoggingPlugin();
        lp.setDomain("temp-plugin-logging");
        lp.domainUpdated();
        lp.onLoad();
    }
    
    @Test
    public void testChannelOpened() {
        lp.processEvent(CoreActionType.CHANNEL_OPENED, new StringBuffer(),
                channel);
        
        assertTrue(lp.lines.containsKey("#test"));
        assertEquals(2, lp.lines.get("#test").size());
        assertTrue(lp.lines.get("#test").get(1).isEmpty());
        assertTrue(lp.lines.get("#test").get(0).indexOf("opened") > -1);
        lp.lines.clear();
    }
    
    @Test
    public void testChannelClosed() {
        lp.processEvent(CoreActionType.CHANNEL_CLOSED, new StringBuffer(),
                channel);
        
        assertTrue(lp.lines.containsKey("#test"));
        assertEquals(1, lp.lines.get("#test").size());
        assertTrue(lp.lines.get("#test").get(0).indexOf("closed") > -1);
        lp.lines.clear();
    }
    
    @Test
    public void testQueryOpened() {
        lp.processEvent(CoreActionType.QUERY_OPENED, new StringBuffer(),
                query);

        assertTrue(lp.lines.containsKey("foo!bar@baz"));
        assertEquals(3, lp.lines.get("foo!bar@baz").size());
        assertTrue(lp.lines.get("foo!bar@baz").get(2).isEmpty());
        assertTrue(lp.lines.get("foo!bar@baz").get(1).indexOf("foo!bar@baz") > -1);
        assertTrue(lp.lines.get("foo!bar@baz").get(0).indexOf("opened") > -1);
        lp.lines.clear();
    }
    
    @Test
    public void testQueryClosed() {
        lp.processEvent(CoreActionType.QUERY_CLOSED, new StringBuffer(),
                query);
        
        assertTrue(lp.lines.containsKey("foo!bar@baz"));
        assertEquals(1, lp.lines.get("foo!bar@baz").size());
        assertTrue(lp.lines.get("foo!bar@baz").get(0).indexOf("closed") > -1);
        lp.lines.clear();
    }

}
