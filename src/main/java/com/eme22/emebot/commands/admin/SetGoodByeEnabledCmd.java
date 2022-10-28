package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
@Component
public class SetGoodByeEnabledCmd extends AdminCommand {

    private Bot bot;

    public SetGoodByeEnabledCmd(Bot bot) {
        this.bot = bot;
        this.name = "setgoodbyeon";
        this.help = "activa o desactiva los mensajes de despedida";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva los mensajes de despedida.").setRequired(true));
        this.arguments = "<true - false>";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping canal = event.getOption("estado");
        Settings s = getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.getAsBoolean()) {
            s.setDespedidasChannelEnabled(true);
        }
        else
            s.setAntiRaidMode(false);
    }

    @Override
    protected void execute(CommandEvent event) {

        String estado = event.getArgs();

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (estado.equals("true")) {
            event.replySuccess(" El mensaje de despedida se ha activado");
            s.setDespedidasChannelEnabled(true);
        }

        else if (estado.equals("false")) {
            event.replySuccess(" El mensaje de despedida se ha desactivado");
            s.setDespedidasChannelEnabled(false);
        }

        else {
            event.replyError(" Ponga un valor valido");
        }
    }
}
