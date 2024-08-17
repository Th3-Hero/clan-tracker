package com.th3hero.clantracker.app.controllers;

import com.th3hero.clantracker.jpa.ui.ActivityInfo;
import com.th3hero.clantracker.jpa.ui.Clan;
import com.th3hero.clantracker.app.services.DataRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/data")
@Tag(name = "Data Retrieval Controller", description = "Allows for the retrieval and preprocessing of data from the database")
@RequiredArgsConstructor
public class DataRetrievalController {
    private final DataRetrievalService dataRetrievalService;

    @GetMapping("/defaults")
    @Operation(summary = "Get the default date range for activity summary data")
    public Integer getDefaultActivitySummaryDateRange() {
        return dataRetrievalService.getDefaultActivitySummaryDateRange();
    }

    @GetMapping("/clan-list")
    @Operation(summary = "Get a list of all clans")
    public List<Clan> getClanList() {
        return dataRetrievalService.getClanList();
    }

    @GetMapping("/clan-activity/{clanId}")
    @Operation(summary = "Get activity data for a specific clan. If no date range is provided, the default range will be used.")
    public ActivityInfo getClanActivityInfo(
        @PathVariable @NotNull Long clanId,
        @RequestParam(required = false) LocalDateTime startDate,
        @RequestParam(required = false) LocalDateTime endDate
    ) {
        return dataRetrievalService.getClanActivityData(clanId, startDate, endDate);
    }
}
