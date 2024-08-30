package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.app.wargaming.ClanInfo;
import com.th3hero.clantracker.app.wargaming.ClanSearch.BasicClan;
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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClanTrackerService {

    private final Validator validator;

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
        BasicClan clan = basicClanValidator(clanTag, apiService.clanSearch(clanTag));
        fetchClanMembers(clan.id());
        schedulingService.scheduleMemberActivityFetchJob(clan.id());
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
        EnrichedClan clanDetails = enrichedClanValidator(clanId, apiService.clanDetails(clanId));
        List<Long> memberIds = clanDetails.members().stream()
            .map(BasicPlayer::id)
            .toList();
        // get detailed member info for the members in the clan
        List<EnrichedPlayer> members = enrichedPlayerValidator(apiService.memberDetails(memberIds));
        if (members.size() != memberIds.size()) {
            log.error("Failed to fetch all members for clan {}", clanId);
        }

        Map<Long, EnrichedPlayer> enrichedPlayerMap = members.stream()
            .collect(Collectors.toMap(EnrichedPlayer::accountId, Function.identity()));

        ClanJpa clan = clanRepository.save(
            ClanJpa.create(
                clanDetails.clanId(),
                clanDetails.tag()
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

    private static List<MemberJpa> createMembers(EnrichedClan clanDetails, Map<Long, EnrichedPlayer> enrichedPlayerMap, ClanJpa clan) {
        return clanDetails.members().stream()
            .filter(basicPlayer -> enrichedPlayerMap.containsKey(basicPlayer.id()))
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

    private static List<MemberActivityJpa> createMemberActivityJpas(EnrichedClan clanDetails, Map<Long, EnrichedPlayer> enrichedPlayerMap, ClanJpa clan) {
        final LocalDateTime fetchDateTime = LocalDateTime.now();
        return clanDetails.members().stream()
            .filter(basicPlayer -> enrichedPlayerMap.containsKey(basicPlayer.id()))
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

    /**
     * Get the validated clan info for a specific clan.
     * @param clanTag the clan tag to search for.
     * @param clan the clan info to validate.
     * @return the validated clan info.
     * @throws ClanNotFoundException if the clan is not found.
     * @throws InvalidWargamingResponseException if the clan info is invalid.
     */
    private BasicClan basicClanValidator(String clanTag, Optional<BasicClan> clan) {
        if (clan.isEmpty()) {
            throw new ClanNotFoundException("Failed to find clan with tag %s".formatted(clanTag));
        }

        Set<ConstraintViolation<BasicClan>> violations = validator.validate(clan.get());
        if (!violations.isEmpty()) {
            throw new InvalidWargamingResponseException(validationErrorMessage(violations));
        }
        return clan.get();
    }

    /**
     * Get the validated enriched clan info for a specific clan.
     * Any invalid members will not be included in the returned clan info.
     * @param clanId the id of the clan to validate.
     * @param clan the enriched clan info to validate.
     * @return the validated enriched clan info.
     * @throws ClanNotFoundException if the clan is not found.
     * @throws InvalidWargamingResponseException if the clan info is invalid.
     */
    private EnrichedClan enrichedClanValidator(Long clanId, Optional<EnrichedClan> clan) {
        if (clan.isEmpty()) {
            throw new ClanNotFoundException("Failed to find clan with id %s".formatted(clanId));
        }

        List<BasicPlayer> members = clan.get().members();
        List<BasicPlayer> validatedMembers = new ArrayList<>();
        for (BasicPlayer member : members) {
            Set<ConstraintViolation<BasicPlayer>> memberViolations = validator.validate(member);
            if (!memberViolations.isEmpty()) {
                log.error("Member violation: {}", validationErrorMessage(memberViolations));
            }
            validatedMembers.add(member);
        }
        EnrichedClan enrichedClan = new EnrichedClan(clan.get().clanId(), clan.get().tag(), validatedMembers);
        Set<ConstraintViolation<EnrichedClan>> violations = validator.validate(enrichedClan);
        if (!violations.isEmpty()) {
            throw new InvalidWargamingResponseException(validationErrorMessage(violations));
        }

        return clan.get();
    }

    /**
     * Get a list of validated members. Any invalid members will not be included in the returned list.
     * @param members the members to validate.
     * @return the validated members.
     */
    private List<EnrichedPlayer> enrichedPlayerValidator(List<EnrichedPlayer> members) {
        List<EnrichedPlayer> validatedMembers = new ArrayList<>();
        for (EnrichedPlayer member : members) {
            Set<ConstraintViolation<EnrichedPlayer>> memberViolations = validator.validate(member);
            if (!memberViolations.isEmpty()) {
                log.error("Member violation: {}", validationErrorMessage(memberViolations));
            }
            validatedMembers.add(member);
        }
        return validatedMembers;
    }

    /**
     * Get the validated enriched clan info for a specific clan.
     * An exception will be thrown if ALL members of the clan cannot be validated.
     * @param clanId the id of the clan to validate.
     * @param clan the enriched clan info to validate.
     * @return the validated enriched clan info.
     * @throws ClanNotFoundException if the clan is not found.
     * @throws InvalidWargamingResponseException if the clan info is invalid.
     */
    private EnrichedClan enrichedClanValidatorStrict(Long clanId, Optional<EnrichedClan> clan) {
        if (clan.isEmpty()) {
            throw new ClanNotFoundException("Failed to find clan with id %s".formatted(clanId));
        }

        Set<ConstraintViolation<EnrichedClan>> violations = validator.validate(clan.get());
        if (!violations.isEmpty()) {
            throw new InvalidWargamingResponseException(validationErrorMessage(violations));
        }

        return clan.get();
    }

    /**
     * Get a list of validated members. An exception will be thrown if ANY of members cannot be validated.
     * @param members the members to validate.
     * @return the validated members.
     */
    private List<EnrichedPlayer> enrichedPlayerValidatorStrict(List<EnrichedPlayer> members) {
        var violations = validator.validate(members);
        if (!violations.isEmpty()) {
            throw new InvalidWargamingResponseException(validationErrorMessage(violations));
        }
        return members;
    }

    private <T> String validationErrorMessage(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
    }
}
