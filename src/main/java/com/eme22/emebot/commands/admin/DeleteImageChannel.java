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
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class DeleteImageChannel extends AdminCommand
{
    public DeleteImageChannel(Bot bot)
    {
        this.name = "delimagechannel";
        this.help = "elimina un canal de la lista de no texto";
        this.arguments = "<channel>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a quitar.").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        TextChannel textChannel = Objects.requireNonNull(event.getGuild()).getTextChannelById(Objects.requireNonNull(event.getOption("canal")).getAsMessageChannel().getId());
        Settings s = getClient().getSettingsFor(event.getGuild());
        if (s.isOnlyImageChannel(textChannel)){
            s.removeFromOnlyImageChannels(textChannel);
            event.reply(getClient().getSuccess()+" Canal <#"+textChannel.getId()+"> quitado de la lista de canales sin texto").queue();
        }
        else {
            event.reply(getClient().getError() + " Canal <#"+textChannel.getId()+"> no esta en la lista de canales sin texto").setEphemeral(true).queue();
        }

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError(" Incluya un canal de Texto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
        if(list.isEmpty())
            event.replyWarning(" No se han encontrado canales de texto que coincidan con \""+event.getArgs()+"\"");
        else
        {
            list.forEach( textChannel -> {
                if (s.isOnlyImageChannel(textChannel)) {
                    s.removeFromOnlyImageChannels(textChannel);
                    event.replySuccess(" Canal <#" + textChannel.getId() + "> quitado de la lista de canales sin texto");
                }
                else {
                    event.replyError(" Canal <#"+textChannel.getId()+"> no esta en la lista de canales sin texto");
                }
            });

        }

    }
    
}
