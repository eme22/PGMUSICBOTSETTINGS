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

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
public class PrefixCmd extends AdminCommand
{
    public PrefixCmd(Bot bot)
    {
        this.name = "prefix";
        this.help = "pone un prefijo por servidor";
        this.arguments = "<prefix|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "prefix", "Selecciona el prefijo de los comandos (none = limpiar prefijo).").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String prefix = event.getOption("prefix").getAsString();

        Settings s = getClient().getSettingsFor(event.getGuild());
        if(prefix.equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.reply(getClient().getSuccess()+ " Prefijo del servidor limpiado.").queue();
        }
        else
        {
            s.setPrefix(prefix);
            event.reply(getClient().getSuccess()+" Prefijo personalizado fijado en `" + prefix + "` en *" + event.getGuild().getName() + "*").queue();
        }

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please include a prefix or NONE");
            return;
        }
        
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.replySuccess("Prefijo del servidor limpiado.");
        }
        else
        {
            s.setPrefix(event.getArgs());
            event.replySuccess("Prefijo personalizado fijado en `" + event.getArgs() + "` en *" + event.getGuild().getName() + "*");
        }
    }
}
