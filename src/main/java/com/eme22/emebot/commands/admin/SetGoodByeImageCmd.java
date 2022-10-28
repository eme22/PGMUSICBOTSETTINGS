package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
@Component
public class SetGoodByeImageCmd extends AdminCommand {

    public SetGoodByeImageCmd(Bot bot)
    {
        this.name = "setgoodbyeimg";
        this.help = "cambia la imagen de despedidas a una personalizada";
        this.arguments = "<link|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "imagen", "imagen de fondo del mensaje de despedidas.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String image = event.getOption("imagen").getAsString();
        Settings s = getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setDespedidasChannelImage(null);
            event.reply(getClient().getSuccess()+" La imagen de despedidas se ha quitado.").queue();
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setDespedidasChannelImage(image);
            event.reply(getClient().getSuccess()+"La imagen de despedidas es ahora "+image).queue();
        }
        else {
            event.reply(getClient().getError()+ " Incluya un link a una imagen valida o NONE para usar la imagen por defecto").setEphemeral(true).queue();
        }
    }
    
    @Override
    protected void execute(CommandEvent event)
    {
        String image = event.getArgs();
        if(image.isEmpty())
        {
            event.replyError(" Incluya un link a una imagen o NONE para usar la imagen por defecto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(image.equalsIgnoreCase("none"))
        {
            s.setDespedidasChannelImage(null);
            event.replySuccess(" La imagen de despedidas se ha quitado.");
            return;
        }
        if (OtherUtil.checkImage(image)){
            s.setDespedidasChannelImage(image);
            event.replySuccess(" La imagen de despedidas es ahora "+image);
        }
        else {
            event.replyError(" Incluya un link a una imagen valida o NONE para usar la imagen por defecto");
        }
    }
}
