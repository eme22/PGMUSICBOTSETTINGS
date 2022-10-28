package com.eme22.emebot.commands.general;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.entities.Birthday;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;

@Component
public class SetBirthdayCmd extends SlashCommand {

    Bot bot;

    public SetBirthdayCmd(Bot bot) {
        this.bot = bot;
        this.name = "setbirthday";
        this.help = "agrega tu cumpleaños al servidor: <dia> <mes>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER, "dia", "dia de tu cumpleaños").setRequired(true),
                new OptionData(OptionType.INTEGER, "mes", "mes de tu cumpleaños").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        try {

            int dia = Integer.parseInt(event.getOption("dia").getAsString());
            int mes = Integer.parseInt(event.getOption("mes").getAsString());

            int year = Calendar.getInstance().get(Calendar.YEAR);

            Calendar cal = Calendar.getInstance();
            cal.set(year, mes-1, dia);

            if (cal.isLenient()) {
                event.reply(getClient().getError()+ " La fecha no es valida").setEphemeral(true).queue();
                return;
            }

            if (cal.compareTo(Calendar.getInstance())<0) {
                cal.add(Calendar.YEAR, 1);
            }

            Birthday cumple = new Birthday();
            cumple.setDate(cal.getTime());
            cumple.setActive(true);
            cumple.setUserId(event.getUser().getIdLong());

            bot.getSettingsManager().getSettings(event.getGuild()).addBirthDay(cumple);

            event.reply(getClient().getSuccess()+ "Se recordará tu cumpleaños el "+ dia+ "/"+mes).setEphemeral(true).queue();

    } catch (NumberFormatException e) {
        event.reply(getClient().getError()+ " La fecha no es valida").setEphemeral(true).queue();
    }

    }

    @Override
    protected void execute(CommandEvent event) {





        try {
            String[] args = event.getArgs().split(" ");

            Settings settings = bot.getSettingsManager().getSettings(event.getGuild());

            int dia = Integer.parseInt(args[0]);
            int mes = Integer.parseInt(args[1]);

            int year = Calendar.getInstance().get(Calendar.YEAR);

            Calendar cal = Calendar.getInstance();
            cal.set(year, mes-1, dia);

            if (cal.isLenient()) {
                event.replyError(" La fecha no es valida");
                return;
            }

            if (cal.compareTo(Calendar.getInstance())<0) {
                cal.add(Calendar.YEAR, 1);
            }

            Birthday cumple = new Birthday();
            cumple.setDate(cal.getTime());
            cumple.setActive(true);
            cumple.setUserId(event.getMember().getUser().getIdLong());

            Birthday old = settings.getUserBirthday(event.getMember().getUser().getIdLong());

            if ( old != null) {
                settings.removeBirthDay(event.getMember().getUser().getIdLong());
            }

            settings.addBirthDay(cumple);

            event.replySuccess(getClient().getSuccess()+ "Se recordará tu cumpleaños el "+ dia+ "/"+mes);
        } catch (NumberFormatException e) {
            event.replyError(" La fecha no es valida");
        }




    }
}
