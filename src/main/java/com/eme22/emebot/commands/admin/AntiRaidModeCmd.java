package com.eme22.emebot.commands.admin;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.commands.AdminCommand;
import com.eme22.emebot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.Collections;
@Component
public class AntiRaidModeCmd extends AdminCommand {

    private Bot bot;

    public AntiRaidModeCmd(Bot bot) {
        this.bot = bot;
        this.name = "enableantiraidmode";
        this.help = "agrega un meme para el comando especial de memes, puede ser adjuntado al mensaje";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "estado", "activa o desactiva el modo anti raid.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping canal = event.getOption("estado");
        Settings s = getClient().getSettingsFor(event.getGuild());

        if (canal != null && canal.getAsBoolean()) {
            s.setAntiRaidMode(true);
            makeServerSafe();
        }
        else
            s.setAntiRaidMode(false);



    }

    private void makeServerSafe() {
        bot.getJDA().getRoles().forEach(role -> {
            if (role.hasPermission(Permission.MANAGE_CHANNEL))
                role.getManager().revokePermissions(Permission.MANAGE_CHANNEL);
        });
    }


}
