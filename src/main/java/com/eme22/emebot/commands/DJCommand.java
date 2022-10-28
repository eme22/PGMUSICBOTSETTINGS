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
package com.eme22.emebot.commands;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public abstract class DJCommand extends MusicCommand
{
    public DJCommand(Bot bot)
    {
        super(bot);
        this.category = new Category("DJ", this::checkDJPermission);
    }
    
    public boolean checkDJPermission(CommandEvent event)
    {
        if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
            return true;
        if (event.getAuthor().equals(event.getGuild().getOwner().getUser()))
            return true;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        Role admin = settings.getAdminRoleId(event.getGuild());
        if(event.getMember().getRoles().contains(admin))
            return true;

        Role dj = settings.getDJRoleId(event.getGuild());
        return dj!=null && (event.getMember().getRoles().contains(dj) || dj.getIdLong()==event.getMember().getIdLong());
    }

    public boolean checkDJPermission(SlashCommandEvent event)
    {
        if(event.getMember().getId().equals(getClient().getOwnerId()))
            return true;
        if(event.getGuild()==null)
            return true;
        Settings settings = getClient().getSettingsFor(event.getGuild());
        Role admin = settings.getAdminRoleId(event.getGuild());
        if(event.getMember().getRoles().contains(admin))
            return true;

        Role dj = settings.getDJRoleId(event.getGuild());
        return dj!=null && (event.getMember().getRoles().contains(dj) || dj.getIdLong()==event.getUser().getIdLong());
    }
}
