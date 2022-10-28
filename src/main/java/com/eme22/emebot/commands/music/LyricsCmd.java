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
package com.eme22.emebot.commands.music;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.audio.AudioHandler;
import com.eme22.emebot.commands.MusicCommand;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jlyrics.Lyrics;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
public class LyricsCmd extends MusicCommand
{
    
    public LyricsCmd(Bot bot)
    {
        super(bot);
        this.name = "lyrics";
        this.arguments = "[cancion]";
        this.help = "shows the lyrics of a song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "cancion", "Busca la letra de la cancion").setRequired(false));

    }

    @Override
    public void doCommand(CommandEvent event)
    {
        String title;
        if(event.getArgs().isEmpty())
        {
            AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (sendingHandler.isMusicPlaying(event.getJDA()))
                title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
            else
            {
                event.replyError("There must be music playing to use that!");
                return;
            }
        }
        else
            title = event.getArgs();
        event.getChannel().sendTyping().queue();
        // client.getLyrics(title).thenAccept(lyrics -> 
        // {
        //     if(lyrics == null)
        //     {
        //         event.replyError("Lyrics for `" + title + "` could not be found!" + (event.getArgs().isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : ""));
        //         return;
        //     }

        //     showLyrics(event, event.getSelfMember().getColor(), null, title, lyrics);
        // });
        Lyrics lyrics = OtherUtil.getLyrics(title);
        if(lyrics == null)
        {
            event.replyError("Lyrics for `" + title + "` could not be found!" + (event.getArgs().isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : ""));
            return;
        }
        showLyrics(event, event.getSelfMember().getColor(), null, title, lyrics);
    }

    @Override
    public void doCommand(SlashCommandEvent event) {

        OptionMapping option = event.getOption("cancion");

        String title;
        if(option == null || option.getAsString().isEmpty())
        {
            AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (sendingHandler.isMusicPlaying(event.getJDA()))
                title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
            else
            {
                event.reply(getClient().getError()+ " There must be music playing to use that!").setEphemeral(true).queue();
                return;
            }
        }
        else
            title = option.getAsString();
        // event.deferReply().queue( interactionHook -> client.getLyrics(title).thenAccept(lyrics ->
        // {
        //     if(lyrics == null)
        //     {
        //         interactionHook.editOriginal (getClient().getError()+ "Lyrics for `" + title + "` could not be found!" + (title.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")).queue();
        //         return;
        //     }

        //     showLyrics(event, event.getGuild().getSelfMember().getColor(), null, title, lyrics);
        // }));

        event.deferReply().queue(interaction -> {
            Lyrics lyrics = OtherUtil.getLyrics(title);
            if(lyrics == null)
            {
                interaction.editOriginal(getClient().getError()+ "Lyrics for `" + title + "` could not be found!" + (title.isEmpty() ? " Try entering the song name manually (`lyrics [song name]`)" : "")).queue();
                return;
            }

            showLyrics(event, event.getGuild().getSelfMember().getColor(), null, title, lyrics);
        });

    }

    public static void showLyrics(@Nullable CommandEvent event, Color color, TextChannel channel, String title, Lyrics lyrics) {
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(lyrics.getAuthor())
                .setColor(color)
                .setTitle(lyrics.getTitle(), lyrics.getURL());
        if(lyrics.getContent().length()>15000)
        {
            if (event == null)
                channel.sendMessage("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL()).complete();
            else
                event.replyWarning("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL());
        }
        else if(lyrics.getContent().length()>2000)
        {
            String content = lyrics.getContent().trim();
            while(content.length() > 2000)
            {
                int index = content.lastIndexOf("\n\n", 2000);
                if(index == -1)
                    index = content.lastIndexOf("\n", 2000);
                if(index == -1)
                    index = content.lastIndexOf(" ", 2000);
                if(index == -1)
                    index = 2000;
                if (event == null)
                    channel.sendMessageEmbeds(eb.setDescription(content.substring(0, index).trim()).build()).complete();
                else
                    event.reply(eb.setDescription(content.substring(0, index).trim()).build());
                content = content.substring(index).trim();
                eb.setAuthor(null).setTitle(null, null);
            }
            if (event == null)
                channel.sendMessageEmbeds(eb.setDescription(content).build()).complete();
            else
                event.reply(eb.setDescription(content).build());
        }
        else
            if (event == null)
                channel.sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build()).complete();
            else
                event.reply(eb.setDescription(lyrics.getContent()).build());
    }

    private void showLyrics(SlashCommandEvent event, Color color, TextChannel channel, String title, Lyrics lyrics) {
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(lyrics.getAuthor())
                .setColor(color)
                .setTitle(lyrics.getTitle(), lyrics.getURL());
        if(lyrics.getContent().length()>15000)
                event.reply(getClient().getError()+ "Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL()).setEphemeral(true).queue();

        else if(lyrics.getContent().length()>2000)
        {
            String content = lyrics.getContent().trim();
            while(content.length() > 2000)
            {
                int index = content.lastIndexOf("\n\n", 2000);
                if(index == -1)
                    index = content.lastIndexOf("\n", 2000);
                if(index == -1)
                    index = content.lastIndexOf(" ", 2000);
                if(index == -1)
                    index = 2000;
                if (event == null)
                    channel.sendMessageEmbeds(eb.setDescription(content.substring(0, index).trim()).build()).complete();
                else
                    event.replyEmbeds(eb.setDescription(content.substring(0, index).trim()).build()).queue();
                content = content.substring(index).trim();
                eb.setAuthor(null).setTitle(null, null);
            }
            if (event == null)
                channel.sendMessageEmbeds(eb.setDescription(content).build()).complete();
            else
                event.replyEmbeds(eb.setDescription(content).build()).queue();
        }
        else
        if (event == null)
            channel.sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build()).complete();
        else
            event.replyEmbeds(eb.setDescription(lyrics.getContent()).build()).queue();
    }

}
