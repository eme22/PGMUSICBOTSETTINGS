/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.eme22.emebot.commands.general;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class ShowImageChannelsCmd extends SlashCommand
{

    public ShowImageChannelsCmd(Bot bot)
    {
        this.name = "showimgch";
        this.help = "muestra los canales de solo imagen listados en el servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Settings s = getClient().getSettingsFor(event.getGuild());
        MessageBuilder builder = new MessageBuilder().append(" ** Canales de solo Imagenes **");
        ArrayList<TextChannel> onlyimages = s.getOnlyImageChannels(event.getGuild());

        StringBuilder builder1 = new StringBuilder();

        onlyimages.forEach( image -> builder1.append(image.getName()).append(" \n"));

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getGuild().getSelfMember().getColor())
                .setDescription(builder1.toString());
        builder.setEmbeds(ebuilder.build());

        event.reply(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) 
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        MessageBuilder builder = new MessageBuilder().append(" ** Canales de solo Imagenes **");
        ArrayList<TextChannel> onlyimages = s.getOnlyImageChannels(event.getGuild());

        StringBuilder builder1 = new StringBuilder();

        onlyimages.forEach( image -> builder1.append(image.getName()).append(" \n"));

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription(builder1.toString());
        builder.setEmbeds(ebuilder.build());
        event.reply(builder.build());
    }
    
}
