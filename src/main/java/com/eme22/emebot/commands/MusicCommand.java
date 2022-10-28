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
import com.eme22.emebot.audio.AudioHandler;
import com.eme22.emebot.settings.Settings;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends SlashCommand
{
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    
    public MusicCommand(Bot bot)
    {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());

        if (!isTextChannelAllowed(event, tchannel)) return;

        switch (OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember())){
            case 0: {
                event.replyError("¡Debes estar en un canal de audio para usar este comando!");
                return;
            }
            case 2: {
                event.replyError("¡No puedes usar ese comando en un canal AFK!");
                return;
            }
        }

        bot.getPlayerManager().setUpHandler(event.getGuild());

        if (bePlaying && !isMusicPlaying(event)) return;
        if (beListening && !isBotConnected(event, settings)) return;

        doCommand(event);
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        Settings settings = getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());

        if (!isTextChannelAllowed(event, tchannel)) return;

        switch (OtherUtil.isUserInVoice(event.getGuild(), settings, event.getMember())){
            case 0: {
                event.reply(getClient().getError()+"¡Debes estar en un canal de audio para usar este comando!").setEphemeral(true).queue();
                return;
            }
            case 2: {
                event.reply("¡No puedes usar ese comando en un canal AFK!").setEphemeral(true).queue();
                return;
            }
        }

        bot.getPlayerManager().setUpHandler(event.getGuild());

        if (bePlaying && !isMusicPlaying(event)) return;
        if (beListening && !isBotConnected(event)) return;

        doCommand(event);
    }

    private boolean isTextChannelAllowed(CommandEvent event, TextChannel tchannel) {
        if (tchannel == null)
            return true;
        else if (!event.getTextChannel().getId().equals(tchannel.getId())) {
            event.getMessage().delete().queue();
            event.replyInDm(event.getClient().getError()+" You can only use that command in "+tchannel.getAsMention()+"!");
            return false;
        }
        return true;

    }

    private boolean isTextChannelAllowed(SlashCommandEvent event, TextChannel tchannel) {
        if (tchannel == null)
            return true;
        else if (!event.getTextChannel().getId().equals(tchannel.getId())) {
            event.reply(getClient().getError()+" You can only use that command in "+tchannel.getAsMention()+"!").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private boolean isMusicPlaying(CommandEvent event) {
        if (!((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError()+" There must be music playing to use that!");
            return false;
        }
        return true;
    }

    private boolean isMusicPlaying(SlashCommandEvent event) {
        if (!((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(getClient().getError()+" There must be music playing to use that!").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    public boolean isBotConnected(CommandEvent event, Settings settings) {

        if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            GuildVoiceState userState = event.getMember().getVoiceState();
            try {
                event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                return true;
            } catch (PermissionException ex) {
                event.reply(event.getClient().getError() + " I am unable to connect to " + userState.getChannel().getAsMention() + "!");
                return false;
            }
        }
        return true;
    }

    public boolean isBotConnected(SlashCommandEvent event){

        if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            GuildVoiceState userState = event.getMember().getVoiceState();
            try {
                event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                return true;
            } catch (PermissionException ex) {
                event.reply(getClient().getError() + " I am unable to connect to " + userState.getChannel().getAsMention() + "!").queue();
                return false;
            }
        }
        return true;

    }

    public abstract void doCommand(CommandEvent event);

    public abstract void doCommand(SlashCommandEvent event);
}
