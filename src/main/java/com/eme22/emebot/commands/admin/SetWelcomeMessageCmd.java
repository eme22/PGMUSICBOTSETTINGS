package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
@Component
public class SetWelcomeMessageCmd extends AdminCommand {

    public SetWelcomeMessageCmd(Bot bot) {
        this.name = "setwelcomemsg";
        this.help = "cambia el mensaje de bienvenida";
        this.arguments = "<message>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mensaje", "mensaje a decir cuando un usuario ingresa el servidor.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("mensaje")).getAsString();
        Settings s = getClient().getSettingsFor(event.getGuild());
        s.setBienvenidasChannelMessage(message);
        event.reply(getClient().getSuccess() + "El mensaje de bienvenida es ahora: \n" + "\"" + message + "\"").queue();

    }

    @Override
    protected void execute(CommandEvent event) {
        String image = event.getArgs();
        if (image.isEmpty()) {
            event.replyError(" Incluya un texto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        s.setBienvenidasChannelMessage(image);
        event.replySuccess(" El mensaje de bienvenida es ahora: \n" + "\"" + image + "\"");

    }
}