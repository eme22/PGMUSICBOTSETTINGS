package com.eme22.emebot.commands.general;

import com.eme22.emebot.entities.Bot;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.stereotype.Component;

@Component
public class RemoveBirthdayCmd extends SlashCommand {

    Bot bot;

    public RemoveBirthdayCmd(Bot bot) {
        this.bot = bot;
        this.name = "removebirthday";
        this.help = "borra tu cumpleaños del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

            bot.getSettingsManager().getSettings(event.getGuild()).removeBirthDay(event.getUser().getIdLong());

            event.reply(getClient().getSuccess()+ " Se ha borrado tu cumpleaños").setEphemeral(true).queue();


    }

    @Override
    protected void execute(CommandEvent event) {

        bot.getSettingsManager().getSettings(event.getGuild()).removeBirthDay(event.getMember().getUser().getIdLong());

        event.replySuccess(" Se ha borrado tu cumpleaños");


    }
}
