/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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
package com.eme22.emebot.commands.dj;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.audio.AudioHandler;
import com.eme22.emebot.commands.DJCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class SkiptoCmd extends DJCommand 
{
    public SkiptoCmd(Bot bot)
    {
        super(bot);
        this.name = "skipto";
        this.help = "skips to the specified song";
        this.arguments = "<posicion>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "Posicion para cambiar a la cola").setRequired(true));

    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        int index = 0;
        try
        {
            index = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException e)
        {
            event.reply(event.getClient().getError()+" `"+event.getArgs()+"` is not a valid integer!");
            return;
        }
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(index<1 || index>handler.getQueue().size())
        {
            event.reply(event.getClient().getError()+" Position must be a valid integer between 1 and "+handler.getQueue().size()+"!");
            return;
        }
        handler.getQueue().skip(index-1);
        event.reply(event.getClient().getSuccess()+" Skipped to **"+handler.getQueue().get(0).getTrack().getInfo().title+"**");
        handler.getPlayer().stopTrack();
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        OptionMapping option = event.getOption("posicion");
        if (option == null)
            return;
        int index = 0;
        try
        {
            index = Integer.parseInt(option.getAsString());
        }
        catch(NumberFormatException e)
        {
            event.reply(getClient().getError()+" `"+option.getAsString()+"` is not a valid integer!").queue();
            return;
        }
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(index<1 || index>handler.getQueue().size())
        {
            event.reply(getClient().getError()+" Position must be a valid integer between 1 and "+handler.getQueue().size()+"!").queue();
            return;
        }
        handler.getQueue().skip(index-1);
        event.reply(getClient().getSuccess()+" Skipped to **"+handler.getQueue().get(0).getTrack().getInfo().title+"**").queue();
        handler.getPlayer().stopTrack();
    }
}
