package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.TestEntities;
import com.th3hero.clantracker.app.dto.config.Config;
import com.th3hero.clantracker.app.dto.config.ConfigUpload;
import com.th3hero.clantracker.jpa.repositories.ConfigRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ConfigService configService;

    @Test
    void getConfigJpa() {
        final var config = TestEntities.configJpa();

        when(configRepository.findAll())
            .thenReturn(List.of(config));

        final var result = configService.getConfigJpa();

        assertThat(result).isEqualTo(config);
    }

    @Test
    void getConfigJpa_missingConfig() {
        when(configRepository.findAll())
            .thenReturn(List.of());

        assertThatExceptionOfType(EntityNotFoundException.class)
            .isThrownBy(() -> configService.getConfigJpa());
    }

    @Test
    void getConfigJpa_multipleConfigs() {
        final var config = TestEntities.configJpa();
        final var config2 = TestEntities.configJpa();

        when(configRepository.findAll())
            .thenReturn(List.of(config, config2));

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> configService.getConfigJpa());
    }

    @Test
    void createConfig() {
        when(configRepository.findAll())
            .thenReturn(List.of());

        final var configUpload = TestEntities.configUpload();
        final var configJpa = TestEntities.configJpa();

        when(configRepository.save(any()))
            .thenReturn(configJpa);

        final var result = configService.createConfig(configUpload);

        assertThat(result).isEqualTo(Config.fromJpa(configJpa));
    }

    @Test
    void createConfig_existingConfig() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = TestEntities.configUpload();

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));

        assertThatExceptionOfType(EntityExistsException.class)
            .isThrownBy(() -> configService.createConfig(configUpload));
    }

    @Test
    void updateConfig() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(100, 100, 100, 100, 100);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository, times(2)).save(argThat(config ->
            config.getMemberActivityUpdateInterval() == 100 &&
            config.getDefaultActivitySummaryDateRange() == 100 &&
            config.getPerformanceThresholdBad() == 100 &&
            config.getPerformanceThresholdPoor() == 100 &&
            config.getPerformanceThresholdGood() == 100
        ));
    }

    @Test
    void updateConfig_updateMemberActivityUpdateInterval() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(100, null, null, null, null);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository, times(2)).save(argThat(config ->
            config.getMemberActivityUpdateInterval() == 100
        ));
    }

    @Test
    void updateConfig_updateDefaultActivitySummary() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(null, null, null, null, 100);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository).save(argThat(config ->
            config.getDefaultActivitySummaryDateRange() == 100
        ));
    }

    @Test
    void updateConfig_updatePerformanceThresholdBad() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(null, 100, null, null, null);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository).save(argThat(config ->
            config.getPerformanceThresholdBad() == 100
        ));
    }

    @Test
    void updateConfig_updatePerformanceThresholdPoor() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(null, null, 100, null, null);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository).save(argThat(config ->
            config.getPerformanceThresholdPoor() == 100
        ));
    }

    @Test
    void updateConfig_updatePerformanceThresholdGood() {
        final var configJpa = TestEntities.configJpa();
        final var configUpload = new ConfigUpload(null, null, null, 100, null);

        when(configRepository.findAll())
            .thenReturn(List.of(configJpa));
        when(configRepository.save(any()))
            .thenReturn(configJpa);

        configService.updateConfig(configUpload);

        verify(configRepository).save(argThat(config ->
            config.getPerformanceThresholdGood() == 100
        ));
    }
}