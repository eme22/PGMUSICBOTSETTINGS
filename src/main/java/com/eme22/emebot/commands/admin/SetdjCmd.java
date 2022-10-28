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
import net.dv8tion.jda.api.entities.Role;
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
public class SetdjCmd extends AdminCommand
{
    public SetdjCmd(Bot bot)
    {
        this.name = "setdj";
        this.help = "actualiza el rol de DJ";
        this.arguments = "<rolename|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.ROLE, "rol", "rol a poner de dj. Ponga @Everyone para limpiar").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Role role = event.getOption("rol").getAsRole();
        Settings s = getClient().getSettingsFor(event.getGuild());
        if(role.getIdLong() == event.getGuild().getIdLong()) {
            s.setDjRoleId(0);
            event.reply(getClient().getSuccess()+" Rol de DJ limpiado. Todos pueden usar los comandos de DJ.").queue();
        }
        else {
            s.setDjRoleId(role.getIdLong());
            event.reply(getClient().getSuccess()+" Los comandos de dj ahora pueden ser usados por usuarios con el rol **"+role.getAsMention()+"**.").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Please include a role name or NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setDjRoleId(0);
            event.reply(event.getClient().getSuccess()+" DJ role cleared; Only Admins can use the DJ commands.");
        }
        else
        {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" No Roles found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+FormatUtil.listOfRoles(list, event.getArgs()));
            else
            {
                s.setDjRoleId(list.get(0).getIdLong());
                event.reply(event.getClient().getSuccess()+" DJ commands can now be used by users with the **"+list.get(0).getName()+"** role.");
            }
        }
    }
    
}
