package com.th3hero.clantracker.app.services;

import com.th3hero.clantracker.app.exceptions.InvalidWargamingResponseException;
import com.th3hero.clantracker.app.wargaming.*;
import com.th3hero.clantracker.app.wargaming.ClanInfo.EnrichedClan;
import com.th3hero.clantracker.app.wargaming.ClanSearch.BasicClan;
import com.th3hero.clantracker.app.wargaming.MemberInfo.EnrichedPlayer;
import com.th3hero.clantracker.app.wargaming.PlayerInfo.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {

    @Value("${wargaming.api-token}")
    private String apiToken;

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    private static final String DEFAULT_ERROR_MESSAGE = "Invalid response from Wargaming API";
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * Calls the Wargaming API with the given request string and returns the response as the given response type
     *
     * @param requestString The request string to be appended to the base URL
     * @param responseType The class of the response type
     * @param <T> The type of the response
     * @return The response from the Wargaming API
     */
    private <T extends WargamingResponse> T callApi(String requestString, Class<T> responseType) {
        return retryTemplate.execute(context -> {
            String apiBaseUrl = "https://api.worldoftanks.com";
            String uri = apiBaseUrl + "/wot" + requestString;
            final var response = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    HttpStatusCode::isError,
                    (clientRequest, clientResponse) -> {
                        log.warn("Retrying request due to error status code. URI: {} Status: {}", uri, clientResponse.getStatusCode());
                        throw new InvalidWargamingResponseException(DEFAULT_ERROR_MESSAGE);
                    })
                .toEntity(responseType);
            final var responseBody = response.getBody();
            if (responseBody == null) {
                log.warn("Retrying. Null response body from Wargaming API. URI: {}", uri);
                throw new InvalidWargamingResponseException(DEFAULT_ERROR_MESSAGE);
            }
            if (!"ok".equals(responseBody.status())) {
                log.warn("Retrying request due to error. URI: {} Error: {}", uri, responseBody.error());
                throw new InvalidWargamingResponseException("Invalid response from Wargaming API: %s".formatted(responseBody.error()));
            }
            return responseBody;
        });
    }

    /**
     * Searches the wargaming api for a clan with the given tag
     *
     * @param clanTag The tag of the clan to search for
     * @return An {@link Optional} containing the clan if it was found, or an empty {@link Optional} if it was not found
     * @throws InvalidWargamingResponseException If the response from the Wargaming API is invalid.
     */
    public Optional<BasicClan> clanSearch(String clanTag) {
        String requestString = "/clans/list/?application_id=%s&search=%s&fields=%s".formatted(
            apiToken,
            clanTag,
            clanSearchFields()
        );

        final var response = callApi(requestString, ClanSearch.class);
        return response.data().stream()
            .filter(clan -> clan.tag().equals(clanTag))
            .findFirst();
    }

    /**
     * Gets basic clan details and basic clan info.
     *
     * @param clanId The id of the clan to get details for.
     * @return An {@link Optional} containing the clan if it was found, or an empty {@link Optional} if it was not found.
     */
    public Optional<EnrichedClan> clanDetails(Long clanId) {
        String requestString = "/clans/info/?application_id=%s&clan_id=%s&fields=%s".formatted(
            apiToken,
            clanId,
            clanDetailsFields()
        );
        final var response = callApi(requestString, ClanInfo.class);
        return response.data().values().stream()
            .filter(clan -> clan.clanId().equals(clanId))
            .findFirst();
    }

    /**
     * Gets detailed information about the members of a clan. Auto splits the request into batches of 100 members.
     *
     * @param memberIds The ids of the members to get details for.
     * @return A list of {@link EnrichedPlayer} objects containing the details of the members.
     */
    public List<EnrichedPlayer> memberDetails(List<Long> memberIds) {
        List<EnrichedPlayer> allPlayers = new ArrayList<>();
        for (int i = 0; i < memberIds.size(); i += MAX_BATCH_SIZE) {
            List<Long> batch = memberIds.subList(i, Math.min(i + MAX_BATCH_SIZE, memberIds.size()));
            String requestString = "/account/info/?application_id=%s&account_id=%s&extra=%s&fields=%s".formatted(
                apiToken,
                buildIdString(batch),
                memberDetailsExtra(),
                memberDetailsFields()
            );
            final var response = callApi(requestString, MemberInfo.class);
            allPlayers.addAll(response.data().values().stream().toList());
        }
        return allPlayers;
    }

    public Map<Long, Player> playerInfo(List<Long> playerIds) {
        Map<Long, Player> allPlayers = new HashMap<>();
        for (int i = 0; i < playerIds.size(); i += MAX_BATCH_SIZE) {
            List<Long> batch = playerIds.subList(i, Math.min(i + MAX_BATCH_SIZE, playerIds.size()));
            String requestString = "/account/info/?application_id=%s&account_id=%s&fields=account_id,nickname".formatted(
                apiToken,
                buildIdString(batch)
            );
            final var response = callApi(requestString, PlayerInfo.class);
            allPlayers.putAll(response.data().values().stream().collect(Collectors.toMap(Player::accountId, player -> player)));
        }
        return allPlayers;
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
