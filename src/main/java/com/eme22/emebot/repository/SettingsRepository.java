package com.eme22.emebot.repository;

import com.eme22.emebot.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface SettingsRepository extends CrudRepository<Settings, Long> {

    @Override
    @NotNull
    @Cacheable("settings")
    Iterable<Settings> findAll();
}
