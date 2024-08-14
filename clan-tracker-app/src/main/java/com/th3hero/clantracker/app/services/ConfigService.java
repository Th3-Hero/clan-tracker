package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.dto.Config;
import com.th3hero.clantracker.app.dto.ConfigUpload;
import com.th3hero.clantracker.app.listeners.events.MemberActivityUpdateIntervalChangedEvent;
import com.th3hero.clantracker.jpa.entities.ConfigJpa;
import com.th3hero.clantracker.jpa.repositories.ConfigRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ConfigJpa getConfigJpa() {
        List<ConfigJpa> configList = configRepository.findAll();
        if (configList.isEmpty()) {
            log.error("No existing config was found");
            throw new EntityNotFoundException("No existing config was found. Contact the bot owner to fix this issue.");
        }
        // As it stands we should only have one config for the bot
        if (configList.size() > 1) {
            log.error("More then one config was found");
            throw new IllegalStateException("The application has managed to reach an invalid state with multiple configurations. No clue how we got here ¯\\_(ツ)_/¯");
        }

        return configList.getFirst();
    }

    public Config getConfig() {
        return Config.fromJpa(getConfigJpa());
    }

    public Config createConfig(ConfigUpload configUpload) {
        if (!configRepository.findAll().isEmpty()) {
            throw new EntityExistsException("There is already an existing configuration. Please update it instead of creating a new configuration");
        }

        var configJpa = ConfigJpa.builder()
            .memberActivityUpdateInterval(configUpload.memberActivityUpdateInterval())
            .defaultActivitySummaryDateRange(configUpload.defaultActivitySummaryDateRange())
            .performanceThresholdBad(configUpload.performanceThresholdBad())
            .performanceThresholdPoor(configUpload.performanceThresholdPoor())
            .performanceThresholdGood(configUpload.performanceThresholdGood()).build();

        log.debug("Creating new config: {}", configJpa);
        return Config.fromJpa(configRepository.save(configJpa));
    }

    public Config updateConfig(ConfigUpload configUpload) {
        if (configUpload.memberActivityUpdateInterval() != null) {
            updatedMemberActivityUpdateInterval(configUpload);
        }

        ConfigJpa configJpa = getConfigJpa();

        if (configUpload.defaultActivitySummaryDateRange() != null) {
            configJpa.setDefaultActivitySummaryDateRange(configUpload.defaultActivitySummaryDateRange());
        }
        if (configUpload.performanceThresholdBad() != null) {
            configJpa.setPerformanceThresholdBad(configUpload.performanceThresholdBad());
        }
        if (configUpload.performanceThresholdPoor() != null) {
            configJpa.setPerformanceThresholdPoor(configUpload.performanceThresholdPoor());
        }
        if (configUpload.performanceThresholdGood() != null) {
            configJpa.setPerformanceThresholdGood(configUpload.performanceThresholdGood());
        }

        log.debug("Updating config: {}", configJpa);
        return Config.fromJpa(configRepository.save(configJpa));
    }

    private void updatedMemberActivityUpdateInterval(ConfigUpload configUpload) {
        ConfigJpa configJpa = getConfigJpa();
        configJpa.setMemberActivityUpdateInterval(configUpload.memberActivityUpdateInterval());
        configRepository.save(configJpa);
        applicationEventPublisher.publishEvent(new MemberActivityUpdateIntervalChangedEvent());
    }
}
