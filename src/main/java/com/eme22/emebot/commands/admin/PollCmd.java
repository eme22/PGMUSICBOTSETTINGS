package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.entities.Answer;
import com.eme22.emebot.entities.Poll;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class PollCmd extends AdminCommand {

    protected final Bot bot;

    public PollCmd(Bot bot) {

        this.bot = bot;
        this.name = "poll";
        this.help = "crea una votacion con los datos enviados";
        this.arguments = "[Question] [Answer 1] [Answer 2]...[Answer 9]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "pregunta", "pregunta a hacer").setRequired(true),
                new OptionData(OptionType.STRING, "respuesta1", "respuesta u opcion").setRequired(true),
                new OptionData(OptionType.STRING, "respuesta2", "respuesta u opcion").setRequired(true),
                new OptionData(OptionType.STRING, "respuesta3", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta4", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta5", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta6", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta7", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta8", "respuesta u opcion").setRequired(false),
                new OptionData(OptionType.STRING, "respuesta9", "respuesta u opcion").setRequired(false)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping questionOption = event.getOption("pregunta");

        List<Answer> responses = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            OptionMapping responseOption = event.getOption("respuesta" + i);
            if (responseOption != null) {
                responses.add(new Answer(responseOption.getAsString()));
            }
        }

        //System.out.println(responses.size());

        Poll poll = new Poll();
        poll.setQuestion(questionOption.getAsString());
        poll.setAnswers(responses);

        EmbedBuilder eb = new EmbedBuilder();

        int answers = responses.size();
        eb.setDescription(OtherUtil.makePollString(poll));
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue(success -> {

            for (int j = 0; j < answers; j++) {
                success.addReaction("U+003" + j + " U+FE0F U+20E3").queue();
            }
            bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).addPollForGuild(success.getIdLong(),
                    poll);

            event.reply(getClient().getSuccess()+ " Encuesta creada con exito. Puedes cancelarla borrando el mensaje.").setEphemeral(true).queue();
        });

    }

    @Override
    protected void execute(CommandEvent event) {

        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError()
                    + " Por favor incluya al menos una Pregunta y 2 respuestas en la pregunta");
            return;
        }

        Poll poll = new Poll();

        Pattern p = Pattern.compile("\\[(.*?)\\]");
        Matcher m = p.matcher(event.getArgs());

        final int[] i = { 1 };
        while (m.find()) {
            if (i[0] >= 10)
                break;
            if (i[0] == 1) {
                poll.setQuestion(m.group(1));
            } else {
                Answer answers = new Answer(m.group(1));
                poll.addAnswer(answers);
            }

            i[0]++;
        }

        if (i[0] == 10)
            i[0] = 9;

        EmbedBuilder eb = new EmbedBuilder();

        int answers = poll.getAnswers().size();
        eb.setDescription(OtherUtil.makePollString(poll));
        event.reply(eb.build(), success -> {
            for (int j = 0; j < answers; j++) {
                success.addReaction("U+003" + j + " U+FE0F U+20E3").queue();
            }
            bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).addPollForGuild(success.getIdLong(),
                    poll);
        });

    }
}
