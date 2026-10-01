package com.gempukku.lotro.league;

import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.prizes.PrizeDefinitionException;
import com.gempukku.lotro.prizes.PrizeService;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The single path for turning a league definition ({@link League.LeagueType} + {@link LeagueParams}) into a league:
 * fills in defaults, validates the definition, instantiates the matching {@link LeagueData} and, on request,
 * persists it.  Both the admin API and any automated league creation go through here so that the validation rules
 * cannot drift apart.
 * <p>
 * Nothing in here knows about HTTP; a bad definition surfaces as a {@link LeagueDefinitionException} naming the
 * offending parameter.
 */
public class LeagueFactory {
    public static final int MAX_NAME_LENGTH = 45;
    public static final int MAX_RACE_PATH_LENGTH = 18;
    /**
     * Leagues stay visible for this many days after their last serie ends, so that final standings can be viewed.
     */
    public static final int DISPLAY_END_GRACE_DAYS = 2;

    private final LotroCardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueDAO _leagueDao;
    private final LeagueService _leagueService;
    private PrizeService _prizeService;
    private Random _raceRandom = new Random();
    private volatile RTMDPathGenerator.Pool _racePool;
    private volatile int _racePoolCards;

    /**
     * A validated definition together with the league it instantiates.  {@code params} is the definition as it will
     * be persisted (defaults already applied); {@code start}/{@code displayEnd} are the dates stored on the league row.
     */
    public record PreparedLeague(League.LeagueType type, LeagueParams params, LeagueData data,
                                 List<LeagueSerieInfo> series, ZonedDateTime start, ZonedDateTime displayEnd) {
    }

    public LeagueFactory(LotroCardBlueprintLibrary cardLibrary, ProductLibrary productLibrary,
                         LotroFormatLibrary formatLibrary, SoloDraftDefinitions soloDraftDefinitions,
                         LeagueDAO leagueDao, LeagueService leagueService) {
        _cardLibrary = cardLibrary;
        _productLibrary = productLibrary;
        _formatLibrary = formatLibrary;
        _soloDraftDefinitions = soloDraftDefinitions;
        _leagueDao = leagueDao;
        _leagueService = leagueService;
    }

    /**
     * Applies defaults, validates and instantiates the league without persisting anything.  Use the result to
     * preview the league or hand it to {@link #create(PreparedLeague)}.
     * @throws LeagueDefinitionException if the definition is not valid; nothing has been modified except that
     *                                   defaults may have been filled in on {@code params}.
     */
    public PreparedLeague prepare(League.LeagueType type, LeagueParams params) throws LeagueDefinitionException {
        if (type == null)
            throw new LeagueDefinitionException("type", "League type must be provided.");
        if (params == null)
            throw new LeagueDefinitionException("parameters", "League parameters must be provided.");

        applyDefaults(type, params);
        validate(type, params);

        LeagueData data = instantiate(type, params);
        List<LeagueSerieInfo> series = data.getSeries();
        if (series == null || series.isEmpty())
            throw new LeagueDefinitionException("series", "The league definition produced no series.");

        ZonedDateTime start = series.getFirst().getStart();
        ZonedDateTime displayEnd = series.getLast().getEnd().plusDays(DISPLAY_END_GRACE_DAYS);

        return new PreparedLeague(type, params, data, Collections.unmodifiableList(series), start, displayEnd);
    }

    /**
     * Persists a prepared league and refreshes the league cache.
     * @return the database id of the new league row
     */
    public int create(PreparedLeague prepared) {
        return create(prepared, null);
    }

    /**
     * Persists a prepared league, recording which league schedule (if any) created it, and refreshes the league cache.
     * @param scheduleId the league_schedule row on whose behalf the league is created, or null for a hand-made league
     * @return the database id of the new league row
     */
    public int create(PreparedLeague prepared, Integer scheduleId) {
        var params = prepared.params();
        int id = _leagueDao.addLeague(params.name, params.code, prepared.type(), params, prepared.start(),
                prepared.displayEnd(), params.cost, scheduleId);
        _leagueService.clearCache();
        registerPromises(params);
        return id;
    }

    /**
     * Prize promises become placeholders as soon as the league exists, so admins can resolve them at any time
     * (a promise resolved before the league ends is awarded as the real card).
     */
    private void registerPromises(LeagueParams params) {
        if (_prizeService == null || params.prizeTiers == null || params.prizeTiers.isEmpty())
            return;
        _prizeService.registerPromises(new PrizeService.EventRef(PrizeService.KIND_LEAGUE, String.valueOf(params.code),
                params.name, params.campaign), params.prizeTiers);
    }

