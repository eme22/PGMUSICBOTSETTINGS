package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class BotFixedMessageCmd extends SlashCommand {


    public BotFixedMessageCmd(Bot bot) {
        this.name = "messagext";
        this.help = "hace hablar al bot con opciones extendidas";
        this.arguments = "[intervalo: 30S = 30 segundos, 1H = 1 hora, 2D = 2 dias ] [fecha de inicio dd/mm hh:mm:ss] [fecha de fin dd/mm hh:mm:ss] [mensaje (Comandos especiales %day% %month% %date% %time% %who%)]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.category = new Category("Admin", event ->
        {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getAuthor().getId().equals(event.getGuild().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            Role admin = settings.getAdminRoleId(event.getGuild());
            return admin!=null && (event.getMember().getRoles().contains(admin) || admin.getIdLong()==event.getGuild().getIdLong());
        });
        this.guildOnly = true;

        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "intervalo", "intervalo: Ejemplo: 10S: S (Segundo) | M (Minuto) | H (Hora) | D (Dia) ").setRequired(true),
                new OptionData(OptionType.STRING, "inicio", "fecha de inicio dd/mm hh:mm:ss").setRequired(true),
                new OptionData(OptionType.STRING, "fin", "fecha de fin dd/mm hh:mm:ss").setRequired(true),
                new OptionData(OptionType.STRING, "message", "mensaje a decir (Comandos especiales %day% %month% %date% %time% %who% )").setRequired(true)
        );

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping intervalo = event.getOption("intervalo");
        OptionMapping inicio = event.getOption("inicio");
        OptionMapping fin = event.getOption("fin");
        OptionMapping message = event.getOption("message");

        long interval = 0;

        Pattern pattern = Pattern.compile("(\\d?\\d?\\d?\\d)([DHMS])");
        Matcher matcher = pattern.matcher(intervalo.getAsString());
        if (matcher.matches()){
            interval = getInterval(matcher.group(1), matcher.group(2));
        }
        else {
                event.reply(client.getError() + " Inserte un intervalo valido").setEphemeral(true).queue();
        }

        LocalDateTime inicio2;
        LocalDateTime fin2;

        try {
            inicio2 = parseWithDefaultYear( inicio.getAsString());
        }
        catch (DateTimeParseException e){
            event.reply(client.getError() + " Inserte una fecha de inicio validamente formateada, ejemplo: 15/06 20:00:00").setEphemeral(true).queue();
            return;
        }

        try {
            fin2 = parseWithDefaultYear( fin.getAsString());
        }
        catch (DateTimeParseException e){
            event.reply(client.getError() + " Inserte una fecha de finalizacion validamente formateada, ejemplo: 15/06 20:00:00").setEphemeral(true).queue();
            return;
        }

        if (inicio2.isBefore(LocalDateTime.now())) {
            event.reply(client.getError() + " La fecha de inicio no puede ser en el pasado").setEphemeral(true).queue();
            return;
        }

        if (inicio2.isAfter(fin2)) {
            event.reply(client.getError() + " La fecha de inicio no puede ser despues de la fecha de fin").setEphemeral(true).queue();
            return;
        }

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        Runnable task = () -> {

            if (LocalDateTime.now().isAfter(fin2)) {
                scheduler.shutdownNow();
            }
            else{
                event.getChannel().sendMessage(buildMessage(message.getAsString(), fin2, event.getUser())).queue();
            }

        };

        scheduler.scheduleAtFixedRate(
                task,
                LocalDateTime.now().until(inicio2, ChronoUnit.MILLIS),
                interval,
                TimeUnit.MILLISECONDS);

        event.reply("Task Created starting at: "+ inicio2+ "Now: "+LocalDateTime.now()+"Every: "+TimeUnit.MILLISECONDS.toSeconds(interval)+" seconds").setEphemeral(true).queue();

    }

    private long getInterval(String number, String timeunit) {
        switch (timeunit.charAt(0)){
            default: break;
            case 'S': return TimeUnit.SECONDS.toMillis(Long.parseLong(number));
            case 'M': return TimeUnit.MINUTES.toMillis(Long.parseLong(number));
            case 'H': return TimeUnit.HOURS.toMillis(Long.parseLong(number));
            case 'D': return TimeUnit.DAYS.toMillis(Long.parseLong(number));
        }
        return 0;
    }

    private String buildMessage(String message2, LocalDateTime fecha3, User user) {
        message2 = message2.replace("%day%", getDay(fecha3));
        message2 = message2.replace("%date%", getDate(fecha3));
        message2 = message2.replace("%time%", getTime(fecha3));
        message2 = message2.replace("%who%", user.getAsMention());
        message2 = message2.replace("\\n", "\n");
        return message2;
    }


    private String getDay(LocalDateTime fecha){

        LocalDateTime now = LocalDateTime.now();

        boolean today = now.getMonthValue() == fecha.getMonthValue() && now.getDayOfMonth() == fecha.getDayOfMonth();
        boolean tomorrow = now.plusDays(1).getMonthValue() == fecha.getMonthValue() && now.plusDays(1).getDayOfMonth() == fecha.getDayOfMonth();

        if (today)
            return  "HOY";

        if (tomorrow)
            return "MAÑANA";

        switch (fecha.getDayOfWeek()){
            default: return  "";
            case MONDAY: return "LUNES";
            case TUESDAY: return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY: return  "JUEVES";
            case FRIDAY: return "VIERNES";
            case SATURDAY: return "SABADO";
            case SUNDAY: return "DOMINGO";
        }
    }

    private String getDate(LocalDateTime fecha){

        LocalDateTime now = LocalDateTime.now();

        boolean today = now.getMonthValue() == fecha.getMonthValue() && now.getDayOfMonth() == fecha.getDayOfMonth();
        boolean tomorrow = now.plusDays(1).getMonthValue() == fecha.getMonthValue() && now.plusDays(1).getDayOfMonth() == fecha.getDayOfMonth();

        if (today || tomorrow)
            return  "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fecha.format(formatter);
    }

    private static LocalDateTime parseWithDefaultYear(String stringWithoutYear) {
        DateTimeFormatter parseFormatter = new DateTimeFormatterBuilder()
                .appendPattern("dd/MM HH:mm:ss")
                .parseDefaulting(ChronoField.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                .toFormatter(Locale.ENGLISH);
        return LocalDateTime.parse(stringWithoutYear, parseFormatter);
    }

    private String getTime(LocalDateTime fecha){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh.mm a");
        return fecha.format(formatter);
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}
