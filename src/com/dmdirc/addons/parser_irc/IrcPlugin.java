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

package com.dmdirc.addons.parser_irc;

import com.dmdirc.parser.common.MyInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.ProtocolDescription;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.parser.irc.IRCProtocolDescription;
import com.dmdirc.plugins.BasePlugin;

import java.net.URI;

/**
 * A plugin which provides access to the IRC Parser.
 *
 * @since 0.6.4
 * @author chris
 */
public class IrcPlugin extends BasePlugin {

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        // Do nothing
    }

    /**
     * Get an IRC parser instance.
     *
     * @param myInfo The client information to use
     * @param address The address of the server to connect to
     * @return An appropriately configured parser
     */
    public Parser getParser(final MyInfo myInfo, final URI address) {
        return new IRCParser(myInfo, address);
    }

    /**
     * Retrieves a description of the IRC protocol.
     *
     * @return An appropriate protocol description object
     */
    public ProtocolDescription getDescription() {
        return new IRCProtocolDescription();
    }

}
