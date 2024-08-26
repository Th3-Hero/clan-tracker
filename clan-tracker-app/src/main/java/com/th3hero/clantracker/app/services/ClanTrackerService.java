package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.jpa.entities.ClanJpa;
import com.th3hero.clantracker.jpa.entities.MemberActivityJpa;
import com.th3hero.clantracker.jpa.entities.MemberJpa;
import com.th3hero.clantracker.jpa.repositories.ClanRepository;
import com.th3hero.clantracker.jpa.repositories.MemberActivityRepository;
import com.th3hero.clantracker.jpa.repositories.MemberRepository;
import com.th3hero.clantracker.app.utils.DateUtils;
import com.th3hero.clantracker.app.utils.Utils;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan.BasicPlayer;
import com.th3hero.clantracker.app.wargaming.ClanSearch;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClanTrackerService {

    private final ApiService apiService;
    private final SchedulingService schedulingService;
    private final ClanRepository clanRepository;
    private final MemberRepository memberRepository;
    private final MemberActivityRepository memberActivityRepository;

    /**
     * Add a clan for tracking.
     * @param clanTag the clan tag to add for tracking.
     */
    public void addClanForTracking(String clanTag) {
        Optional<ClanSearch.BasicClan> clan = apiService.clanSearch(clanTag);
        if (clan.isEmpty()) {
            log.debug("No clan with tag {} was found", clanTag);
            throw new ClanNotFoundException("No clan with tag %s was found".formatted(clanTag));
        }
        fetchClanMembers(clan.get().id());
        schedulingService.scheduleMemberActivityFetchJob(clan.get().id());
    }

    /**
     * Remove a clan from tracking.
     * @param clanTag the clan tag to remove from tracking.
     */
    public void removeClanFromTracking(String clanTag) {
        Optional<ClanJpa> clan = clanRepository.findByTag(clanTag);
        if (clan.isEmpty()) {
            log.debug("No clan with tag {} is being tracked.", clanTag);
            throw new ClanNotFoundException("No clan with tag %s is being tracked.".formatted(clanTag));
        }
        schedulingService.removeMemberActivityFetchJob(clan.get().getId());
        memberRepository.deleteAll(clan.get().getMembers());
        clanRepository.delete(clan.get());
    }

    /**
     * Fetch clan members data for a specific clan.
     * @param clanId the id of the clan to fetch members for.
     */
    public void fetchClanMembers(Long clanId) {
        // get clan info and basic member info
        Optional<EnrichedClan> clanDetails = apiService.clanDetails(clanId);
        if (clanDetails.isEmpty()) {
            log.info("Clan with id {} not found", clanId);
            throw new ClanNotFoundException("Clan with id %s not found".formatted(clanId));
        }
        List<Long> memberIds = clanDetails.get().members().stream()
            .map(BasicPlayer::id)
            .toList();
        // get detailed member info for the members in the clan
        List<EnrichedPlayer> members = apiService.memberDetails(memberIds);
        if (members.size() != memberIds.size()) {
            log.error("Failed to fetch all members for clan {}", clanId);
            throw new InvalidWargamingResponseException("Failed to fetch all members for clan %s".formatted(clanId));
        }

        Map<Long, EnrichedPlayer> enrichedPlayerMap = members.stream()
            .collect(Collectors.toMap(EnrichedPlayer::accountId, Function.identity()));

        ClanJpa clan = clanRepository.save(
            ClanJpa.create(
                clanDetails.get().clanId(),
                clanDetails.get().tag()
            )
        );

        List<MemberJpa> memberJpas = createMembers(clanDetails, enrichedPlayerMap, clan);

        memberRepository.saveAll(memberJpas);

        List<MemberActivityJpa> memberActivityJpas = createMemberActivityJpas(clanDetails, enrichedPlayerMap, clan);

        memberActivityRepository.saveAll(memberActivityJpas);

        clan.getMembers().addAll(memberJpas);
        clanRepository.save(clan);
    }

    public void importExistingClanActivity(MultipartFile file, Long clanId) {
        try {
            InputStream inputStream = file.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            List<MemberActivityJpa> memberData = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                List<String> data = List.of(line.split(","));
                // dateTime, playerId, playerName, rank, joinedClanDate, lastBattleDateTime, randoms, skirms, advances, cwTotal
                if (data.size() != 10) {
                    log.error("Invalid data: {}", data);
                    return;
                }
                LocalDateTime updatedAt = DateUtils.fromDateTime(data.getFirst());
                Long playerId = Long.parseLong(data.get(1));
                String playerName = data.get(2);
                Rank rank = EnumUtils.getEnumIgnoreCase(Rank.class, Utils.importRankTransform(data.get(3)));
                LocalDateTime joinedClan = DateUtils.fromDateString(data.get(4));
                LocalDateTime lastBattle = DateUtils.fromDateTime(data.get(5));
                long randoms = 0L;
                if (!data.get(6).isBlank()) {
                    randoms = Long.parseLong(data.get(6));
                }
                Long skirms = Long.parseLong(data.get(7));
                Long advances = Long.parseLong(data.get(8));
                Long cwTotal = Long.parseLong(data.get(9));
                MemberActivityJpa memberActivityJpa = MemberActivityJpa.create(
                    playerId,
                    updatedAt,
                    playerName,
                    rank,
                    clanId,
                    joinedClan,
                    lastBattle,
                    randoms,
                    skirms,
                    advances,
                    cwTotal
                );
                memberData.add(memberActivityJpa);
            }

            memberActivityRepository.saveAll(memberData);
        } catch (IOException e) {
            log.error("Failed to read file", e);
        }
    }

    private static List<MemberJpa> createMembers(Optional<EnrichedClan> clanDetails, Map<Long, EnrichedPlayer> enrichedPlayerMap, ClanJpa clan) {
        if (clanDetails.isEmpty()) {
            throw new ClanNotFoundException("Clan not found");
        }
        return clanDetails.get().members().stream()
            .map(basicPlayer -> {
                // merge the player info from the basic and enriched player info and create a MemberJpa object from it.
                EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(basicPlayer.id());
                Rank rank = EnumUtils.getEnumIgnoreCase(Rank.class, basicPlayer.role());
                if (rank == null) {
                    throw new InvalidWargamingResponseException("Unknown rank %s".formatted(basicPlayer.role()));
                }
                return MemberJpa.create(
                    enrichedPlayer.accountId(),
                    enrichedPlayer.nickname(),
                    clan,
                    rank,
                    DateUtils.fromTimestamp(basicPlayer.joinedAt()),
                    DateUtils.fromTimestamp(enrichedPlayer.updatedAt())
                );
            })
            .toList();
    }

    private static List<MemberActivityJpa> createMemberActivityJpas(Optional<EnrichedClan> clanDetails, Map<Long, EnrichedPlayer> enrichedPlayerMap, ClanJpa clan) {
        final LocalDateTime fetchDateTime = LocalDateTime.now();
        if (clanDetails.isEmpty()) {
            throw new ClanNotFoundException("Clan not found");
        }
        return clanDetails.get().members().stream()
            .map(basicPlayer -> {
                // merge the player info from the basic and enriched player info and create a MemberActivityJpa object from it.
                EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(basicPlayer.id());
                Long clanWarAbsoluteBattles = enrichedPlayer.statistics().get("globalmap_absolute").battles();
                Long clanWarMiddleBattles = enrichedPlayer.statistics().get("globalmap_middle").battles();
                Long clanWarChampionBattles = enrichedPlayer.statistics().get("globalmap_champion").battles();
                Long clanWarTotalBattles = clanWarAbsoluteBattles + clanWarMiddleBattles + clanWarChampionBattles;
                return MemberActivityJpa.create(
                    enrichedPlayer.accountId(),
                    fetchDateTime,
                    enrichedPlayer.nickname(),
                    EnumUtils.getEnumIgnoreCase(Rank.class, basicPlayer.role()),
                    clan.getId(),
                    DateUtils.fromTimestamp(basicPlayer.joinedAt()),
                    DateUtils.fromTimestamp(enrichedPlayer.lastBattleTime()),
                    enrichedPlayer.statistics().get("random").battles(),
                    enrichedPlayer.statistics().get("stronghold_skirmish").battles(),
                    enrichedPlayer.statistics().get("stronghold_defense").battles(),
                    clanWarTotalBattles
                );
            })
            .toList();
    }

}
