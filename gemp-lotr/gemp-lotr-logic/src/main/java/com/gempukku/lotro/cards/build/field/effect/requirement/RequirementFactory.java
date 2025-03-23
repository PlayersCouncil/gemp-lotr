package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequirementFactory {
    private final Map<String, RequirementProducer> requirementProducers = new HashMap<>();

    public RequirementFactory() {
        requirementProducers.put("and", new AndRequirementProducer());
        requirementProducers.put("not", new NotRequirementProducer());
        requirementProducers.put("or", new OrRequirementProducer());
        requirementProducers.put("isequal", new IsEqual());
        requirementProducers.put("isgreaterthan", new IsGreaterThan());
        requirementProducers.put("isgreaterthanorequal", new IsGreaterThanOrEqual());
        requirementProducers.put("islessthan", new IsLessThan());
        requirementProducers.put("islessthanorequal", new IsLessThanOrEqual());
        requirementProducers.put("canplayfromdiscard", new CanPlayFromDiscard());
        requirementProducers.put("canselfbeplayed", new CanSelfBePlayed());
        requirementProducers.put("canspot", new CanSpot());
        requirementProducers.put("cantspot", new CantSpot());
        requirementProducers.put("canspotburdens", new CanSpotBurdens());
        requirementProducers.put("cantspotburdens", new CantSpotBurdens());
        requirementProducers.put("canspotculturetokens", new CanSpotCultureTokens());
        requirementProducers.put("canspotfpcultures", new CanSpotFPCultures());
        requirementProducers.put("cantspotfpcultures", new CantSpotFPCultures());
        requirementProducers.put("canspotdifferentcultures", new CanSpotDifferentCultures());
        requirementProducers.put("canspotsameculture", new CanSpotSameCulture());
        requirementProducers.put("canspotshadowcultures", new CanSpotShadowCultures());
        requirementProducers.put("canspotthreats", new CanSpotThreats());
        requirementProducers.put("cantspotthreats", new CantSpotThreats());
        requirementProducers.put("canspottwilight", new CanSpotTwilight());
        requirementProducers.put("canspotwounds", new CanSpotWounds());
        requirementProducers.put("cardsindeckcount", new CardsInDeckCount());
        requirementProducers.put("cardsinhandmorethan", new CardsInHandMoreThan());
        requirementProducers.put("controlssite", new ControlsSite());
        requirementProducers.put("didwinskirmish", new DidWinSkirmish());
        requirementProducers.put("fierceskirmish", new FierceSkirmish());
        requirementProducers.put("hascardinadventuredeck", new HasCardInAdventureDeck());
        requirementProducers.put("hascardindeadpile", new HasCardInDeadPile());
        requirementProducers.put("hascardindiscard", new HasCardInDiscard());
        requirementProducers.put("hascardinhand", new HasCardInHand());
        requirementProducers.put("hascardinremoved", new HasCardInRemoved());
        requirementProducers.put("hascardstacked", new HasCardStacked());
        requirementProducers.put("hascardinmemory", new HasCardInMemory());
        requirementProducers.put("hasmemory", new HasMemory());
        requirementProducers.put("hasinzonedata", new HasInZoneData());
        requirementProducers.put("haveinitiative", new HaveInitiative());
        requirementProducers.put("isahead", new IsAhead());
        requirementProducers.put("isowner", new IsOwnerRequirementProducer());
        requirementProducers.put("isside", new IsSideRequirementProducer());
        requirementProducers.put("killedwithsurplusdamage", new KilledWithSurplusDamage());
        requirementProducers.put("location", new Location());
        requirementProducers.put("lostskirmishthisturn", new LostSkirmishThisTurn());
        requirementProducers.put("memoryis", new MemoryIs());
        requirementProducers.put("memorylike", new MemoryLike());
        requirementProducers.put("memorymatches", new MemoryMatches());
        requirementProducers.put("movecountminimum", new MoveCountMinimum());
        requirementProducers.put("opponentdoesnotcontrolsite", new OpponentDoesNotControlSite());
        requirementProducers.put("perphaselimit", new PerPhaseLimit());
        requirementProducers.put("perturnlimit", new PerTurnLimit());
        requirementProducers.put("playablefromdiscard", new PlayableFromDiscard());
        requirementProducers.put("phase", new PhaseRequirement());
        requirementProducers.put("playedcardthisphase", new PlayedCardThisPhase());
        requirementProducers.put("ringisactive", new RingIsActive());
        requirementProducers.put("ringison", new RingIsOn());
        requirementProducers.put("sarumanfirstsentenceactive", new SarumanFirstSentenceActive());
        requirementProducers.put("shadowplayerreplacedcurrentsite", new ShadowPlayerReplacedCurrentSite());
        requirementProducers.put("siteavailabletocontrol", new SiteAvailableToControl());
        requirementProducers.put("twilightpoollessthan", new TwilightPoolLessThan());
        requirementProducers.put("wasassignedtoskirmish", new WasAssignedToSkirmish());
        requirementProducers.put("wasplayedfromzone", new WasPlayedFromZone());
    }

    public Requirement getRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(object.get("type"), "type");
        final RequirementProducer requirementProducer = requirementProducers.get(type.toLowerCase());
        if (requirementProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        return requirementProducer.getPlayRequirement(object, environment);
    }

    public Requirement[] getRequirements(JSONObject[] object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Requirement[] result = new Requirement[object.length];
        for (int i = 0; i < object.length; i++)
            result[i] = getRequirement(object[i], environment);
        return result;
    }
}
