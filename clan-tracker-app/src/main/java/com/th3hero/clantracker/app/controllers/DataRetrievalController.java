package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.api.ui.ActivityInfo;
import com.th3hero.clantracker.api.ui.Clan;
import com.th3hero.clantracker.api.ui.Config;
import com.th3hero.clantracker.api.ui.PlayerSearch;
import com.th3hero.clantracker.app.services.DataRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/data")
@Tag(name = "Data Retrieval Controller", description = "Allows for the retrieval and preprocessing of data from the database")
@RequiredArgsConstructor
public class DataRetrievalController {
    private final DataRetrievalService dataRetrievalService;

    @GetMapping("/config")
    @Operation(summary = "Get the default date range for activity summary data")
    public Config getDefaultActivitySummaryDateRange() {
        return dataRetrievalService.getDefaultConfig();
    }

    @GetMapping("/clan-list")
    @Operation(summary = "Get a list of all clans")
    public List<Clan> getClanList() {
        return dataRetrievalService.getClanList();
    }

    @GetMapping("/clan-activity/{clanId}")
    @Operation(summary = "Get activity data for a specific clan. If no date range is provided, the default range will be used.")
    public ActivityInfo getClanActivityInfo(
        @PathVariable @NotNull(message = "Clan id must be provided.") Long clanId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        return dataRetrievalService.getClanActivityData(clanId, startDate, endDate);
    }

    @GetMapping("/player-activity/{idOrName}")
    @Operation(summary = "Get search result for a player's activity within the provided time period. If multiple players matching the partial name are found, all matches will be returned.")
    public PlayerSearch getPlayerActivityInfo(
        @PathVariable @NotNull(message = "Player id must be provided.") String idOrName,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        return dataRetrievalService.getPlayerActivity(idOrName, startDate, endDate);
    }
}
