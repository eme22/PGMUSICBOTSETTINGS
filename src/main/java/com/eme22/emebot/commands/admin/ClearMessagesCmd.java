package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
public class ClearMessagesCmd extends AdminCommand {

    public ClearMessagesCmd(Bot bot)
    {
        this.name = "clear";
        this.help = "limpia los mensajes especificados";
        this.arguments = "<2 - 100>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "mensajes", "numero entre 2 al 100").setMinValue(2).setMaxValue(100).setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int values = Integer.parseInt(event.getOption("mensajes").getAsString());
        List<Message> messages = event.getChannel().getHistory().retrievePast(values).complete();
        event.getTextChannel().deleteMessages(messages).queue();
        event.reply(getClient().getSuccess() +" " + values + " mensajes borrados!").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            int values = Integer.parseInt(event.getArgs());

            if (values < 1 || values > 100) {
                event.replyError("El valor tiene que ser entre 1 y  100!");
                return;
            }

            //event.getMessage().delete();
            List<Message> messages = event.getChannel().getHistory().retrievePast(values+1).complete();
            event.getTextChannel().deleteMessages(messages).queue();
            event.getChannel().sendMessage( event.getClient().getSuccess() +" " + values + " mensajes borrados!").queue(m ->
                    m.delete().queueAfter(5, TimeUnit.SECONDS));

        } catch (NumberFormatException ex) {
            event.replyError("Escribe un numero entre 1 y 100");
        }
    }
}
