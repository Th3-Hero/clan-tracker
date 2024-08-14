package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.app.services.ClanTrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/management")
@Tag(name = "Management Controller", description = "Handles all operations regarding the management of clans and members")
public class ManagementController {
    private final ClanTrackerService clanTrackerService;

    @PostMapping("/track-clan")
    @Operation(summary = "Add a clan to the tracking list")
    @ResponseStatus(HttpStatus.CREATED)
    public void addClanForTracking(@NotNull @Valid String clanTag) {
        clanTrackerService.addClanForTracking(clanTag);
    }

    @DeleteMapping("/track-clan")
    @Operation(summary = "Remove a clan from the tracking list")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeClanFromTracking(String clanTag) {
        clanTrackerService.removeClanFromTracking(clanTag);
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import existing activity data csv file")
    @ResponseStatus(HttpStatus.CREATED)
    public void importExistingActivityData(@RequestParam("file") MultipartFile file, Long clanId) {
        clanTrackerService.importExistingClanActivity(file, clanId);
    }
}
