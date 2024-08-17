package com.th3hero.clantracker.api.ui;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "Rank",
    description = "The rank of a clan member."
)
public enum Rank {
    COMMANDER,
    EXECUTIVE_OFFICER,
    COMBAT_OFFICER,
    PERSONNEL_OFFICER,
    INTELLIGENCE_OFFICER,
    QUARTERMASTER,
    RECRUITMENT_OFFICER,
    JUNIOR_OFFICER,
    PRIVATE,
    RECRUIT,
    RESERVIST
}