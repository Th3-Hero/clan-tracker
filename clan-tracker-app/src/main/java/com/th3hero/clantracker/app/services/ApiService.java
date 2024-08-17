package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.app.wargaming.ClanInfo;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan;
import com.th3hero.clantracker.app.wargaming.ClanSearch;
import com.th3hero.clantracker.app.wargaming.ClanSearch.BasicClan;
import com.th3hero.clantracker.app.wargaming.MemberInfo;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {

    @Value("${wargaming.api-token}")
    private String apiToken;

    private final RestClient restClient;

    private static final String DEFAULT_ERROR_MESSAGE = "Invalid response from Wargaming API";

    private <T> T callApi(String requestString, Class<T> responseType) {
        String apiBaseUrl = "https://api.worldoftanks.com";
        String uri = apiBaseUrl + "/wot" + requestString;
        final var response = restClient.get()
            .uri(uri)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                (clientRequest, clientResponse) -> {
                    throw new InvalidWargamingResponseException(DEFAULT_ERROR_MESSAGE);
                })
            .toEntity(responseType);
        final var responseBody = response.getBody();
        if (responseBody == null) {
            throw new InvalidWargamingResponseException(DEFAULT_ERROR_MESSAGE);
        }
        return responseBody;
    }

    public Optional<BasicClan> clanSearch(String clanTag) {
        String requestString = "/clans/list/?application_id=%s&search=%s&fields=%s".formatted(
            apiToken,
            clanTag,
            clanSearchFields()
        );
        final var response = callApi(requestString, ClanSearch.class);
        if (!"ok".equals(response.status())) {
            log.info(response.status());
            throw new InvalidWargamingResponseException("%s".formatted(response.error()));
        }
        return response.data().stream()
            .filter(clan -> clan.tag().equals(clanTag))
            .findFirst();
    }

    public Optional<EnrichedClan> clanDetails(Long clanId) {
        String requestString = "/clans/info/?application_id=%s&clan_id=%s&fields=%s".formatted(
            apiToken,
            clanId,
            clanDetailsFields()
        );
        final var response = callApi(requestString, ClanInfo.class);
        if (!"ok".equals(response.status())) {
            throw new InvalidWargamingResponseException("%s".formatted(response.error()));
        }
        return response.data().values().stream()
            .filter(clan -> clan.clanId().equals(clanId))
            .findFirst();
    }

    public List<EnrichedPlayer> memberDetails(List<Long> memberIds) {
        String requestString = "/account/info/?application_id=%s&account_id=%s&extra=%s&fields=%s".formatted(
            apiToken,
            buildIdString(memberIds),
            memberDetailsExtra(),
            memberDetailsFields()
        );
        final var response = callApi(requestString, MemberInfo.class);
        if (!"ok".equals(response.status())) {
            throw new InvalidWargamingResponseException("%s".formatted(response.error()));
        }
        return response.data().values().stream().toList();
    }

    private String buildIdString(List<Long> ids) {
        return ids.stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
    }

    private String clanSearchFields() {
        return String.join(",", List.of(
            "clan_id",
            "tag"
        ));
    }

    private String clanDetailsFields() {
        return String.join(",", List.of(
            "clan_id",
            "tag",
            "members.account_id",
            "members.joined_at",
            "members.role"
        ));
    }

    private String memberDetailsFields() {
        return String.join(",", List.of(
            "updated_at",
            "account_id",
            "clan_id",
            "nickname",
            "last_battle_time",
            "statistics.random.battles",
            "statistics.stronghold_skirmish.battles",
            "statistics.stronghold_defense.battles",
            "statistics.globalmap_absolute.battles",
            "statistics.globalmap_champion.battles",
            "statistics.globalmap_middle.battles"
        ));
    }

    private String memberDetailsExtra() {
        return String.join(",", List.of(
            "statistics.globalmap_absolute",
            "statistics.globalmap_champion",
            "statistics.globalmap_middle",
            "statistics.random"
        ));
    }
}
