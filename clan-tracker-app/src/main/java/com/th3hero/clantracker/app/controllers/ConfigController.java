package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.app.dto.Config;
import com.th3hero.clantracker.app.dto.ConfigUpload;
import com.th3hero.clantracker.app.services.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/config")
@Tag(name = "Config Controller", description = "Handles admin operations regarding dynamic configuration")
public class ConfigController {
    private final ConfigService configService;

    @GetMapping
    @Operation(summary = "Returns an existing config")
    public Config getConfig() {
        return configService.getConfig();
    }

    @PostMapping
    @Operation(summary = "Create a new config if non exists")
    @ResponseStatus(HttpStatus.CREATED)
    public Config createConfig(
        @RequestBody @NonNull @Valid ConfigUpload configUpload
    ) {
        return configService.createConfig(configUpload);
    }

    @PatchMapping
    @Operation(summary = "Update the existing config")
    public Config updateConfig(
        @RequestBody @NonNull @Valid ConfigUpload configUpload
    ) {
        return configService.updateConfig(configUpload);
    }
}
