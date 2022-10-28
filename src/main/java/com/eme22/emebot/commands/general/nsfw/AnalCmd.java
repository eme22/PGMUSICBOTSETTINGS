package com.eme22.emebot.commands.general.nsfw;

import com.eme22.anime.AnimeImageClient;
import com.eme22.anime.Endpoints;
import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.nsfw.NSFWStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
@Component
public class AnalCmd extends SlashCommand {

    public AnalCmd(Bot bot) {
        this.name = "anal";
        this.help = "anal al usuario seleccionado";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.nsfwOnly = true;
        this.guildOnly = true;
        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a analear.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member memberKisser = event.getMember();
        Member memberKissed = event.getOption("usuario").getAsMember();

        if (memberKissed.getUser().isBot()) {
            event.reply(getClient().getError() + " Asegurese de que el usuario no sea un bot").setEphemeral(true)
                    .queue();
            return;
        }
        if (memberKisser.equals(memberKissed)) {
            event.reply(getClient().getError() + "Asegurese de que el usuario no sea usted").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(memberKisser.getAsMention() + NSFWStrings.getRandomAnal() + memberKissed.getAsMention());
        try {
            builder.setImage(new AnimeImageClient().getImage(Endpoints.KAWAII_NSFW.ANAL));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        event.replyEmbeds(builder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Por favor incluya un nombre");
            return;
        }

        List<Member> member = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (member.isEmpty()) {
            event.replyError("Asegurese de que el usuario exista y no sea un bot");
            return;
        }

        Member memberKisser = event.getMember();
        Member memberKissed = member.get(0);

        if (memberKisser.equals(memberKissed)) {
            event.replyError("Asegurese de que el usuario no sea usted");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(memberKisser.getAsMention() + NSFWStrings.getRandomAnal() + memberKissed.getAsMention());
        try {
            builder.setImage(new AnimeImageClient().getImage(Endpoints.KAWAII_NSFW.ANAL));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        event.reply(builder.build());
    }

    private String getRandomImage() {
        AnimeImageClient animeImageClient = new AnimeImageClient();
        try {
            return animeImageClient.getImage(Endpoints.KAWAII_NSFW.ANAL);

        }
        catch (Exception e) {
            return getRandomImage();
        }
    }
}