    /**
     * Lets promises be recorded on creation; without it leagues are still created, promises are simply recorded
     * when they are awarded.
     */
    public void setPrizeService(PrizeService prizeService) {
        _prizeService = prizeService;
    }

    /**
     * Convenience for {@code create(prepare(type, params))}.
     */
    public int create(League.LeagueType type, LeagueParams params) throws LeagueDefinitionException {
        return create(prepare(type, params));
    }

    /**
     * Replaces the definition of an existing league with {@code params}, validating it exactly as {@link #prepare}
     * does for a new league, then rewrites the league row and refreshes the league cache.
     * <p>
     * The league's type and code are taken from {@code existing} and cannot be changed: the code is what the
     * participants' collections, match history and standings are keyed on, so any code carried in {@code params}
     * is overwritten.  Changing the name, description, cost, start date, serie durations, match limits, prizes,
     * invite-only flag or repeat-match limit is safe at any time (the collection display name follows
     * {@code collectionName} as usual).  Changing what a limited league hands out (the sealed template or draft
     * type) or a constructed league's collection after players have joined is <em>not</em> recoverable: collections
     * already dealt are not rebuilt, and nothing here blocks such an edit; the admin UI is responsible for warning.
     * Moving the start date of a league that has already started likewise does not undo anything that already
     * happened.
     * @param existing the league as currently stored
     * @param params   the new definition; defaults are filled in and {@code code} is overwritten
     * @return the prepared league as persisted
     * @throws LeagueDefinitionException if the definition is not valid; nothing has been persisted
     */
    public PreparedLeague update(League existing, LeagueParams params) throws LeagueDefinitionException {
        if (existing == null)
            throw new LeagueDefinitionException("code", "The league to update must be provided.");
        if (params == null)
            throw new LeagueDefinitionException("parameters", "League parameters must be provided.");

        params.code = existing.getCode();
        PreparedLeague prepared = prepare(existing.getType(), params);

        _leagueDao.updateLeague(existing.getCode(), params.name, params, prepared.start(), prepared.displayEnd(),
                params.cost);
        _leagueService.clearCache();
        registerPromises(params);
        return prepared;
    }

    /**
     * Fills in the parts of a definition that are derived rather than chosen: the league code (a timestamp, which
     * also keys the league's collection) and the collection name.
     */
    private void applyDefaults(League.LeagueType type, LeagueParams params) throws LeagueDefinitionException {
        if (params.code == 0)
            params.code = System.currentTimeMillis();

        // Limited leagues hand each player a fresh collection named after the league; constructed ones play from
        // an existing collection ("default" being the player's own cards).  LeagueParams initialises the field to
        // "default", so for limited leagues that value counts as unset.
        switch (type) {
            case SEALED, SOLODRAFT -> {
                if (StringUtils.isBlank(params.collectionName) || "default".equals(params.collectionName))
                    params.collectionName = params.name;
            }
            case CONSTRUCTED, RTMD -> {
                if (StringUtils.isBlank(params.collectionName))
                    params.collectionName = "default";
            }
        }

        if (params.series == null)
            params.series = new ArrayList<>();
        if (params.raceVisualPath == null)
            params.raceVisualPath = new ArrayList<>();
        if (params.prizeTiers == null)
            params.prizeTiers = new ArrayList<>();
        // a blank campaign tag means "no campaign"
        params.campaign = StringUtils.isBlank(params.campaign) ? null : params.campaign.trim();

        generateRacePath(type, params);
    }

    /**
     * Rolls the race path of a definition that asked for one ({@code raceRandomizeEachInstance}), which is how a
     * league schedule keeps its races fresh: the schedule stores the flag, and every league it materialises gets its
     * own randomisation at that moment rather than a copy of one path chosen when the schedule was written.
     * <p>
     * The flag is cleared on the way out, so the league that is persisted holds an ordinary fixed path that an admin
     * can still tweak (through the league editor) while the league has not started yet.  A definition without the
     * flag - every hand-made league - is left exactly as it was.
     */
    private void generateRacePath(League.LeagueType type, LeagueParams params) throws LeagueDefinitionException {
        if (type != League.LeagueType.RTMD || !params.raceRandomizeEachInstance)
            return;

        params.raceRandomizeEachInstance = false;

        int length = params.racePathLength;
        if (length <= 0)
            length = (params.racePath == null || params.racePath.isEmpty())
                    ? RTMDPathGenerator.DEFAULT_PATH_LENGTH : params.racePath.size();

        var generated = RTMDPathGenerator.generate(racePool(), length,
                params.raceIntensityFloor, params.raceIntensityCeiling, null, _raceRandom);
        if (generated.isEmpty()) {
            // nothing matched the intensity range (or no cards are loaded); fall back to whatever the definition
            // already had, and say so plainly when it had nothing
            if (params.racePath != null && !params.racePath.isEmpty())
                return;
            throw new LeagueDefinitionException("racePath", "No race path could be generated: no meta-site has an"
                    + " intensity between " + params.raceIntensityFloor + " and " + params.raceIntensityCeiling + ".");
        }

        params.racePath = new ArrayList<>(generated.racePath());
        params.raceVisualPath = new ArrayList<>(generated.raceVisualPath());
        params.racePathLength = params.racePath.size();
    }

