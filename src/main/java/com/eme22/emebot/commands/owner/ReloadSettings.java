package com.eme22.emebot.commands.owner;

import com.eme22.emebot.commands.OwnerCommand;
import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Log4j2
@Component
public class ReloadSettings extends OwnerCommand {

    private final Bot bot;

    public ReloadSettings(Bot bot) {
        this.bot = bot;
        this.name = "reloadsettings";
        this.help = "reload all settings";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {

        bot.getSettingsManager().loadSettings();


        try {
            OtherUtil.loadFileFromGit(new File("serversettings.json"), bot.getConfig().getGithubToken());
            bot.getSettingsManager().loadSettings();
            event.replySuccess( " Se han cargado correctamente las opciones del servidor!");
        } catch (IOException | NoSuchAlgorithmException | NullPointerException e) {
            log.warn("Se ha fallado en cargar las opciones del servidor, se usaran las locales: " + e);
            event.replyError(" No se han podido cargar las opciones del servidor!");

        }
    }
}
