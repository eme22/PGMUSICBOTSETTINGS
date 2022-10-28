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
package com.eme22.emebot.commands.music;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.audio.AudioHandler;
import com.eme22.emebot.audio.QueuedTrack;
import com.eme22.emebot.commands.MusicCommand;
import com.eme22.emebot.playlist.PlaylistLoader.Playlist;
import com.eme22.emebot.utils.FormatUtil;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class PlayCmd extends MusicCommand {
    private final static String LOAD = "\uD83D\uDCE5"; // 📥
    private final static String CANCEL = "\uD83D\uDEAB"; // 🚫

    private final String loadingEmoji;

    public PlayCmd(Bot bot) {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoadingEmoji();
        this.name = "play";
        this.arguments = "<title|URL|subcommand>";
        this.help = "plays the provided song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        // this.children = new SlashCommand[]{new PlaylistCmd(bot)};
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "link", "Busca la cancion, playlist o link que desea reproducir.")
                        .setRequired(false));
    }

    @Override
    public void doCommand(SlashCommandEvent event) {

        OptionMapping option = event.getOption("link");
        if (option == null) {
            /*
             * AudioHandler handler =
             * (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
             * if(handler.getPlayer().getPlayingTrack()!=null &&
             * handler.getPlayer().isPaused()) {
             * if(checkDJPermission(event))
             * {
             * handler.getPlayer().setPaused(false);
             * event.reply(getClient().getSuccess()+
             * "Resumido **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**.").
             * queue();
             * }
             * else
             * event.reply(getClient().getError()+
             * "Solo los DJ pueden utilizar este comando!").setEphemeral(true).queue();
             * return;
             * }
             */
            String builder = getClient().getWarning() + " Comando Play:\n" + "\n`" + getClient().getPrefix() + name +
                    " <titulo>` - reproduce la primera cancion encontrada con ese nombre" +
                    "\n`" + getClient().getPrefix() + name +
                    " <URL>` - reproduce cancion, video, o stream";
            event.reply(builder).setEphemeral(true).queue();
            return;
        }

        String args = option.getAsString().startsWith("<") && option.getAsString().endsWith(">")
                ? option.getAsString().substring(1, option.getAsString().length() - 1)
                : option.getAsString();

        event.reply(loadingEmoji + " Cargando... `[" + args + "]`").queue(
                s -> s.retrieveOriginal().queue(m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args,
                        new ResultHandler(m, null, event, false))));
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            /*
             * AudioHandler handler =
             * (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
             * if(handler.getPlayer().getPlayingTrack() !=null &&
             * handler.getPlayer().isPaused())
             * {
             * if(checkDJPermission(event))
             * {
             * handler.getPlayer().setPaused(false);
             * event.replySuccess("Resumido **"+handler.getPlayer().getPlayingTrack().
             * getInfo().title+"**.");
             * }
             * else
             * event.replyError("Solo los DJ pueden utilizar este comando!");
             * return;
             * }
             */
            StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Comando Play:\n");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name)
                    .append(" <titulo>` - reproduce la primera cancion encontrada con ese nombre");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name)
                    .append(" <URL>` - reproduce cancion, video, o stream");
            for (Command cmd : children)
                builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ")
                        .append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ")
                        .append(cmd.getHelp());
            event.reply(builder.toString());
            return;
        }
        String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();
        event.reply(loadingEmoji + " Cargando... `[" + args + "]`", m -> bot.getPlayerManager()
                .loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, null, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final SlashCommandEvent slashEvent;
        private final CommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message m, CommandEvent event, SlashCommandEvent slashEvent, boolean ytsearch) {
            this.m = m;
            this.event = event;
            this.slashEvent = slashEvent;
            this.ytsearch = ytsearch;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter((slashEvent == null ? event.getClient() : getClient()).getWarning()
                        + " Esta pista (**" + track.getInfo().title + "**) es mas larga que el maximo: `"
                        + FormatUtil.formatTime(track.getDuration()) + "` permitido > `"
                        + FormatUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
                return;

            }

            AudioHandler handler = (AudioHandler) (slashEvent == null ? event.getGuild() : slashEvent.getGuild())
                    .getAudioManager().getSendingHandler();

            int pos = handler.addTrack(
                    new QueuedTrack(track, (slashEvent == null ? event.getAuthor() : slashEvent.getUser()))) + 1;

            String addMsg = FormatUtil.filter((slashEvent == null ? event.getClient() : getClient()).getSuccess()
                    + " Agregado **" + track.getInfo().title
                    + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "to begin playing" : " to the queue at position " + pos));
            if (playlist == null
                    || !(slashEvent == null ? event.getSelfMember() : slashEvent.getGuild().getSelfMember())
                            .hasPermission((slashEvent == null ? event.getTextChannel() : slashEvent.getTextChannel()),
                                    Permission.MESSAGE_ADD_REACTION))
                m.editMessage(addMsg).queue();
            else {
                new ButtonMenu.Builder()
                        .setText(addMsg + "\n" + (slashEvent == null ? event.getClient() : getClient()).getWarning()
                                + " This track has a playlist of **" + playlist.getTracks().size()
                                + "** tracks attached. Select " + LOAD + " to load playlist.")
                        .setChoices(LOAD, CANCEL)
                        .setEventWaiter(bot.getWaiter())
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re -> {
                            if (re.getName().equals(LOAD))
                                m.editMessage(addMsg + "\n"
                                        + (slashEvent == null ? event.getClient() : getClient()).getSuccess()
                                        + " Loaded **" + loadPlaylist(playlist, track) + "** additional tracks!")
                                        .queue();
                            else
                                m.editMessage(addMsg).queue();
                        }).setFinalAction(m -> {
                            try {
                                m.clearReactions().queue();
                            } catch (PermissionException ignore) {
                            }
                        }).build().display(m);
            }
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = { 0 };
            playlist.getTracks().forEach((track) -> {
                if (!bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler) (slashEvent == null ? event.getGuild()
                            : slashEvent.getGuild()).getAudioManager().getSendingHandler();
                    handler.addTrack(
                            new QueuedTrack(track, (slashEvent == null ? event.getAuthor() : slashEvent.getUser())));
                    count[0]++;
                }
            });
            return count[0];
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                AudioTrack single = playlist.getSelectedTrack() == null ? playlist.getTracks().get(0)
                        : playlist.getSelectedTrack();
                loadSingle(single, null);
            } else if (playlist.getSelectedTrack() != null) {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single, playlist);
            } else {
                int count = loadPlaylist(playlist, null);
                if (count == 0) {
                    m.editMessage(FormatUtil.filter((slashEvent == null ? event.getClient() : getClient()).getWarning()
                            + " All entries in this playlist "
                            + (playlist.getName() == null ? ""
                                    : "(**" + playlist.getName()
                                            + "**) ")
                            + "were longer than the allowed maximum (`" + bot.getConfig().getMaxTime() + "`)")).queue();
                } else {
                    m.editMessage(
                            FormatUtil.filter(
                                    (slashEvent == null ? event.getClient() : getClient()).getSuccess() + " Found "
                                            + (playlist.getName() == null ? "a playlist"
                                                    : "playlist **" + playlist.getName() + "**")
                                            + " with `"
                                            + playlist.getTracks().size() + "` entries; added to the queue!"
                                            + (count < playlist.getTracks().size() ? "\n"
                                                    + (slashEvent == null ? event.getClient() : getClient())
                                                            .getWarning()
                                                    + " Tracks longer than the allowed maximum (`"
                                                    + bot.getConfig().getMaxTime() + "`) have been omitted." : "")))
                            .queue();
                }
            }
        }

        @Override
        public void noMatches() {
            if (slashEvent == null) {
                if (ytsearch)
                    m.editMessage(FormatUtil.filter(
                            event.getClient().getWarning() + " No hay resultados para `" + event.getArgs() + "`."))
                            .queue();
                else
                    bot.getPlayerManager().loadItemOrdered(event.getClient(), "ytsearch:" + event.getArgs(),
                            new ResultHandler(m, event, null, true));

            } else {
                OptionMapping arg = slashEvent.getOption("link");
                if (arg == null) {
                    if (ytsearch)
                        m.editMessage(FormatUtil.filter(getClient().getWarning() + " No hay resultados.")).queue();

                } else {
                    if (ytsearch) {
                        Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
                        if (playlist == null) {
                            m.editMessage(FormatUtil.filter(
                                    getClient().getWarning() + " No hay resultados para `" + arg.getAsString() + "`."))
                                    .queue();
                            return;
                        }

                        slashEvent.getChannel().sendMessage(loadingEmoji + " Loading playlist **" + arg.getAsString()
                                + "**... (" + playlist.getItems().size() + " items)").queue(m -> {
                                    AudioHandler handler = (AudioHandler) slashEvent.getGuild().getAudioManager()
                                            .getSendingHandler();
                                    playlist.loadTracks(bot.getPlayerManager(),
                                            (at) -> handler.addTrack(new QueuedTrack(at, slashEvent.getUser())), () -> {
                                                StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                                                        ? getClient().getWarning() + " No tracks were loaded!"
                                                        : getClient().getSuccess() + " Loaded **"
                                                                + playlist.getTracks().size() + "** tracks!");
                                                if (!playlist.getErrors().isEmpty())
                                                    builder.append("\nThe following tracks failed to load:");
                                                playlist.getErrors().forEach(err -> builder.append("\n`[")
                                                        .append(err.getIndex() + 1).append("]` **")
                                                        .append(err.getItem()).append("**: ").append(err.getReason()));
                                                String str = builder.toString();
                                                if (str.length() > 2000)
                                                    str = str.substring(0, 1994) + " (...)";
                                                m.editMessage(FormatUtil.filter(str)).queue();
                                            });
                                });
                    } else
                        bot.getPlayerManager().loadItemOrdered(getClient(), "ytsearch:" + arg.getAsString(),
                                new ResultHandler(m, event, slashEvent, true));
                }
            }

        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON)
                m.editMessage((slashEvent == null ? event.getClient() : getClient()).getError() + " Error loading: "
                        + throwable.getMessage()).queue();
            else
                m.editMessage(
                        (slashEvent == null ? event.getClient() : getClient()).getError() + " Error loading track.")
                        .queue();
        }
    }

    public class PlaylistCmd extends MusicCommand {
        public PlaylistCmd(Bot bot) {
            super(bot);
            this.name = "playlist";
            this.aliases = new String[] { "pl" };
            this.arguments = "<name>";
            this.help = "plays the provided playlist";
            this.beListening = true;
            this.bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + " Please include a playlist name.");
                return;
            }
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
                event.replyError("I could not find `" + event.getArgs() + ".txt` in the Playlists folder.");
                return;
            }
            event.getChannel().sendMessage(loadingEmoji + " Loading playlist **" + event.getArgs() + "**... ("
                    + playlist.getItems().size() + " items)").queue(m -> {
                        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        playlist.loadTracks(bot.getPlayerManager(),
                                (at) -> handler.addTrack(new QueuedTrack(at, event.getAuthor())), () -> {
                                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                                            ? event.getClient().getWarning() + " No tracks were loaded!"
                                            : event.getClient().getSuccess() + " Loaded **"
                                                    + playlist.getTracks().size() + "** tracks!");
                                    if (!playlist.getErrors().isEmpty())
                                        builder.append("\nThe following tracks failed to load:");
                                    playlist.getErrors()
                                            .forEach(err -> builder.append("\n`[").append(err.getIndex() + 1)
                                                    .append("]` **").append(err.getItem()).append("**: ")
                                                    .append(err.getReason()));
                                    String str = builder.toString();
                                    if (str.length() > 2000)
                                        str = str.substring(0, 1994) + " (...)";
                                    m.editMessage(FormatUtil.filter(str)).queue();
                                });
                    });
        }

        @Override
        public void doCommand(SlashCommandEvent event) {

        }
    }
}
