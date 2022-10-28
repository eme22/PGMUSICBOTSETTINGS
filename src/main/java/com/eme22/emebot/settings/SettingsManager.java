/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.emebot.settings;

import com.eme22.emebot.config.BotConfiguration;
import com.eme22.emebot.repository.SettingsRepository;
import com.eme22.emebot.utils.OtherUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
@Log4j2
public class SettingsManager implements GuildSettingsManager<Settings> {
    private final static double SKIP_RATIO = .55;
    private final HashMap<Long, Settings> settings = new HashMap<>();
    private final SettingsRepository repository;
    private final ApplicationArguments applicationArgument;
    private final BotConfiguration botConfiguration;

    @Autowired
    public SettingsManager(SettingsRepository settingsRepository, ApplicationArguments applicationArgument, BotConfiguration botConfiguration) {
        this.repository = settingsRepository;
        this.applicationArgument = applicationArgument;
        this.botConfiguration = botConfiguration;
    }

    @PostConstruct
    public void loadSettings() {
        try {

            loadOldData();

            Iterable<Settings> settings = repository.findAll();

            if (settings.spliterator().getExactSizeIfKnown() == 0) {

                ObjectMapper mapper = new ObjectMapper();
                File file = new File("serversettings.json");
                if (!file.exists())
                    throw new IOException();
                HashMap<Long, Settings> temp_Settings = mapper.readValue(file, new TypeReference<>() {
                });
                temp_Settings.forEach((aLong, settingsTEST) -> {
                    settingsTEST.setGuild(aLong);
                    this.settings.put(aLong, settingsTEST);
                    repository.save(settingsTEST);
                });
            } else {
                for (Settings guildSetting : settings) {

                    log.info("Settings loaded: "+ guildSetting.toString());
                    this.settings.put(guildSetting.getGuild(), guildSetting);

                }
            }

        } catch (IOException e) {
            LoggerFactory.getLogger("Settings")
                    .warn("Failed to load server settings (this is normal if no settings have been set yet): " + e);
        }
    }

    /**
     * Gets non-null settings for a Guild
     * 
     * @param guildRegister the guild to get settings for
     * @return the existing settings, or new settings for that guild
     */
    @Override
    public Settings getSettings(Guild guildRegister) {
        return getSettings(guildRegister.getIdLong());
    }

    public Settings getSettings(long guildId) {
        Settings data = null;
        try {
            data = settings.computeIfAbsent(guildId, id -> createDefaultSettings(guildId));
        } catch (Exception e) {
            log.error("Ha habido un error obteniendo las configuraciones", e);
        }
        return data;
    }

    protected Settings createDefaultSettings(long guildId) {
        return new Settings(this, guildId, 0, 0, 0, 0, BotConfiguration.DEFAULT_VOLUME, null, RepeatMode.OFF, null, SKIP_RATIO, false,0, null,
                null,false,0, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(),new ArrayList<>(), 0,false);
    }

    @PreDestroy
    public void writeSettings() {
        log.info("Guardando Datos...");
        repository.saveAll(this.settings.values());
    }

    public void saveSettings(Settings settings) {
        log.info("Guardando Datos...");
        repository.save(settings);
    }

    protected void deleteSettings(String guild) {
        settings.remove(Long.parseLong(guild));
        repository.deleteById(Long.valueOf(guild));
    }

    void loadOldData() {

        boolean dev = OtherUtil.checkDev(applicationArgument.getSourceArgs());

        log.info("Developer Mode: " + dev);

        if (!dev) {
            try {
                OtherUtil.loadFileFromGit(new File("serversettings.json"), botConfiguration.getGithubToken());
            } catch (IOException | NoSuchAlgorithmException | NullPointerException e) {
                log.warn("Se ha fallado en cargar las opciones del servidor, se usaran las locales: " + e);

            }
        }


    }
}
