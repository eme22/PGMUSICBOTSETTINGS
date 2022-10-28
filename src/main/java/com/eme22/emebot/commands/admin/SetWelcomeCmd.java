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
import com.eme22.emebot.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class SetWelcomeCmd extends AdminCommand
{
    public SetWelcomeCmd(Bot bot)
    {
        this.name = "sethello";
        this.help = "especifica un canal para las bienvenidas";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para mensaje de bienvenidas. Se utilizara el canal por defecto si esta activado.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        MessageChannel channel = event.getOption("canal").getAsMessageChannel();

        Settings s = getClient().getSettingsFor(event.getGuild());

        s.setBienvenidasChannelId(channel.getIdLong());
        event.reply(getClient().getSuccess()+" El canal de las bienvenidas es ahora <#"+channel.getId()+">").queue();

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError(" Ponga un canal de texto o NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setBienvenidasChannelId(0);
            event.replySuccess(" El canal de las bienvenidas ha sido quitado.");
        }
        else
        {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.replyWarning(" No Text Channels found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.replyWarning(FormatUtil.listOfTChannels(list, event.getArgs()));
            else
            {
                s.setBienvenidasChannelId(list.get(0).getIdLong());
                event.replySuccess(" El canal de las bienvenidas es ahora <#"+list.get(0).getId()+">");
            }
        }
    }
    
}
