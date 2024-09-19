package com.th3hero.clantracker.app.listeners;

import com.th3hero.clantracker.app.listeners.events.MemberActivityUpdateIntervalChangedEvent;
import com.th3hero.clantracker.app.services.SchedulingService;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberActivityUpdateIntervalListener {
    private final ClanRepository clanRepository;
    private final SchedulingService schedulingService;


    @EventListener
    public void updateMemberActivityIntervalListener(MemberActivityUpdateIntervalChangedEvent event) {
        List<ClanJpa> clans = clanRepository.findAll();
        for (ClanJpa clan : clans) {
            schedulingService.updateMemberActivityFetchJobInterval(clan.getId());
        }
    }
}
