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
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
public class SetvcCmd extends AdminCommand 
{
    public SetvcCmd(Bot bot)
    {
        this.name = "setvc";
        this.help = "especifica un canal para la musica";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para especificar canal de voz.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping option = event.getOption("canal");
        VoiceChannel channel = null;
        if (option != null){
            channel = event.getGuild().getVoiceChannelById(option.getAsGuildChannel().getId());
        }

        if (channel != null) {
            Settings s = getClient().getSettingsFor(event.getGuild());
            s.setVoiceChannelId(channel.getIdLong());
            event.reply(getClient().getSuccess()+" Ahora la música sólo puede reproducirse en "+channel.getAsMention()).queue();
        }
        else
            event.reply("Asegurese de que es un canal de voz").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Por favor, incluya un canal de voz o NONE para ninguno");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setVoiceChannelId(0);
            event.reply(event.getClient().getSuccess()+" Ahora se puede reproducir música en cualquier canal");
        }
        else
        {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" No Voice Channels found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+FormatUtil.listOfVChannels(list, event.getArgs()));
            else
            {
                s.setVoiceChannelId(list.get(0).getIdLong());
                event.reply(event.getClient().getSuccess()+" Ahora la música sólo puede reproducirse en "+list.get(0).getAsMention());
            }
        }
    }
}