    /**
     * The cards a generated path is drawn from.  Reading them means walking the whole blueprint library, and
     * projecting a schedule's next few dozen occurrences prepares each of them, so the result is kept until the
     * library's card count changes (which is what a card reload does).
     */
    private RTMDPathGenerator.Pool racePool() {
        int cards = _cardLibrary == null ? 0 : _cardLibrary.getBaseCards().size();
        var pool = _racePool;
        if (pool == null || cards != _racePoolCards) {
            pool = RTMDPathGenerator.loadPool(_cardLibrary);
            _racePool = pool;
            _racePoolCards = cards;
        }
        return pool;
    }

    /**
     * Lets a test pin the randomness used for generated race paths.
     */
    public void setRaceRandom(Random raceRandom) {
        _raceRandom = raceRandom == null ? new Random() : raceRandom;
    }

    /**
     * Checks a definition against every rule the admin UI used to enforce, plus the lookups that would otherwise
     * fail deep inside the league constructors.  Public so that a definition can be checked without instantiating
     * anything (for instance when an admin saves a schedule template).
     */
    public void validate(League.LeagueType type, LeagueParams params) throws LeagueDefinitionException {
        if (type == null)
            throw new LeagueDefinitionException("type", "League type must be provided.");
        if (params == null)
            throw new LeagueDefinitionException("parameters", "League parameters must be provided.");

        // --- fields common to every type ---
        if (StringUtils.isBlank(params.name))
            throw new LeagueDefinitionException("name", "Parameter 'name' cannot be blank.");
        if (params.name.length() > MAX_NAME_LENGTH)
            throw new LeagueDefinitionException("name", "League name must be " + MAX_NAME_LENGTH + " characters or less.");
        if (params.start == null)
            throw new LeagueDefinitionException("start", "Parameter 'start' must be provided.");
        if (params.cost < 0)
            throw new LeagueDefinitionException("cost", "League cost cannot be negative.");
        if (params.maxRepeatMatches < 1)
            throw new LeagueDefinitionException("maxRepeatMatches", "Players must be allowed to face each other at least once.");
        if (StringUtils.isBlank(params.collectionName))
            throw new LeagueDefinitionException("collectionName", "Parameter 'collectionName' cannot be blank.");

        // the campaign tag is stored as prize_award.event_id / prize_placeholder.event_id (VARCHAR(45))
        if (params.campaign != null && params.campaign.length() > MAX_NAME_LENGTH)
            throw new LeagueDefinitionException("campaign", "Campaign tag must be " + MAX_NAME_LENGTH + " characters or less.");

        try {
            PrizeService.validateTiers(params.prizeTiers, _cardLibrary, _productLibrary);
        } catch (PrizeDefinitionException exp) {
            throw new LeagueDefinitionException("prizeTiers", exp.getMessage());
        }

        // --- series ---
        if (params.series == null || params.series.isEmpty())
            throw new LeagueDefinitionException("series", "At least one series must be defined.");

        for (int i = 0; i < params.series.size(); i++) {
            var serie = params.series.get(i);
            String label = "series[" + i + "]";
            if (serie == null)
                throw new LeagueDefinitionException(label, "Series definition " + (i + 1) + " is missing.");
            if (StringUtils.isBlank(serie.format()))
                throw new LeagueDefinitionException(label + ".format", "Series " + (i + 1) + " has no format.");
            if (serie.duration() < 1)
                throw new LeagueDefinitionException(label + ".duration", "Series " + (i + 1) + " must last at least one day.");
            if (serie.matches() < 1)
                throw new LeagueDefinitionException(label + ".matches", "Series " + (i + 1) + " must allow at least one ranked match.");
        }

        switch (type) {
            case SEALED -> validateSealed(params);
            case SOLODRAFT -> validateSoloDraft(params);
            case CONSTRUCTED -> validateConstructedSeries(params);
            case RTMD -> {
                validateConstructedSeries(params);
                validateRace(params);
            }
        }
    }

