package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
@Component
public class BotMessageCmd extends AdminCommand {

    public BotMessageCmd(Bot bot) {
        this.name = "message";
        this.help = "hace hablar al bot";
        this.arguments = "<mensaje>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
        event.reply(getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();
        event.getChannel().sendMessage(message).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();
        if(message.isEmpty())
        {
            event.replyError(" Incluya un mensaje");
            return;
        }
        event.reply(message);
    }
}
