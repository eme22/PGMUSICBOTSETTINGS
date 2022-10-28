package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
@Component
public class AddMemeCmd extends AdminCommand {

    public AddMemeCmd(Bot bot)
    {
        this.name = "addmeme";
        this.help = "agrega un meme para el comando especial de memes, puede ser adjuntado al mensaje";
        this.arguments = "<meme> <link>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "meme", "nombre o descripcion del meme").setRequired(true),
                new OptionData(OptionType.STRING, "link", "link de la imagen del meme").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String message = Objects.requireNonNull(event.getOption("meme")).getAsString();
        String link = Objects.requireNonNull(event.getOption("link")).getAsString();
        try {
            new URL(link);
        } catch (MalformedURLException e) {
            event.reply(getClient().getError()+" Link Incorrecto").setEphemeral(true).queue();
            return;
        }
        Settings s = getClient().getSettingsFor(event.getGuild());
        s.addToMemeImages(message, link);
        event.reply(getClient().getSuccess()+" Imagen "+ link +" Agregada a la lista de memes").setEphemeral(true).queue();
    }

    @Override
    protected void execute(CommandEvent event) {

        String message;
        String link = null;

        if(event.getArgs().isEmpty()) {
            List<Message.Attachment> attachmentList = event.getMessage().getAttachments();
            if (attachmentList.isEmpty()) {
                event.reply(event.getClient().getError()+" Incluya texto y un link");
                return;
            }
            else {
                link = attachmentList.get(0).getUrl();
            }
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (link != null) {
            message = event.getArgs();
        }
        else {
            String args = event.getArgs();
            message = args.substring(0, args.lastIndexOf(" "));
            link = args.substring(args.lastIndexOf(" ")+1);
        }

        try {
            new URL(link);
        } catch (MalformedURLException e) {
            event.replyError(" Link Incorrecto");
            return;
        }
        s.addToMemeImages(message, link);
        event.reply(event.getClient().getSuccess()+" Imagen "+ link +" Agregada a la lista de memes");


    }
}