    private void validateSealed(LeagueParams params) throws LeagueDefinitionException {
        if (params.series.size() != 1)
            throw new LeagueDefinitionException("series", "A sealed league takes exactly one series definition; the sealed template determines how many series are played.");

        String template = params.series.getFirst().format();
        try {
            if (_formatLibrary.GetSealedTemplate(template) == null)
                throw new LeagueDefinitionException("format", "Unknown sealed league template '" + template + "'.");
        } catch (RuntimeException exp) {
            // GetSealedTemplate throws rather than returning null for an unknown template
            throw new LeagueDefinitionException("format", "Unknown sealed league template '" + template + "'.");
        }
    }

    private void validateSoloDraft(LeagueParams params) throws LeagueDefinitionException {
        if (params.series.size() != 1)
            throw new LeagueDefinitionException("series", "A solo draft league takes exactly one series definition.");

        String draft = params.series.getFirst().format();
        if (_soloDraftDefinitions.getSoloDraft(draft) == null)
            throw new LeagueDefinitionException("format", "Unknown solo draft type '" + draft + "'.");
    }

    private void validateConstructedSeries(LeagueParams params) throws LeagueDefinitionException {
        for (int i = 0; i < params.series.size(); i++) {
            String format = params.series.get(i).format();
            if (_formatLibrary.getFormat(format) == null)
                throw new LeagueDefinitionException("series[" + i + "].format", "Unknown format '" + format + "' in series " + (i + 1) + ".");
        }
    }

    private void validateRace(LeagueParams params) throws LeagueDefinitionException {
        var path = params.racePath;
        if (path == null || path.isEmpty())
            throw new LeagueDefinitionException("racePath", "Race path must contain at least one modifier.");
        if (path.size() > MAX_RACE_PATH_LENGTH)
            throw new LeagueDefinitionException("racePath", "Race path must contain between 1 and " + MAX_RACE_PATH_LENGTH
                    + " modifiers (got " + path.size() + ").");
        for (String blueprintId : path)
            requireBlueprint("racePath", blueprintId, "race path");

        var visualPath = params.raceVisualPath;
        if (visualPath == null || visualPath.isEmpty())
            throw new LeagueDefinitionException("raceVisualPath", "Visual path must contain at least one card.");
        if (visualPath.size() != path.size())
            throw new LeagueDefinitionException("raceVisualPath", "Visual path must have the same number of entries as the modifier path ("
                    + path.size() + ").");
        for (String blueprintId : visualPath)
            requireBlueprint("raceVisualPath", blueprintId, "visual path");

        if (params.raceIntensityFloor > params.raceIntensityCeiling)
            throw new LeagueDefinitionException("raceIntensity", "Intensity floor must be <= ceiling.");
        if (params.raceAdvancementMode == null)
            throw new LeagueDefinitionException("raceAdvancementMode", "Advancement mode must be WIN or SCORE.");
        if (params.raceAdvanceFactor < 1)
            throw new LeagueDefinitionException("raceAdvanceFactor", "Advance factor must be at least 1.");
    }

    private void requireBlueprint(String parameter, String blueprintId, String where) throws LeagueDefinitionException {
        if (StringUtils.isBlank(blueprintId))
            throw new LeagueDefinitionException(parameter, "Blank blueprint ID in " + where + ".");
        try {
            if (_cardLibrary.getLotroCardBlueprint(blueprintId) == null)
                throw new LeagueDefinitionException(parameter, "Blueprint '" + blueprintId + "' not found in " + where + ".");
        } catch (LeagueDefinitionException exp) {
            throw exp;
        } catch (Exception exp) {
            throw new LeagueDefinitionException(parameter, "Invalid blueprint ID in " + where + ": " + blueprintId);
        }
    }

    private LeagueData instantiate(League.LeagueType type, LeagueParams params) {
        return switch (type) {
            case SEALED -> new SealedLeague(_productLibrary, _formatLibrary, params);
            case SOLODRAFT -> new SoloDraftLeague(_productLibrary, _formatLibrary, _soloDraftDefinitions, params);
            case CONSTRUCTED -> new ConstructedLeague(_productLibrary, _formatLibrary, params);
            case RTMD -> new RTMDLeague(_productLibrary, _formatLibrary, params);
        };
    }
}
