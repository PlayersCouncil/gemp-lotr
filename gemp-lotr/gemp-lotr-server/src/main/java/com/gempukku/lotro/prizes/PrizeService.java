package com.gempukku.lotro.prizes;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.BlueprintUtils;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.*;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueParams;
import com.gempukku.lotro.packs.ProductLibrary;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Awards the configurable prize tiers of an event ({@link PrizeTier}) on top of the automatic prizes, and manages
 * the <em>promised</em> prizes: a promise is handed out as a placeholder card in set {@link #PLACEHOLDER_SET}
 * ({@code 404_<placeholderId>}) and later {@link #resolve resolved} to a real card, at which point every holder's
 * placeholder is swapped for the real thing.
 * <p>
 * Everything awarded goes through {@link CollectionsManager#addItemsToPlayerCollection} so it shows up as a new
 * delivery, and is logged in {@code prize_award}, which is also what stops the same (event, tier, player) from ever
 * being awarded twice.
 */
public class PrizeService {
    private static final Logger logger = LogManager.getLogger(PrizeService.class);

    public static final int PLACEHOLDER_SET = 404;
    public static final int MAX_LABEL_LENGTH = 255;
    /** Leagues that ended more than this many days ago no longer count towards a campaign. */
    public static final int CAMPAIGN_LOOKBACK_DAYS = 180;

    public static final String KIND_LEAGUE = "league";
    public static final String KIND_TOURNAMENT = "tournament";
    public static final String KIND_CAMPAIGN = "campaign";
    public static final String KIND_MANUAL = "manual";

    /**
     * The event whose tiers are being awarded.
     * @param kind     {@link #KIND_LEAGUE} or {@link #KIND_TOURNAMENT}
     * @param id       league code / tournament id: the dedup key together with the tier index
     * @param name     shown to players in the delivery reason
     * @param campaign campaign tag the event belongs to, or null
     */
    public record EventRef(String kind, String id, String name, String campaign) {
        public static EventRef forLeague(League league, LeagueParams params) {
            return new EventRef(KIND_LEAGUE, league.getCodeStr(), league.getName(), params == null ? null : params.campaign);
        }
    }

    /**
     * What {@link #resolve} did: which players had their placeholder swapped and how many cards that was in total.
     */
    public record ResolutionResult(int placeholderId, String label, String blueprintId, List<String> players, int cardsSwapped) {
        public int playerCount() {
            return players.size();
        }
    }

    private final CollectionsManager _collectionsManager;
    private final PlayerDAO _playerDAO;
    private final CollectionDAO _collectionDAO;
    private final PrizePlaceholderDAO _placeholderDAO;
    private final PrizeAwardDAO _awardDAO;
    private final LeagueDAO _leagueDAO;
    private final LeagueMatchDAO _leagueMatchDAO;
    private final LotroCardBlueprintLibrary _library;
    private final ProductLibrary _productLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;

    public PrizeService(CollectionsManager collectionsManager, PlayerDAO playerDAO, CollectionDAO collectionDAO,
                        PrizePlaceholderDAO placeholderDAO, PrizeAwardDAO awardDAO,
                        LeagueDAO leagueDAO, LeagueMatchDAO leagueMatchDAO,
                        LotroCardBlueprintLibrary library, ProductLibrary productLibrary,
                        LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions) {
        _collectionsManager = collectionsManager;
        _playerDAO = playerDAO;
        _collectionDAO = collectionDAO;
        _placeholderDAO = placeholderDAO;
        _awardDAO = awardDAO;
        _leagueDAO = leagueDAO;
        _leagueMatchDAO = leagueMatchDAO;
        _library = library;
        _productLibrary = productLibrary;
        _formatLibrary = formatLibrary;
        _soloDraftDefinitions = soloDraftDefinitions;

        try {
            registerPlaceholdersWithLibrary();
        } catch (RuntimeException exp) {
            // Unregistered placeholders still render (as a plain "Future Prize"), so a database hiccup at startup
            // must not take the server down.
            logger.error("Unable to register outstanding prize placeholders with the card library", exp);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Placeholder ids
    // ------------------------------------------------------------------------------------------------

    /**
     * @return the blueprint id handed out for a placeholder row: {@code 404_<id>}
     */
    public static String placeholderBlueprint(int id) {
        return PLACEHOLDER_SET + "_" + id;
    }

    public static boolean isPlaceholder(String blueprintId) {
        return LotroCardBlueprintLibrary.isPlaceholderId(blueprintId);
    }

    /**
     * @return the placeholder row id encoded in a {@code 404_<id>} blueprint, or -1 if it is not one
     */
    public static int placeholderId(String blueprintId) {
        if (!isPlaceholder(blueprintId))
            return -1;
        String stripped = BlueprintUtils.stripModifiers(blueprintId);
        return Integer.parseInt(stripped.substring(stripped.indexOf('_') + 1));
    }

    // ------------------------------------------------------------------------------------------------
    // Validation and description
    // ------------------------------------------------------------------------------------------------

    /**
     * Checks a tier definition: kinds, ranges and counts, that every concrete id resolves to a card in the library
     * or a pack in the product library (unknown ids are rejected outright), and that every promise has a label.
     * A null or empty list is valid (no extra prizes).
     * @throws PrizeDefinitionException naming the offending field, e.g. {@code prizeTiers[1].items[0].blueprintId}
     */
    public static void validateTiers(List<PrizeTier> tiers, LotroCardBlueprintLibrary library, ProductLibrary products) throws PrizeDefinitionException {
        if (tiers == null)
            return;
        for (int i = 0; i < tiers.size(); i++) {
            String field = "prizeTiers[" + i + "]";
            PrizeTier tier = tiers.get(i);
            if (tier == null)
                throw new PrizeDefinitionException(field, "Prize tier " + (i + 1) + " is missing.");
            if (tier.kind == null)
                throw new PrizeDefinitionException(field + ".kind", "Prize tier " + (i + 1) + " must be PLACEMENT or PARTICIPATION.");

            switch (tier.kind) {
                case PLACEMENT -> {
                    if (tier.from < 1)
                        throw new PrizeDefinitionException(field + ".from", "Prize tier " + (i + 1) + ": the first place must be 1 or more.");
                    if (tier.to < tier.from)
                        throw new PrizeDefinitionException(field + ".to", "Prize tier " + (i + 1) + ": the last place must not be before the first place.");
                }
                case PARTICIPATION -> {
                    if (tier.games < 0)
                        throw new PrizeDefinitionException(field + ".games", "Prize tier " + (i + 1) + ": the number of games cannot be negative.");
                    if (tier.scope == null)
                        throw new PrizeDefinitionException(field + ".scope", "Prize tier " + (i + 1) + " must count games in the EVENT or across the CAMPAIGN.");
                }
            }

            if (tier.label != null && tier.label.length() > MAX_LABEL_LENGTH)
                throw new PrizeDefinitionException(field + ".label", "Prize tier " + (i + 1) + ": the label must be " + MAX_LABEL_LENGTH + " characters or less.");

            if (tier.items == null || tier.items.isEmpty())
                throw new PrizeDefinitionException(field + ".items", "Prize tier " + (i + 1) + " awards nothing; add at least one item.");

            for (int j = 0; j < tier.items.size(); j++) {
                String itemField = field + ".items[" + j + "]";
                PrizeItem item = tier.items.get(j);
                if (item == null)
                    throw new PrizeDefinitionException(itemField, "Prize tier " + (i + 1) + ", item " + (j + 1) + " is missing.");
                if (item.count < 1)
                    throw new PrizeDefinitionException(itemField + ".count", "Prize tier " + (i + 1) + ", item " + (j + 1) + ": the count must be at least 1.");

                boolean hasCard = StringUtils.isNotBlank(item.blueprintId);
                boolean hasPromise = StringUtils.isNotBlank(item.promise);
                if (hasCard == hasPromise)
                    throw new PrizeDefinitionException(itemField, "Prize tier " + (i + 1) + ", item " + (j + 1) + " must be either a card/pack or a promise, not both or neither.");

                if (hasPromise) {
                    if (item.promise.trim().length() > MAX_LABEL_LENGTH)
                        throw new PrizeDefinitionException(itemField + ".promise", "Prize tier " + (i + 1) + ", item " + (j + 1) + ": the promise must be " + MAX_LABEL_LENGTH + " characters or less.");
                } else {
                    String problem = describeUnknownProduct(item.blueprintId.trim(), library, products);
                    if (problem != null)
                        throw new PrizeDefinitionException(itemField + ".blueprintId", "Prize tier " + (i + 1) + ", item " + (j + 1) + ": " + problem);
                }
            }
        }
    }

    /**
     * @return null if {@code id} is a known card (with any modifiers) or a known pack, otherwise why it is not
     */
    private static String describeUnknownProduct(String id, LotroCardBlueprintLibrary library, ProductLibrary products) {
        if (isPlaceholder(id))
            return "'" + id + "' is a future-prize placeholder; define a promise instead.";
        if (id.contains("_")) {
            try {
                if (library.getLotroCardBlueprint(id) == null)
                    return "unknown card '" + id + "'.";
            } catch (CardNotFoundException exp) {
                return "unknown card '" + id + "'.";
            } catch (RuntimeException exp) {
                return "invalid card id '" + id + "'.";
            }
            return null;
        }
        if (products == null || products.GetProduct(id) == null)
            return "unknown pack '" + id + "'.";
        return null;
    }

    /**
     * @return one human-readable line per tier, for previews and logs
     */
    public List<String> describeTiers(List<PrizeTier> tiers) {
        List<String> result = new ArrayList<>();
        if (tiers == null)
            return result;
        for (PrizeTier tier : tiers)
            result.add(describeTier(tier));
        return result;
    }

    public String describeTier(PrizeTier tier) {
        if (tier == null)
            return "(missing tier)";
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(tier.label))
            sb.append(tier.label.trim()).append(" - ");
        if (tier.kind == PrizeTier.Kind.PARTICIPATION) {
            sb.append("Participation (").append(tier.games).append("+ games ");
            sb.append(tier.scope == PrizeTier.Scope.CAMPAIGN ? "across the campaign" : "in this event").append(")");
        } else if (tier.from == tier.to) {
            sb.append("Place ").append(tier.from);
        } else {
            sb.append("Places ").append(tier.from).append("-").append(tier.to);
        }
        sb.append(": ");
        if (tier.items == null || tier.items.isEmpty()) {
            sb.append("nothing");
        } else {
            boolean first = true;
            for (PrizeItem item : tier.items) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(describeItem(item));
            }
        }
        return sb.toString();
    }

    private String describeItem(PrizeItem item) {
        if (item == null)
            return "(missing item)";
        if (item.hasPromise())
            return item.count + "x \"" + item.promise.trim() + "\" (promised)";
        String id = item.blueprintId == null ? "" : item.blueprintId.trim();
        String name = null;
        if (id.contains("_")) {
            try {
                var bp = _library.getLotroCardBlueprint(id);
                if (bp != null)
                    name = bp.getFullName();
            } catch (Exception ignored) {
            }
        }
        if (name == null)
            return item.count + "x " + id;
        return item.count + "x " + name + " (" + id + ")";
    }

    // ------------------------------------------------------------------------------------------------
    // Awarding
    // ------------------------------------------------------------------------------------------------

    /**
     * Awards a league's {@link LeagueParams#prizeTiers}.  Called by the league service once the league has ended
     * (its status has gone from 0 to 1).  Never throws: a prize problem must not stop the league from closing.
     */
    public void awardLeagueTiers(League league, List<PlayerStanding> standings) {
        if (league == null)
            return;
        LeagueParams params;
        try {
            params = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getParameters();
        } catch (RuntimeException exp) {
            logger.error("Unable to read the definition of league " + league.getName() + " to award its prize tiers", exp);
            return;
        }
        if (params == null)
            return;
        try {
            awardTiers(EventRef.forLeague(league, params), params.prizeTiers, standings, null);
        } catch (RuntimeException exp) {
            logger.error("Unable to award the prize tiers of league " + league.getName(), exp);
        }
    }

    /**
     * Awards every tier of {@code tiers} to the qualifying players of {@code standings}.  Each award is delivered
     * to the player's "My cards" collection with a reason like {@code <event name> prize: <tier description>}
     * (or {@code <adminReason>: <tier description>} when one is given) and logged in {@code prize_award}; a tier a
     * player has already received for this event (or, for campaign tiers, for this campaign) is skipped.
     * <p>
     * Promises are handed out as placeholders: one {@code prize_placeholder} row per (event, tier, promise label),
     * reused when it already exists unresolved, registered with the card library so the placeholder shows the
     * promise as its title.
     */
    public void awardTiers(EventRef event, List<PrizeTier> tiers, List<PlayerStanding> standings, String adminReason) {
        if (event == null || tiers == null || tiers.isEmpty())
            return;
        List<PlayerStanding> sorted = sortedStandings(standings);

        Set<String> eventAwarded = awardedKeys(_awardDAO.getAwards(event.kind(), event.id()));
        Set<String> campaignAwarded = null;
        Map<String, Integer> campaignGames = null;

        for (int tierIndex = 0; tierIndex < tiers.size(); tierIndex++) {
            PrizeTier tier = tiers.get(tierIndex);
            if (tier == null || tier.items == null || tier.items.isEmpty())
                continue;

            boolean campaignTier = tier.kind == PrizeTier.Kind.PARTICIPATION && tier.scope == PrizeTier.Scope.CAMPAIGN;
            if (campaignTier && StringUtils.isBlank(event.campaign())) {
                logger.warn(event.name() + ": tier " + (tierIndex + 1) + " counts games across the campaign but the event has no campaign tag; counting this event only");
                campaignTier = false;
            }

            List<String> winners;
            if (campaignTier) {
                if (campaignGames == null)
                    campaignGames = countCampaignGames(event, sorted);
                if (campaignAwarded == null)
                    campaignAwarded = awardedKeys(_awardDAO.getAwards(KIND_CAMPAIGN, event.campaign()));
                winners = campaignWinners(tier, campaignGames);
            } else {
                winners = eventWinners(tier, sorted);
            }
            if (winners.isEmpty())
                continue;

            String description = describeTier(tier);
            String reason = (StringUtils.isBlank(adminReason) ? event.name() + " prize" : adminReason.trim()) + ": " + description;
            List<CardCollection.Item> items = null;

            for (String player : winners) {
                String key = campaignTier ? awardKey(null, tier.effectiveLabel(), player) : awardKey(tierIndex, null, player);
                Set<String> already = campaignTier ? campaignAwarded : eventAwarded;
                if (already.contains(key))
                    continue;

                try {
                    if (items == null)
                        items = materialiseItems(event, tierIndex, tier, campaignTier);

                    _collectionsManager.addItemsToPlayerCollection(true, reason, player, CollectionType.MY_CARDS, items);

                    var award = new DBDefs.PrizeAward();
                    award.event_kind = campaignTier ? KIND_CAMPAIGN : event.kind();
                    award.event_id = campaignTier ? event.campaign() : event.id();
                    award.event_name = event.name();
                    award.tier_index = tierIndex;
                    award.tier_label = tier.effectiveLabel();
                    award.player = player;
                    award.items = itemsText(items);
                    award.awarded_on = DateUtils.Now().toLocalDateTime();
                    _awardDAO.addAward(award);
                    already.add(key);
                } catch (RuntimeException exp) {
                    logger.error("Unable to award '" + description + "' of " + event.name() + " to " + player, exp);
                }
            }
        }
    }

    private static List<PlayerStanding> sortedStandings(List<PlayerStanding> standings) {
        List<PlayerStanding> sorted = new ArrayList<>();
        if (standings != null)
            for (PlayerStanding standing : standings)
                if (standing != null && standing.playerName != null)
                    sorted.add(standing);
        sorted.sort(Comparator.comparingInt((PlayerStanding s) -> s.standing)
                .thenComparing(Comparator.comparingInt((PlayerStanding s) -> s.points).reversed())
                .thenComparing(s -> s.playerName));
        return sorted;
    }

    /**
     * Who qualifies for a tier by this event's standings alone.
     * <p>
     * PLACEMENT is lenient about ties: everyone whose standing is within {@code from..to} qualifies, and so does
     * anyone below the cutoff with the same points as the player in the last qualifying place (so that tie-breakers
     * such as strength of schedule never decide a prize).  A tier whose first place is beyond the field awards
     * nobody.
     */
    static List<String> eventWinners(PrizeTier tier, List<PlayerStanding> sorted) {
        List<String> winners = new ArrayList<>();
        if (tier.kind == PrizeTier.Kind.PARTICIPATION) {
            for (PlayerStanding standing : sorted)
                if (standing.gamesPlayed >= tier.games)
                    winners.add(standing.playerName);
            return winners;
        }

        if (tier.from > sorted.size())
            return winners;

        Integer cutoffPoints = null;
        for (PlayerStanding standing : sorted)
            if (standing.standing <= tier.to)
                cutoffPoints = cutoffPoints == null ? standing.points : Math.min(cutoffPoints, standing.points);

        for (PlayerStanding standing : sorted) {
            if (standing.standing < tier.from)
                continue;
            if (standing.standing <= tier.to || (cutoffPoints != null && standing.points >= cutoffPoints))
                winners.add(standing.playerName);
        }
        return winners;
    }

    private static List<String> campaignWinners(PrizeTier tier, Map<String, Integer> campaignGames) {
        List<String> winners = new ArrayList<>();
        for (var entry : campaignGames.entrySet())
            if (entry.getValue() >= tier.games)
                winners.add(entry.getKey());
        Collections.sort(winners);
        return winners;
    }

    /**
     * Games played per player across every league carrying the event's campaign tag (within the look-back window).
     * For an event that is not a league (whose games are not in the league match table) the event's own standings
     * are added on top.
     */
    private Map<String, Integer> countCampaignGames(EventRef event, List<PlayerStanding> sorted) {
        Map<String, Integer> games = new TreeMap<>();
        // every player of this event is a candidate even with no games on record
        for (PlayerStanding standing : sorted)
            games.merge(standing.playerName, 0, Integer::sum);

        List<League> leagues;
        try {
            leagues = _leagueDAO.loadActiveLeagues(DateUtils.Now().minusDays(CAMPAIGN_LOOKBACK_DAYS));
        } catch (Exception exp) {
            throw new RuntimeException("Unable to load the leagues of campaign " + event.campaign(), exp);
        }
        if (leagues != null) {
            for (League league : leagues) {
                LeagueParams params;
                try {
                    params = league.getLeagueData(_productLibrary, _formatLibrary, _soloDraftDefinitions).getParameters();
                } catch (RuntimeException exp) {
                    logger.warn("Skipping league " + league.getName() + " while counting campaign games: " + exp.getMessage());
                    continue;
                }
                if (params == null || !event.campaign().equals(params.campaign))
                    continue;
                Collection<LeagueMatchResult> matches = _leagueMatchDAO.getLeagueMatches(league.getCodeStr());
                if (matches == null)
                    continue;
                for (LeagueMatchResult match : matches) {
                    games.merge(match.getWinner(), 1, Integer::sum);
                    games.merge(match.getLoser(), 1, Integer::sum);
                }
            }
        }

        if (!KIND_LEAGUE.equals(event.kind()))
            for (PlayerStanding standing : sorted)
                games.merge(standing.playerName, standing.gamesPlayed, Integer::sum);

        return games;
    }

    /**
     * Turns the tier's items into what is actually delivered: concrete items as they are, promises as their
     * placeholder card.
     */
    private List<CardCollection.Item> materialiseItems(EventRef event, int tierIndex, PrizeTier tier, boolean campaignTier) {
        List<CardCollection.Item> items = new ArrayList<>();
        for (PrizeItem item : tier.items) {
            if (item == null)
                continue;
            int count = Math.max(1, item.count);
            if (item.hasPromise()) {
                String label = item.promise.trim();
                DBDefs.PrizePlaceholder placeholder = campaignTier
                        ? placeholderFor(KIND_CAMPAIGN, event.campaign(), null, label, count, event.name())
                        : placeholderFor(event.kind(), event.id(), tierIndex, label, count, event.name());
                // A promise resolved before the event ended is simply the real card by now
                String blueprint = placeholder.isResolved() ? placeholder.resolved_blueprint : placeholderBlueprint(placeholder.id);
                items.add(CardCollection.Item.createItem(blueprint, count));
            } else if (StringUtils.isNotBlank(item.blueprintId)) {
                items.add(CardCollection.Item.createItem(item.blueprintId.trim(), count));
            }
        }
        return items;
    }

    /**
     * Records the promises of an event's tiers as placeholders, so they are listed (and can be resolved) from the
     * moment the event exists rather than when it ends.  Safe to call again after an edit: existing rows are reused,
     * new promises get new rows, and a promise that was removed keeps its row (it may already be held by someone).
     */
    public void registerPromises(EventRef event, List<PrizeTier> tiers) {
        if (event == null || tiers == null)
            return;
        Set<String> eventLabels = new HashSet<>();
        int recorded = 0;
        for (int tierIndex = 0; tierIndex < tiers.size(); tierIndex++) {
            PrizeTier tier = tiers.get(tierIndex);
            if (tier == null || tier.items == null)
                continue;
            boolean campaignTier = tier.kind == PrizeTier.Kind.PARTICIPATION && tier.scope == PrizeTier.Scope.CAMPAIGN
                    && StringUtils.isNotBlank(event.campaign());
            for (PrizeItem item : tier.items) {
                if (item == null || !item.hasPromise())
                    continue;
                String label = item.promise.trim();
                int count = Math.max(1, item.count);
                try {
                    if (campaignTier) {
                        placeholderFor(KIND_CAMPAIGN, event.campaign(), null, label, count, event.name());
                    } else {
                        placeholderFor(event.kind(), event.id(), tierIndex, label, count, event.name());
                        eventLabels.add(label);
                    }
                    recorded++;
                } catch (RuntimeException exp) {
                    logger.error("Unable to record the promise '" + label + "' of " + event.name(), exp);
                }
            }
        }

        // Promises the event no longer makes are dropped again, as long as nobody holds them and they were never
        // resolved (campaign promises are shared with other events and are left alone).
        int dropped = 0;
        if (event.id() != null) {
            try {
                for (DBDefs.PrizePlaceholder row : _placeholderDAO.findByEvent(event.kind(), event.id())) {
                    if (row.isResolved() || eventLabels.contains(row.label))
                        continue;
                    if (holderCount(row.id) > 0)
                        continue;
                    _placeholderDAO.deletePlaceholder(row.id);
                    _library.unregisterPlaceholder(placeholderBlueprint(row.id));
                    dropped++;
                }
            } catch (RuntimeException exp) {
                logger.error("Unable to drop the withdrawn promises of " + event.name(), exp);
            }
        }
        logger.info(event.kind() + " '" + event.name() + "': " + recorded + " prize promise(s) recorded, " + dropped + " withdrawn");
    }

    /**
     * The placeholder for a promise (keyed by event and label), created when there is none yet, and registered with
     * the library while it is unresolved.
     */
    private DBDefs.PrizePlaceholder placeholderFor(String eventKind, String eventId, Integer tierIndex, String label, int count, String eventName) {
        DBDefs.PrizePlaceholder existing = _placeholderDAO.findPromise(eventKind, eventId, label);
        if (existing != null) {
            if (!existing.isResolved())
                _library.registerPlaceholder(placeholderBlueprint(existing.id), existing.label);
            return existing;
        }
        var row = new DBDefs.PrizePlaceholder();
        row.label = label;
        row.count = count;
        row.event_kind = eventKind;
        row.event_id = eventId;
        row.event_name = eventName;
        row.tier_index = tierIndex;
        row.created = DateUtils.Now().toLocalDateTime();
        row.id = _placeholderDAO.createPlaceholder(row);
        _library.registerPlaceholder(placeholderBlueprint(row.id), row.label);
        return row;
    }

    private static Set<String> awardedKeys(List<DBDefs.PrizeAward> awards) {
        Set<String> keys = new HashSet<>();
        if (awards != null)
            for (DBDefs.PrizeAward award : awards) {
                keys.add(awardKey(award.tier_index, null, award.player));
                keys.add(awardKey(null, award.tier_label, award.player));
            }
        return keys;
    }

    private static String awardKey(Integer tierIndex, String tierLabel, String player) {
        if (tierIndex != null)
            return "#" + tierIndex + "|" + player;
        return "@" + tierLabel + "|" + player;
    }

    static String itemsText(List<CardCollection.Item> items) {
        StringBuilder sb = new StringBuilder();
        for (CardCollection.Item item : items) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(item.getCount()).append("x ").append(item.getBlueprintId());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------------------------------
    // Placeholders
    // ------------------------------------------------------------------------------------------------

    public List<DBDefs.PrizePlaceholder> getUnresolved() {
        return _placeholderDAO.getUnresolved();
    }

    public List<DBDefs.PrizePlaceholder> getResolved(int limit) {
        return _placeholderDAO.getResolved(limit);
    }

    public DBDefs.PrizePlaceholder getPlaceholder(int placeholderId) {
        return _placeholderDAO.getPlaceholder(placeholderId);
    }

    /**
     * @return everyone currently holding the placeholder, one entry per (player, collection)
     */
    public List<DBDefs.CollectionHolder> getHolders(int placeholderId) {
        List<DBDefs.CollectionHolder> holders = _collectionDAO.findHolders(placeholderBlueprint(placeholderId));
        return holders == null ? List.of() : holders;
    }

    /**
     * @return how many distinct players hold the placeholder
     */
    public int holderCount(int placeholderId) {
        Set<String> players = new HashSet<>();
        for (DBDefs.CollectionHolder holder : getHolders(placeholderId))
            players.add(holder.player_name);
        return players.size();
    }

    /**
     * Swaps a placeholder for its real card: for every holder the placeholder copies are removed and the same
     * number of {@code blueprintId} (modifiers included, so {@code 9_1*} hands out foils) is delivered as a new
     * transfer with the reason {@code Prize resolved: <label>}.  The row is then marked resolved and the placeholder
     * forgotten by the library (its id keeps rendering as a plain "Future Prize").
     * @throws PrizeDefinitionException if the placeholder does not exist or is already resolved, or the blueprint
     *                                  is not a known card or pack
     */
    public ResolutionResult resolve(int placeholderId, String blueprintId, String adminName) throws PrizeDefinitionException {
        DBDefs.PrizePlaceholder row = _placeholderDAO.getPlaceholder(placeholderId);
        if (row == null)
            throw new PrizeDefinitionException("placeholder", "There is no prize placeholder with id " + placeholderId + ".");
        if (row.isResolved())
            throw new PrizeDefinitionException("placeholder", "Placeholder " + placeholderId + " (" + row.label + ") was already resolved to " + row.resolved_blueprint + ".");
        if (StringUtils.isBlank(blueprintId))
            throw new PrizeDefinitionException("blueprintId", "The card to resolve the placeholder to must be given.");
        String target = blueprintId.trim();
        String problem = describeUnknownProduct(target, _library, _productLibrary);
        if (problem != null)
            throw new PrizeDefinitionException("blueprintId", "Cannot resolve to " + problem);

        String placeholderBlueprint = placeholderBlueprint(placeholderId);
        String reason = "Prize resolved: " + row.label;
        List<String> players = new ArrayList<>();
        int swapped = 0;

        for (DBDefs.CollectionHolder holder : getHolders(placeholderId)) {
            if (holder.quantity <= 0 || holder.player_name == null)
                continue;
            CollectionType collectionType = CollectionType.parseCollectionCode(holder.collection_type);
            if (collectionType == null)
                collectionType = new CollectionType(holder.collection_type, holder.collection_type);

            try {
                var removal = new DefaultCardCollection();
                removal.addItem(placeholderBlueprint, holder.quantity);
                _collectionsManager.removeFromPlayerCollection(holder.player_name, holder.collection_type, removal, reason);
                _collectionsManager.addItemsToPlayerCollection(true, reason, holder.player_name, collectionType,
                        List.of(CardCollection.Item.createItem(target, holder.quantity)));
            } catch (Exception exp) {
                throw new RuntimeException("Unable to swap placeholder " + placeholderId + " for " + target + " in "
                        + holder.player_name + "'s " + holder.collection_type + " collection", exp);
            }
            if (!players.contains(holder.player_name))
                players.add(holder.player_name);
            swapped += holder.quantity;
        }

        _placeholderDAO.markResolved(placeholderId, target, adminName);
        _library.unregisterPlaceholder(placeholderBlueprint);
        logger.info("Prize placeholder " + placeholderId + " (" + row.label + ") resolved to " + target + " by " + adminName
                + ": " + swapped + " card(s) swapped for " + players.size() + " player(s)");
        return new ResolutionResult(placeholderId, row.label, target, Collections.unmodifiableList(players), swapped);
    }

    /**
     * Creates a promise by hand (event kind {@link #KIND_MANUAL}) and hands its placeholder to the named players.
     * Nothing is awarded unless every player name is known.
     * @return the placeholder row, with its id
     */
    public DBDefs.PrizePlaceholder createManualPromise(String label, int count, List<String> players, String adminName) throws PrizeDefinitionException {
        if (StringUtils.isBlank(label))
            throw new PrizeDefinitionException("label", "The promise needs a label.");
        if (label.trim().length() > MAX_LABEL_LENGTH)
            throw new PrizeDefinitionException("label", "The label must be " + MAX_LABEL_LENGTH + " characters or less.");
        if (count < 1)
            throw new PrizeDefinitionException("count", "The count must be at least 1.");
        if (players == null || players.isEmpty())
            throw new PrizeDefinitionException("players", "At least one player must be named.");

        List<String> names = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        for (String player : players) {
            if (StringUtils.isBlank(player))
                continue;
            String name = player.trim();
            if (names.contains(name))
                continue;
            if (_playerDAO.getPlayer(name) == null)
                unknown.add(name);
            else
                names.add(name);
        }
        if (!unknown.isEmpty())
            throw new PrizeDefinitionException("players", "Unknown player(s): " + String.join(", ", unknown) + ". Nothing was awarded.");
        if (names.isEmpty())
            throw new PrizeDefinitionException("players", "At least one player must be named.");

        var row = new DBDefs.PrizePlaceholder();
        row.label = label.trim();
        row.count = count;
        row.event_kind = KIND_MANUAL;
        row.created = DateUtils.Now().toLocalDateTime();
        row.created_by = adminName;
        row.id = _placeholderDAO.createPlaceholder(row);
        _library.registerPlaceholder(placeholderBlueprint(row.id), row.label);

        List<CardCollection.Item> items = List.of(CardCollection.Item.createItem(placeholderBlueprint(row.id), count));
        String reason = "Prize: " + row.label;
        for (String name : names) {
            _collectionsManager.addItemsToPlayerCollection(true, reason, name, CollectionType.MY_CARDS, items);

            var award = new DBDefs.PrizeAward();
            award.event_kind = KIND_MANUAL;
            award.event_id = String.valueOf(row.id);
            award.event_name = row.label;
            award.tier_index = null;
            award.tier_label = row.label;
            award.player = name;
            award.items = itemsText(items);
            award.awarded_on = DateUtils.Now().toLocalDateTime();
            _awardDAO.addAward(award);
        }
        return row;
    }

    /**
     * Tells the card library the title of every outstanding placeholder, so that {@code 404_<id>} renders as the
     * promise it stands for.  Called at construction; safe to call again after a library reload.
     */
    public void registerPlaceholdersWithLibrary() {
        List<DBDefs.PrizePlaceholder> unresolved = _placeholderDAO.getUnresolved();
        if (unresolved == null)
            return;
        for (DBDefs.PrizePlaceholder row : unresolved)
            _library.registerPlaceholder(placeholderBlueprint(row.id), row.label);
    }
}
