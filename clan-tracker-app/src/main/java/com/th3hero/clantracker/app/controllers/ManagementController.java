package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.app.services.ClanTrackerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private static final String CLAN_TAG_SIZE = "Clan tag must be between 2 and 5 characters long.";
    private static final String CLAN_TAG_BLANK = "Clan tag must not be blank.";

    @PostMapping("/track-clan")
    @Operation(summary = "Add a clan to the tracking list")
    @ResponseStatus(HttpStatus.CREATED)
    public void addClanForTracking(
        @Size(min = 2, max = 5, message = CLAN_TAG_SIZE) @NotBlank(message = CLAN_TAG_BLANK) String clanTag
    ) {
        clanTrackerService.addClanForTracking(clanTag);
    }

    @DeleteMapping("/track-clan")
    @Operation(summary = "Remove a clan from the tracking list")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeClanFromTracking(
        @Size(min = 2, max = 5, message = CLAN_TAG_SIZE) @NotBlank(message = CLAN_TAG_BLANK) String clanTag
    ) {
        clanTrackerService.removeClanFromTracking(clanTag);
    }

//    @PostMapping(value = "/import", consumes = "multipart/form-data")
//    @Operation(summary = "Import existing activity data csv file")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void importExistingActivityData(
//        @NotNull(message = "Clan id must be provided.") Long clanId,
//        @NotNull(message = "CSV file for importing must be provided.") @RequestParam("file") MultipartFile file
//    ) {
//        clanTrackerService.importExistingClanActivity(file, clanId);
//    }
}
