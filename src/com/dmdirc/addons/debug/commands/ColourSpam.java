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

/**
 * Outputs a large number of coloured lines.
 */
public class ColourSpam extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param command Parent command
     */
    public ColourSpam(final Debug command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "colourspam";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Spams coloured lines";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        for (int i = 0; i < 100; i++) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    ((char) 3) + "5Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! "
                    + ((char) 3) + "6Colour! " + ((char) 3) + "7Colour! ");
        }
    }

}
