package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
public class EightBallAnswerList extends AdminCommand {

    private final Paginator.Builder builder;

    public EightBallAnswerList(Bot bot) {
        this.name = "8ballanswers";
        this.help = "muestra la lista de respuestas del comando 8ball del servidor";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(20)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(10, TimeUnit.MINUTES);

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Settings s = getClient().getSettingsFor(event.getGuild());
        List<String> data = s.getEightBallAnswers();
        if (data.isEmpty()){
            event.reply( getClient().getError()+ " No hay respuestas para mostrar").setEphemeral(true).queue();
            return;
        }

        event.reply(getClient().getSuccess()+ " Lista de respuestas del comando 8ball").queue();
        builder.setText("").setItems(data.toArray(new String[0]));
        builder.build().paginate(event.getChannel(), 1);
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        List<String> data = s.getEightBallAnswers();
        if (data.isEmpty()){
            event.replyError(" No hay respuestas para mostrar");
            return;
        }

        event.replySuccess( " Lista de respuestas del comando 8ball");
        builder.setText("").setItems(data.toArray(new String[0]));
        builder.build().paginate(event.getChannel(), 1);
    }
}
