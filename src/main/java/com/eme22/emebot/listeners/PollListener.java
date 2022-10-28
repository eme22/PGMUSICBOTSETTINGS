package com.eme22.emebot.listeners;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.entities.Poll;
import com.eme22.emebot.settings.Settings;
import com.eme22.emebot.utils.OtherUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PollListener extends ListenerAdapter {

    private final Bot bot;

    public PollListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot())
            return;

        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null && settings.getPolls().stream().anyMatch(poll -> poll.getId() == event.getMessageIdLong())) {
            Poll polls = settings.getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);
            int num = OtherUtil.EmojiToNumber(event.getReaction().getReactionEmote().getEmoji());
            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())) {
                event.getUser().openPrivateChannel().queue(success -> success.sendMessage(bot.getConfig().getErrorEmoji() + " Solo puedes votar una vez").queue(m ->
                        m.delete().queueAfter(30, TimeUnit.SECONDS)));
                event.getReaction().removeReaction(event.getUser()).queue();
            } else {
                if (polls != null) {
                    polls.addVoteToAnswer(num, event.getUserIdLong());
                    event.getChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();
                }

            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getUser().isBot())
            return;

        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null && settings.getPolls().stream().anyMatch(poll -> poll.getId() == event.getMessageIdLong())) {
            Poll polls = settings.getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);
            int num = OtherUtil.EmojiToNumber(event.getReaction().getReactionEmote().getEmoji());
            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())) {
                if (polls.isUserParticipatingInAnswer(num, event.getUserIdLong())) {
                    polls.removeVoteFromAnswer(num, event.getUserIdLong());
                    event.getChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();
                }
            }
        }
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
        if (settings != null) {
            settings.removePollFromGuild(event.getMessageIdLong());
            settings.deleteRoleManagers(event.getMessageIdLong());
        }
    }
}
