package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
@Component
public class CloneAndDeleteChannel extends AdminCommand {

    public CloneAndDeleteChannel(Bot bot) {
        this.name = "clonechannel2";
        this.help = "clona el canal especificado y lo borra";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a agregar.").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        TextChannel channel = Objects.requireNonNull(event.getGuild()).getTextChannelById(Objects.requireNonNull(event.getOption("canal")).getAsMessageChannel().getId());

        channel.createCopy().queue(
                success -> {
                    Settings s = getClient().getSettingsFor(event.getGuild());

                    checkAndDeleteChannel(channel, success, s, event.getGuild(), event.getJDA());
                });
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();

        channel.createCopy().queue(
                success -> {
                    Settings s = event.getClient().getSettingsFor(event.getGuild());
                    checkAndDeleteChannel(channel, success, s, event.getGuild(), event.getJDA());
                });
    }

    private void checkAndDeleteChannel(TextChannel channel, TextChannel success, Settings s, Guild guild, JDA jda) {
        if (s.getHelloChannel(guild).getIdLong() == channel.getIdLong())
            s.setBienvenidasChannelId(success.getIdLong());

        if (s.getGoodbyeChannel(guild).getIdLong() == channel.getIdLong())
            s.setDespedidasChannelId(success.getIdLong());

        if (s.getTextChannel(guild).getIdLong() == channel.getIdLong())
            s.setTextChannelId(success.getIdLong());

        checkBienvenidas(guild, jda,success, s);

        channel.delete().queue();
        success.sendMessage("El canal se ha clonado con exito").queue();
    }

    private void checkBienvenidas(Guild guild, JDA jda, TextChannel success, Settings s) {
        String bienvenidasMessage = s.getBienvenidasChannelMessage();
        List<TextChannel> channels = FinderUtil.findTextChannels(bienvenidasMessage, guild);
        List<Role> roles = FinderUtil.findRoles(bienvenidasMessage, guild);
        List<User> users = FinderUtil.findUsers(bienvenidasMessage, jda);
        if (!channels.isEmpty()) {
            bienvenidasMessage = bienvenidasMessage.replace(channels.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
        if (!roles.isEmpty()) {
            bienvenidasMessage = bienvenidasMessage.replace(roles.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
        if (!users.isEmpty()) {
            bienvenidasMessage = bienvenidasMessage.replace(users.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
    }
}
