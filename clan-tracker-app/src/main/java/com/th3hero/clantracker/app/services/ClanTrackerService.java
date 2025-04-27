package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.app.exceptions.ClanNotFoundException;
import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.app.utils.DateUtils;
import com.th3hero.clantracker.app.utils.EntityFactory;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan.BasicPlayer;
import com.th3hero.clantracker.app.wargaming.ClanSearch.BasicClan;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.member.MemberRepository;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityJpa;
import com.th3hero.clantracker.jpa.player.activity.PlayerActivityRepository;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotJpa;
import com.th3hero.clantracker.jpa.player.snapshot.PlayerSnapshotRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final PlayerRepository playerRepository;
    private final PlayerActivityRepository playerActivityRepository;
    private final PlayerSnapshotRepository playerSnapshotRepository;

    /**
     * Add a clan for tracking.
     *
     * @param clanTag the clan tag to add for tracking.
     */
    public void addClanForTracking(String clanTag) {
        BasicClan clan = basicClanValidator(clanTag, apiService.clanSearch(clanTag));
        fetchMemberDetails(clan.id());
        schedulingService.scheduleMemberActivityFetchJob(clan.id());
    }

    /**
     * Remove a clan from tracking.
     *
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

    public void fetchMemberDetails(Long clanId) {
        fetchMemberDetails(clanId, LocalDate.now(), LocalTime.of(LocalTime.now().getHour(), 0));
    }

    /**
     * Fetch clan members data for a specific clan.
     *
     * @param clanId the id of the clan to fetch members for.
     */
    public void fetchMemberDetails(Long clanId, LocalDate effectiveDate, LocalTime effectiveTime) {
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
        Map<Long, BasicPlayer> basicPlayerMap = clanDetails.members().stream()
            .collect(Collectors.toMap(BasicPlayer::id, Function.identity()));

        ClanJpa clan = clanRepository.findById(clanId)
            .orElse(
                clanRepository.save(
                    ClanJpa.create(clanDetails.clanId(), clanDetails.tag())
                )
            );

        // Create and save all the members
        List<MemberJpa> memberJpas = createMembers(clanDetails, enrichedPlayerMap, clan);
        memberJpas = memberRepository.saveAll(memberJpas);

        // Create and save all the player activity of the members
        List<PlayerActivityJpa> playerActivityJpas = createPlayerActivityJpas(effectiveDate, effectiveTime, memberJpas, enrichedPlayerMap);
        playerActivityRepository.saveAll(playerActivityJpas);

        // Create and save all the player snapshots of the members
        List<PlayerSnapshotJpa> playerSnapshotJpas = createPlayerSnapshotJpas(effectiveDate, effectiveTime, clan, memberJpas, basicPlayerMap, enrichedPlayerMap);
        playerSnapshotRepository.saveAll(playerSnapshotJpas);

        clan.getMembers().addAll(memberJpas);
        clan = clanRepository.save(clan);

        // Remove any members that are no longer in the clan
        List<MemberJpa> membersNoLongInClan = clan.getMembers().stream()
            .filter(member -> !memberIds.contains(member.getPlayerJpa().getId()))
            .toList();
        if (!membersNoLongInClan.isEmpty()) {
            clan.getMembers().removeAll(membersNoLongInClan);
            memberRepository.deleteAll(membersNoLongInClan);
            clanRepository.save(clan);
            log.debug("Removed {} members that are no longer in the clan {}", membersNoLongInClan.size(), clanId);
        }
    }

    private List<MemberJpa> createMembers(EnrichedClan clanDetails, Map<Long, EnrichedPlayer> enrichedPlayerMap, ClanJpa clan) {
        List<PlayerJpa> playerJpas = clanDetails.members().stream()
            .filter(basicPlayer -> enrichedPlayerMap.containsKey(basicPlayer.id()))
            .map(basicPlayer -> {
                EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(basicPlayer.id());
                return PlayerJpa.create(enrichedPlayer.accountId(), enrichedPlayer.nickname());
            })
            .toList();

        // Players needs to be saved before members as they are a PK in the MemberJpa entity.
        // This could be done with less code but this way we can batch save the players rather than making up to 100 saves.
        List<PlayerJpa> savedPlayers = playerRepository.saveAll(playerJpas);

        Map<Long, PlayerJpa> savedPlayerJpaMap = savedPlayers.stream()
            .collect(Collectors.toMap(PlayerJpa::getId, Function.identity()));

        return clanDetails.members().stream()
            .filter(basicPlayer -> enrichedPlayerMap.containsKey(basicPlayer.id()))
            .map(basicPlayer -> {
                EnrichedPlayer enrichedPlayer = enrichedPlayerMap.get(basicPlayer.id());
                Rank rank = EnumUtils.getEnumIgnoreCase(Rank.class, basicPlayer.role());
                if (rank == null) {
                    throw new InvalidWargamingResponseException("Unknown rank %s".formatted(basicPlayer.role()));
                }
                PlayerJpa playerJpa = savedPlayerJpaMap.get(enrichedPlayer.accountId());

                return MemberJpa.create(
                    playerJpa,
                    clan,
                    rank,
                    DateUtils.fromTimestamp(basicPlayer.joinedAt()),
                    DateUtils.fromTimestamp(enrichedPlayer.updatedAt())
                );
            })
            .toList();
    }

    private static List<PlayerActivityJpa> createPlayerActivityJpas(LocalDate effectiveDate, LocalTime effectiveTime, List<MemberJpa> members, Map<Long, EnrichedPlayer> enrichedPlayerMap) {
        final LocalDateTime fetchDateTime = LocalDateTime.now();
        return members.stream()
            .map(member -> EntityFactory.createPlayerActivityJpa(member, enrichedPlayerMap, fetchDateTime, effectiveDate, effectiveTime))
            .toList();
    }

    private static List<PlayerSnapshotJpa> createPlayerSnapshotJpas(LocalDate effectiveDate, LocalTime effectiveTime, ClanJpa clanJpa, List<MemberJpa> members, Map<Long, BasicPlayer> basicPlayerMap, Map<Long, EnrichedPlayer> enrichedPlayerMap) {
        final LocalDateTime fetchDateTime = LocalDateTime.now();
        return members.stream()
            .map(member -> EntityFactory.createPlayerSnapshotJpa(clanJpa, member, basicPlayerMap, enrichedPlayerMap, fetchDateTime, effectiveDate, effectiveTime))
            .toList();
    }

    /**
     * Get the validated clan info for a specific clan.
     *
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
     *
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
     *
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
     *
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
     *
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
