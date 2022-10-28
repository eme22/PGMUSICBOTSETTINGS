package com.eme22.emebot.config;

import com.eme22.emebot.commands.owner.*;
import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.entities.Prompt;
import com.eme22.emebot.gui.GUI;
import com.eme22.emebot.listeners.Listener;
import com.eme22.emebot.listeners.MusicListener;
import com.eme22.emebot.listeners.PollListener;
import com.eme22.emebot.settings.Settings;
import com.eme22.emebot.settings.SettingsManager;
import com.eme22.emebot.utils.OtherUtil;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.AboutCommand;
import com.typesafe.config.ConfigFactory;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;

@Configuration
@Log4j2
public class ApplicationConfiguration {

    public final static String PLAY_EMOJI = "\u25B6"; // ▶
    public final static String PAUSE_EMOJI = "\u23F8"; // ⏸
    public final static String STOP_EMOJI = "\u23F9"; // ⏹
    public final static Permission[] RECOMMENDED_PERMS = { Permission.MESSAGE_READ, Permission.MESSAGE_WRITE,
            Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.ADMINISTRATOR,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE };
    public final static GatewayIntent[] INTENTS = { GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS };

    private static final String message = "Hola soy MBotApplication' un BOT con lag) (v%s) ";

    private static final String[] features = new String[] {
            "Musica en HQ",
            "Mensaje de bienvenida y despedida configurables",
            "Limpiar mensajes",
            "Votaciones",
            "Memes",
            "Manejo de roles"
    };

    @Bean
    JDA getJDA(BotConfiguration config, CommandClientBuilder commandBuilder, Listener listener, MusicListener musicListener, PollListener pollListener, Bot bot) {
        boolean nogame = false;
        if (config.getStatus() != OnlineStatus.UNKNOWN)
            commandBuilder.setStatus(config.getStatus());
        if (config.getGame() == null)
            commandBuilder.useDefaultGame();
        else if (config.getGame().getName().equalsIgnoreCase("none")) {
            commandBuilder.setActivity(null);
            nogame = true;
        } else
            commandBuilder.setActivity(config.getGame());

        try {

            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .setRateLimitPool(Executors.newScheduledThreadPool(1))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.ONLINE_STATUS)
                    .setActivity(nogame ? null : Activity.playing("loading..."))
                    .setStatus(
                            config.getStatus() == OnlineStatus.INVISIBLE || config.getStatus() == OnlineStatus.OFFLINE
                                    ? OnlineStatus.INVISIBLE
                                    : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(commandBuilder.build(), new EventWaiter(), listener, musicListener,
                            pollListener)
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
            return jda;
        } catch (LoginException e) {
            log.fatal("No se ha podido logear el bot", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    Bot getBot(BotConfiguration config, SettingsManager settings) {
        EventWaiter waiter = new EventWaiter();
        return new Bot(waiter, config, settings);
    }

    @Bean
    CommandClientBuilder getCommandClientBuilder(BotConfiguration config, GuildSettingsManager<Settings> setting, Bot bot, Prompt prompt, Command[] commands, SlashCommand[] slashCommands) {

        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwner()))
                .setEmojis(config.getSuccessEmoji(), config.getWarningEmoji(), config.getErrorEmoji())
                .setHelpWord(config.getHelpWord())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(setting)
                .addSlashCommands(slashCommands)
                .addCommands(commands);

        if (config.getStatus() != OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if (config.getGame() == null)
            cb.useDefaultGame();
        else if (config.getGame().getName().equalsIgnoreCase("none")) {
            cb.setActivity(null);
        } else
            cb.setActivity(config.getGame());

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                log.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        log.info("Loaded config from " + config.getConfigLocation());

        return cb;
    }

    @Bean
    Command getEvalCommand(BotConfiguration config, Bot bot) {

        if (config.isUseEval())
            return new EvalCmd(bot);

        return null;
    }

    @Bean
    AboutCommand getAboutCommand(Prompt prompt, Color color) {

        String version = OtherUtil.checkVersion(prompt);

        AboutCommand aboutCommand = new AboutCommand(
                color,
                String.format(message, version),
                features,
                RECOMMENDED_PERMS);
        aboutCommand.setIsAuthor(false);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB6"); // 🎶

        return aboutCommand;
    }

    @Bean
    Color getColor(){
        return Color.CYAN;
    }

}
