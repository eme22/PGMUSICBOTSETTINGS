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
public class RemoveMemeCmd extends AdminCommand {

    public RemoveMemeCmd(Bot bot)
    {
        this.name = "delmeme";
        this.help = "borra un meme de la lista de memes";
        this.arguments = "<posicion>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "posicion", "posicion en la que esta el meme a borrar").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int a = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(event.getOption("posicion")).getAsString()));
        Settings s = getClient().getSettingsFor(event.getGuild());
        try {
            s.deleteFromMemeImages(a-1);
        } catch (IndexOutOfBoundsException exception) {
            event.reply(getClient().getError()+ " Numero incorrecto").setEphemeral(true).queue();
            return;
        }
        event.reply(getClient().getSuccess()+" Imagen "+ a +" borrada de la lista de memes").queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        String args = event.getArgs();

        if(args.isEmpty())
        {
            event.reply(event.getClient().getError()+" Incluya un numero");
            return;
        }

        int a;

        try {
            a = Integer.parseInt(args);
        }
        catch (NumberFormatException e){
            event.replyError(" Incluya un numero");
            return;
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());
        try {
            s.deleteFromMemeImages(a-1);
        } catch (IndexOutOfBoundsException exception) {
            event.replyError("Numero incorrecto");
            return;
        }

        event.replySuccess(" Imagen "+ a +" borrada de la lista de memes");


    }
}
