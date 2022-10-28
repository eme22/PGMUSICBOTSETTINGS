package com.eme22.emebot.commands.general;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.emebot.entities.Bot;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

@Log4j2
@Component
public class SendMessageAsCmd extends SlashCommand {

    public SendMessageAsCmd(Bot bot) {
        this.name = "sendmessageas";
        this.help = "envia un mensaje como el usuario seleccionado";
        this.arguments = "[usuario] mensaje";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Arrays.asList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a hacerce pasar.").setRequired(true),
                new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String message = event.getOption("mensaje").getAsString();
        User usuario = event.getOption("usuario").getAsUser();

        try {

            sendFakeMessage(usuario, message, event.getTextChannel());
            event.reply(getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();

        } catch (IOException e) {
            log.error("No se ha podido enviar un mensaje", e);
            event.reply(getClient().getError()+ "Ha ocurrido un error al enviar el mensaje").setEphemeral(true).queue();
        }


    }

    private void sendFakeMessage(User usuario, String message, TextChannel textChannel) throws IOException {

        Member member = textChannel.getGuild().getMember(usuario);

        String avatarUrl;
        String name;

        if (member == null) {
            avatarUrl = usuario.getEffectiveAvatarUrl();
            name = usuario.getName();
        }
        else {
            avatarUrl = member.getEffectiveAvatarUrl();
            name = member.getEffectiveName();
        }


        URL url = new URL(avatarUrl);
        Webhook webhook = textChannel
                .createWebhook(name)
                .setAvatar(Icon.from(new BufferedInputStream(url.openStream())))
                .complete();

        try (JDAWebhookClient client = JDAWebhookClient.from(webhook)) {
            client.send(message); // send a JDA message instance
        } finally {
            webhook.delete().queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getArgs().isEmpty()) {
            event.replyError(" Por favor incluya al menos un usuario y mensaje");
            return;
        }

        String[] data = event.getArgs().split("] ");

        if (data.length != 2) {
            event.replyError(" Parametros incorrectos");
            return;
        }

        User usuario = FinderUtil.findUsers(data[0].substring(1).trim(), event.getJDA()).get(0);
        String message = data[1];

        try {

            sendFakeMessage( usuario, message, event.getTextChannel());

            event.getMessage().delete().queue();

        } catch (IOException e) {
            log.error("No se ha podido enviar un mensaje", e);
            event.replyInDm("Ha ocurrido un error al enviar el mensaje, prueba intentando de nuevo");
        }
    }
}

