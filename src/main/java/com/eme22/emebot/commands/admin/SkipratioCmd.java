/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
public class SkipratioCmd extends AdminCommand
{
    public SkipratioCmd(Bot bot)
    {
        this.name = "setskip";
        this.help = "pone un radio para el comando skip";
        this.arguments = "<0 - 100>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "radio", "porcentaje de aprobacion para comando voteskip").setMinValue(0).setMaxValue(100).setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int val = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(event.getOption("radio")).getAsString()));
        Settings s = getClient().getSettingsFor(event.getGuild());
        s.setSkipRatio(val / 100.0);
        event.reply(getClient().getSuccess()+ " Skip percentage has been set to `" + val + "%` of listeners on *" + event.getGuild().getName() + "*").queue();

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        try
        {
            int val = Integer.parseInt(event.getArgs().endsWith("%") ? event.getArgs().substring(0,event.getArgs().length()-1) : event.getArgs());
            if( val < 0 || val > 100)
            {
                event.replyError("The provided value must be between 0 and 100!");
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setSkipRatio(val / 100.0);
            event.replySuccess("Skip percentage has been set to `" + val + "%` of listeners on *" + event.getGuild().getName() + "*");
        }
        catch(NumberFormatException ex)
        {
            event.replyError("Please include an integer between 0 and 100 (default is 55). This number is the percentage of listening users that must vote to skip a song.");
        }
    }
}
