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
package com.eme22.emebot.listeners;

import com.eme22.emebot.audio.AudioHandler;
import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.entities.RoleManager;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eme22.emebot.config.ApplicationConfiguration.RECOMMENDED_PERMS;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@SuppressWarnings("ConstantConditions")
@Component
public class Listener extends ListenerAdapter {
    private final Bot bot;
    private static final Logger log = LoggerFactory.getLogger("MBotApplicationBot - Listener");

    public Listener(Bot bot) {
        this.bot = bot;
    }

    private String setupMessage = null;

    private final HashMap<String, Integer> tempChannels = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuildCache().isEmpty()) {

            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(RECOMMENDED_PERMS));
        }
        // credit(event.getJDA());
        event.getJDA().getGuilds().forEach((guild) -> {
            try {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if (defpl != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
                bot.getBirthdayManager().setupBirthdays(guild);
            } catch (Exception ignore) {
            }
        });
        if (bot.getConfig().isUpdatealerts()) {
            bot.getThreadpool().scheduleWithFixedDelay(() -> {
                try {
                    User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwner()).complete();
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if (latestVersion != null && !currentVersion.equalsIgnoreCase(latestVersion)) {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                } catch (Exception ignored) {
                } // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
        ArrayList<TextChannel> bannedTextChannels = bot.getSettingsManager().getSettings(event.getGuild())
                .getOnlyImageChannels(event.getGuild());

        if (bannedTextChannels.contains(event.getChannel())) {
            List<MessageEmbed> message = event.getMessageEmbeds();

            AtomicBoolean deletable = new AtomicBoolean(true);

            message.forEach(messageEmbed -> {
                if (messageEmbed.getImage() != null || messageEmbed.getVideoInfo() != null)
                    deletable.set(false);
            });

            if (deletable.get())
                event.getChannel().deleteMessageById(event.getMessageId()).complete();
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        ArrayList<TextChannel> bannedTextChannels = bot.getSettingsManager().getSettings(event.getGuild())
                .getOnlyImageChannels(event.getGuild());

        if (bannedTextChannels.contains(event.getChannel())) {
            Message message = event.getMessage();

            if (message.getContentRaw().contains("delimagechannel"))
                return;

            if (message.getContentRaw().contains("https://"))
                return;

            if (message.getAttachments().isEmpty())
                message.delete().complete();

        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        bot.getNowPlayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());

    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
            return;

        Guild guild = event.getGuild();
        ((AudioHandler) guild.getAudioManager().getSendingHandler()).stopAndClear();
        guild.getAudioManager().closeAudioConnection();
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        bot.shutdown();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("¿Desea configurar el bot?");
        event.getGuild().getDefaultChannel().sendMessageEmbeds(embedBuilder.build()).queue(message -> {
            setupMessage = message.getId();
            message.addReaction("U+2705").queue();
            message.addReaction("U+274C").queue();
        });

    }

    private void setupDefaultChannels(Guild guild) {
        try {
            TextChannel commandsChannel = bot.getSettingsManager().getSettings(guild).getTextChannel(guild);
            TextChannel bienvenidasChannel = bot.getSettingsManager().getSettings(guild).getHelloChannel(guild);
            TextChannel despedidasChannel = bot.getSettingsManager().getSettings(guild).getGoodbyeChannel(guild);
            TextChannel defaultChannel = guild.getDefaultChannel();
            List<TextChannel> channels = guild.getTextChannels();

            if (commandsChannel == null) {
                setupChannel("Comandos de Musica", defaultChannel, channels, 0);
            }
            if (bienvenidasChannel == null) {
                setupChannel("Bienvenidas", defaultChannel, channels, 1);
            }
            if (despedidasChannel == null) {
                setupChannel("Despedidas", defaultChannel, channels, 2);
            }
        } catch (Exception exception) {

            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    private void setupChannel(String title, TextChannel defaultChannel, List<TextChannel> channels, int channel) {
        ArrayList<Message> pages = new ArrayList<>();
        int calculatedPages = (int) Math.ceil((double) channels.size() / 10);
        MessageBuilder mb = new MessageBuilder();
        for (int i = 1; i <= calculatedPages; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append("Seleccione el canal para ").append(title).append(": \n");
            for (int j = (i - 1) * 10; j < Math.min(i * 10, channels.size()); j++) {
                sb.append(OtherUtil.numtoString(j)).append(" ").append(channels.get(j).getName()).append("\n");
            }
            mb.setContent(sb.toString());
            pages.add(mb.build());
        }

        pages.forEach(page -> defaultChannel.sendMessage(page).queue(success -> {

            tempChannels.put(success.getId(), channel);
            for (int i = 0; i < getMessageItems(page); i++) {
                success.addReaction("U+003" + i + " U+FE0F U+20E3").queue();
            }
        }));

    }

    private int getMessageItems(Message message) {
        String[] chans = message.getContentRaw().split("\n");
        chans = Arrays.copyOfRange(chans, 1, chans.length);
        return chans.length;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        if (setupMessage != null && setupMessage.equals(event.getMessageId())) {
            if (event.getReactionEmote().getEmoji().contains("white_check_mark")) {
                setupDefaultChannels(event.getGuild());
                return;
            }

            event.retrieveMessage().complete().delete().complete();
        }

        if (tempChannels.containsKey(event.getMessageId())) {

            String reaction = event.getReactionEmote().getName();
            int channel = Integer.parseInt(reaction.replaceAll("[^\\d.]", ""));
            TextChannel channelId = getChannelFromMessage(channel, event.retrieveMessage().complete());
            if (channelId != null) {

                int mode = tempChannels.get(event.getMessageId());
                if (mode == 0) {
                    for (String key : getKeys(tempChannels, 0)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild()).setTextChannelId(channelId.getIdLong());
                }
                if (mode == 1) {
                    for (String key : getKeys(tempChannels, 1)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild())
                            .setBienvenidasChannelId(channelId.getIdLong());
                }
                if (mode == 2) {
                    for (String key : getKeys(tempChannels, 2)) {

                        Message msgToDelete = event.getTextChannel().retrieveMessageById(key).complete();
                        msgToDelete.delete().complete();

                    }
                    bot.getSettingsManager().getSettings(event.getGuild())
                            .setDespedidasChannelId(channelId.getIdLong());
                }
            }
            return;
        }

        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong())
                .getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReactionEmote().getAsReactionCode();

            //System.out.println(manager.isToggled());

            if (manager.isToggled()) {
                List<MessageReaction>  reactionsList = event.getTextChannel().retrieveMessageById(event.getMessageId()).complete().getReactions();

                reactionsList.forEach(messageReaction -> {
                    List<User> users = messageReaction.retrieveUsers().complete();
                    users.forEach(user -> {

                        if (user.equals(event.getUser()) && !event.getReactionEmote().equals(messageReaction.getReactionEmote())) {
                            messageReaction.removeReaction(user).complete();
                        }
                    });
                });

            }

            Map<String, String> data = manager.getEmoji();

            if (data.containsKey(event.getReactionEmote().getAsReactionCode())) {
                String roleT = data.get(reaction);
                List<Role> list = FinderUtil.findRoles(roleT, event.getGuild());
                event.getGuild().addRoleToMember(event.getUserId(), list.get(0)).queue();
            }

        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot())
            return;

        RoleManager manager = bot.getSettingsManager().getSettings(event.getGuild().getIdLong())
                .getRoleManager(event.getMessageIdLong());

        if (manager != null) {
            String reaction = event.getReactionEmote().getAsReactionCode();
            Map<String, String> datas = manager.getEmoji();

            if (datas.containsKey(reaction)) {
                List<Role> list = FinderUtil.findRoles(datas.get(reaction), event.getGuild());
                event.getGuild().removeRoleFromMember(event.getUserId(), list.get(0)).complete();
            }
        }
    }

    private TextChannel getChannelFromMessage(int channel, Message message) {
        String[] chans = message.getContentRaw().split("\n");
        chans = Arrays.copyOfRange(chans, 1, chans.length);

        if (channel > chans.length)
            return null;

        String channam = chans[channel].split(":")[2].substring(1);

        return message.getGuild().getTextChannelsByName(channam, true).get(0);
    }

    private static Set<String> getKeys(Map<String, Integer> map, Integer value) {

        Set<String> result = new HashSet<>();
        if (map.containsValue(value)) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), value)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;

    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();
        try {

            //System.out.println(bot.getSettingsManager().getSettings(guild).getBienvenidasChannelEnabled());

            if (!bot.getSettingsManager().getSettings(guild).getBienvenidasChannelEnabled())
                return;

            TextChannel bienvenidas = bot.getSettingsManager().getSettings(guild).getHelloChannel(guild);

            if (bienvenidas != null) {
                InputStream bienvenida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), true);
                String userImage = getUserImage(member);
                File converted = getMemberFile(member);

                OtherUtil.createImage("BIENVENIDO", member.getName(), member.getId(), bienvenida, userImage, converted);
                if (!converted.exists()) {

                    log.error("Image not created");
                }

                String message = OtherUtil.getMessage(bot, guild, true);

                if (member.isBot())
                    message = "Un bot ha llegado";

                message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername",
                        guild.getName());

                // builder.setThumbnail("attachment://bienvenida.png");
                bienvenidas.sendMessage(message).addFile(converted).queue(sucess -> {
                    if (converted.delete()) {

                        log.error("Image deleted from memory after succes sended");
                    }
                });

            }
        } catch (Exception exception) {

            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    private String getUserImage(User member) {
        String userImage = member.getAvatarUrl();
        if (userImage == null)
            userImage = member.getDefaultAvatarUrl();
        else
            userImage = member.getAvatarUrl();
        return userImage;
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        User member = event.getMember().getUser();

        try {

            //System.out.println(bot.getSettingsManager().getSettings(guild).getDespedidasChannelEnabled());

            if (!bot.getSettingsManager().getSettings(guild).getDespedidasChannelEnabled())
                return;


            TextChannel despedidas = bot.getSettingsManager().getSettings(guild).getGoodbyeChannel(guild);
            if (despedidas != null) {
                InputStream despedida = OtherUtil.getBackground(bot.getSettingsManager().getSettings(guild), false);

                String userImage = getUserImage(member);

                File converted = getMemberFile(member);

                OtherUtil.createImage("SE VA", member.getName(), member.getId(), despedida, userImage, converted);
                if (!converted.exists()) {

                    log.error("Image not created");
                }

                String message = OtherUtil.getMessage(bot, guild, false);
                message = message.replaceAll("@username", member.getAsMention()).replaceAll("@servername",
                        guild.getName());
                despedidas.sendMessage(message).addFile(converted).queue(sucess -> {
                    if (converted.delete()) {
                        log.error("Image deleted from memory after succes sended");
                    }
                });

            }
        } catch (Exception exception) {
            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    @NotNull
    private File getMemberFile(User member) {

        File parent = new File("temp");
        if (!parent.exists()) {
            if (parent.mkdirs()) {
                log.error("Temp folder successfully created");
            }
        }

        File converted = new File(parent, member.getId() + ".png");
        if (converted.delete()) {
            log.error("Image deleted from memory before new image");
        }
        return converted;
    }

}
