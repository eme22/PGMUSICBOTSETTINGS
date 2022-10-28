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

import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public abstract class AdminCommand extends SlashCommand
{
    public AdminCommand()
    {
        this.category = new Category("Admin", event -> 
        {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getAuthor().getId().equals(event.getGuild().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            Role admin = settings.getAdminRoleId(event.getGuild());
            return admin!=null && (event.getMember().getRoles().contains(admin) || admin.getIdLong()==event.getGuild().getIdLong());
        });
        this.guildOnly = true;
    }
}
