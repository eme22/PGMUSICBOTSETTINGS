package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
@Component
public class BotEmbbedMessageCmd extends AdminCommand {

    public BotEmbbedMessageCmd(Bot bot) {
        this.name = "message2";
        this.help = "hace hablar al bot con mensajes embedidos";
        this.arguments = "<mensaje>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
        event.reply(getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();
        event.getChannel().sendMessageEmbeds(new EmbedBuilder().setDescription(message).build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String message = event.getArgs();
        if(message.isEmpty()) {
            event.replyError(" Incluya un mensaje");
            return;
        }
        event.reply(new EmbedBuilder().setDescription(message).build());
    }

}
