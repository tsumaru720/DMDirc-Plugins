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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.updater.UpdateChecker;

/**
 * Forces the client to check for an update.
 */
public class ForceUpdate extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param command Parent command
     */
    public ForceUpdate(final Debug command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "forceupdate";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Forces a client update check";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (IdentityManager.getGlobalConfig().getOptionBool("updater",
                "enable")) {
            new Thread(new UpdateChecker(), "Forced update checker").start();
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Update checking is "
                    + "currenty disabled.  You can enable it by typing:");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, Styliser.CODE_FIXED
                    + "    /set updater enable true");
        }
    }

}
