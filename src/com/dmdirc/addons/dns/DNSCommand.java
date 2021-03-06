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

package com.dmdirc.addons.dns;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Performs DNS lookups for nicknames, hostnames or IPs.
 */
public final class DNSCommand extends Command implements CommandInfo {

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "dns", "<IP|hostname>");
            return;
        }

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Resolving: " + args.getArguments()[0]);
        new Timer("DNS Command Timer").schedule(new TimerTask() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                if (args.getArguments()[0].matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Resolved: "
                            + args.getArguments()[0] + ": "
                            + DNSPlugin.getHostname(args.getArguments()[0]));
                } else {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Resolved: "
                            + args.getArguments()[0] + ": "
                            + DNSPlugin.getIPs(args.getArguments()[0]));
                }
            }
        }, 0);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "dns";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "dns <IP|hostname> - Performs DNS lookup of the specified ip/hostname/nickname";
    }

}
