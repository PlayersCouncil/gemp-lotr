package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModifierSourceFactory {
    private final Map<String, ModifierSourceProducer> modifierProducers = new HashMap<>();

    public ModifierSourceFactory() {
        modifierProducers.put("addactivated", new AddActivated());
        modifierProducers.put("addkeyword", new AddKeyword());
        modifierProducers.put("addnotwilightforcompanionmove", new AddNoTwilightForCompanionMove());
        modifierProducers.put("addsignet", new AddSignet());
        modifierProducers.put("alliestakearcheryfirewoundsinsteadofcompanions", new AddModifierFlag(ModifierFlag.ALLIES_TAKE_ARCHERY_FIRE_WOUNDS_INSTEAD_OF_COMPANIONS));
        modifierProducers.put("allycanparticipateinarcheryfireandskirmishes", new AllyCanParticipateInArcheryFireAndSkirmishes());
        modifierProducers.put("allycanparticipateinarcheryfire", new AllyCanParticipateInArcheryFire());
        modifierProducers.put("allycanparticipateinskirmishes", new AllyCanParticipateInSkirmishes());
        modifierProducers.put("archerytotal", new ArcheryTotal());
        modifierProducers.put("cancelkeywordbonusfrom", new CancelKeywordBonusFrom());
        modifierProducers.put("cancelstrengthbonusfrom", new CancelStrengthBonusFrom());
        modifierProducers.put("cancelstrengthbonusto", new CancelStrengthBonusTo());
        modifierProducers.put("canplaystackedcards", new CanPlayStackedCards());
        modifierProducers.put("cantbear", new CantBear());
        modifierProducers.put("cantbeassignedtoskirmish", new CantBeAssignedToSkirmish());
        modifierProducers.put("cantbeassignedtoskirmishagainst", new CantBeAssignedToSkirmishAgainst());
        modifierProducers.put("cantbediscarded", new CantBeDiscarded());
        modifierProducers.put("cantbeexerted", new CantBeExerted());
        modifierProducers.put("cantbeoverwhelmedmultiplier", new CantBeOverwhelmedMultiplier());
        modifierProducers.put("cantbetransferred", new CantBeTransferred());
        modifierProducers.put("cantcancelskirmish", new CantCancelSkirmish());
        modifierProducers.put("cantdiscardcardsfromhandortopofdrawdeck", new CantDiscardCardsFromHandOrTopOfDrawDeck());
        modifierProducers.put("cantheal", new CantHeal());
        modifierProducers.put("cantlookorrevealhand", new CantLookOrRevealHand());
        modifierProducers.put("cantplaycards", new CantPlayCards());
        modifierProducers.put("cantplaycardsfromdeckordiscardpile", new AddModifierFlag(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK));
        modifierProducers.put("cantplaycardson", new CantPlayCardsOn());
        modifierProducers.put("cantplayphaseevents", new CantPlayPhaseEvents());
        modifierProducers.put("cantplayphaseeventsorphasespecialabilities", new CantPlayPhaseEventsOrPhaseSpecialAbilities());
        modifierProducers.put("cantplayphasespecialabilities", new CantPlayPhaseSpecialAbilities());
        modifierProducers.put("cantpreventwounds", new AddModifierFlag(ModifierFlag.CANT_PREVENT_WOUNDS));
        modifierProducers.put("cantremoveburdens", new CantRemoveBurdens());
        modifierProducers.put("cantreplacesite", new CantReplaceSite());
        modifierProducers.put("canttakearcherywounds", new CantTakeArcheryWounds());
        modifierProducers.put("canttakemorewoundsthan", new CantTakeMoreWoundsThan());
        modifierProducers.put("canttakewounds", new CantTakeWounds());
        modifierProducers.put("cantusespecialabilities", new CantUseSpecialAbilities());
        modifierProducers.put("disablegametext", new DisableGameText());
        modifierProducers.put("doesnotaddtoarcherytotal", new DoesNotAddToArcheryTotal());
        modifierProducers.put("extracosttoplay", new ExtraCostToPlay());
        modifierProducers.put("fpculturespot", new FPCultureSpot());
        modifierProducers.put("fpusesresinsteadofstr", new FPUsesResInsteadOfStr());
        modifierProducers.put("fpusesvitinsteadofstr", new FPUsesVitInsteadOfStr());
        modifierProducers.put("fpcantassigntoskirmish", new FPCantAssignToSkirmish());
        modifierProducers.put("hastomoveifable", new AddModifierFlag(ModifierFlag.HAS_TO_MOVE_IF_POSSIBLE));
        modifierProducers.put("itemclassspot", new ItemClassSpot());
        modifierProducers.put("modifyarcherytotal", new ModifyArcheryTotal());
        modifierProducers.put("modifycost", new ModifyCost());
        modifierProducers.put("modifymovelimit", new ModifyMoveLimit());
        modifierProducers.put("modifyplayoncost", new ModifyPlayOnCost());
        modifierProducers.put("modifyresistance", new ModifyResistance());
        modifierProducers.put("modifyroamingpenalty", new ModifyRoamingPenalty());
        modifierProducers.put("modifysanctuaryheal", new ModifySanctuaryHeal());
        modifierProducers.put("modifysitenumber", new ModifySiteNumber());
        modifierProducers.put("modifystrength", new ModifyStrength());
        modifierProducers.put("nomorethanoneminionmaybeassignedtoeachskirmish", new NoMorethanOneMinionMayBeAssignedToEachSkirmish());
        modifierProducers.put("opponentscantplayphaseeventsorphasespecialabilities", new OpponentsCantPlayPhaseEventsOrPhaseSpecialAbilities());
        modifierProducers.put("opponentmaynotdiscard", new OpponentMayNotDiscard());
        modifierProducers.put("removekeyword", new RemoveKeyword());
        modifierProducers.put("ringtextisinactive", new AddModifierFlag(ModifierFlag.RING_TEXT_INACTIVE));
        modifierProducers.put("sarumanfirstsentenceinactive", new AddModifierFlag(ModifierFlag.SARUMAN_FIRST_SENTENCE_INACTIVE));
        modifierProducers.put("shadowhasinitiative", new ShadowHasInitiative());
        modifierProducers.put("shadowusesvitinsteadofstr", new ShadowUsesVitInsteadOfStr());
        modifierProducers.put("skipphase", new SkipPhase());
    }

    public ModifierSource getModifier(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(object.get("type"), "type");
        final ModifierSourceProducer modifierSourceProducer = modifierProducers.get(type.toLowerCase());
        if (modifierSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + type);
        return modifierSourceProducer.getModifierSource(object, environment);
    }
}
