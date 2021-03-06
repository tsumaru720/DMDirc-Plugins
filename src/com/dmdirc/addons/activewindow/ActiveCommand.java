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
package com.dmdirc.addons.activewindow;

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand.IntelligentCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;

/**
 * Executes another command as if it were executed in the active window.
 */
public final class ActiveCommand extends Command implements CommandInfo,
        IntelligentCommand {

    /** Parent MainFrame. */
    private final MainFrame mainFrame;

    /**
     * Creates a new active command.
     *
     * @param mainFrame Parent MainFrame
     */
    public ActiveCommand(final MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final TextFrame frame = mainFrame.getActiveFrame();
        if (frame.getContainer() instanceof WritableFrameContainer) {
            ((WritableFrameContainer) frame.getContainer()).getCommandParser()
                    .parseCommand(frame.getContainer(), context.getSource(),
                    args.getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "active";
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
        return "active <command> - Executes the command as though it had been "
                + "executed in the active window";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return TabCompleter.getIntelligentResults(arg, context, 0);
    }
}
