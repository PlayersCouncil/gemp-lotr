package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.*;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectAppenderFactory {
    private final Map<String, EffectAppenderProducer> effectAppenderProducers = new HashMap<>();

    public EffectAppenderFactory() {
        // Control-flow / Meta effects
        effectAppenderProducers.put("choice", new Choice());
        effectAppenderProducers.put("costtoeffect", new CostToEffect());
        effectAppenderProducers.put("dowhile", new DoWhile());
        effectAppenderProducers.put("filtercardsinmemory", new FilterCardsInMemory());
        effectAppenderProducers.put("foreachplayer", new ForEachPlayer());
        effectAppenderProducers.put("if", new IfEffect());
        effectAppenderProducers.put("multiple", new Multiple());
        effectAppenderProducers.put("optional", new Optional());
        effectAppenderProducers.put("preventable", new PreventableAppenderProducer());
        effectAppenderProducers.put("refreshself", new RefreshSelf());
        effectAppenderProducers.put("repeat", new Repeat());
        effectAppenderProducers.put("sendmessage", new SendMessage());

        //Regular effects
        effectAppenderProducers.put("addburdens", new AddBurdens());
        effectAppenderProducers.put("addkeyword", new AddKeyword());
        effectAppenderProducers.put("addmodifier", new AddModifier());
        effectAppenderProducers.put("addthreats", new AddThreats());
        effectAppenderProducers.put("addculturetokens", new AddCultureTokens());
        effectAppenderProducers.put("addtrigger", new AddTrigger());
        effectAppenderProducers.put("addtwilight", new AddTwilight());
        effectAppenderProducers.put("alteroverwhelmmultiplier", new AlterOverwhelmMultiplier());
        effectAppenderProducers.put("appendcardidstowhileinzone", new AppendCardIdsToWhileInZone());
        effectAppenderProducers.put("assignfpcharactertoskirmish", new AssignFpCharacterToSkirmish());
        effectAppenderProducers.put("assignthreatwounds", new AssignThreatWounds());
        effectAppenderProducers.put("cancelallassignments", new CancelAllAssignments());
        effectAppenderProducers.put("cancelevent", new CancelEvent());
        effectAppenderProducers.put("cancelskirmish", new CancelSkirmish());
        effectAppenderProducers.put("cancelspecialability", new CancelSpecialAbility());
        effectAppenderProducers.put("canplaynextaction", new CanPlayNextAction());
        effectAppenderProducers.put("chooseactivecards", new ChooseActiveCards());
        effectAppenderProducers.put("chooseaculture", new ChooseACulture());
        effectAppenderProducers.put("chooseakeyword", new ChooseAKeyword());
        effectAppenderProducers.put("chooseanumber", new ChooseANumber());
        effectAppenderProducers.put("choosearace", new ChooseARace());
        effectAppenderProducers.put("choosearbitrarycards", new ChooseArbitraryCards());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardsFromDiscard());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardsFromDrawDeck());
        effectAppenderProducers.put("choosecardsfromsinglestack", new ChooseCardsFromSingleStack());
        effectAppenderProducers.put("choosehowmanyburdenstospot", new ChooseHowManyBurdensToSpot());
        effectAppenderProducers.put("choosehowmanyfpculturestospot", new ChooseHowManyFPCulturesToSpot());
        effectAppenderProducers.put("choosehowmanytospot", new ChooseHowManyToSpot());
        effectAppenderProducers.put("choosehowmanythreatstospot", new ChooseHowManyThreatsToSpot());
        effectAppenderProducers.put("choosehowmanytwilighttokenstospot", new ChooseHowManyTwilightTokensToSpot());
        effectAppenderProducers.put("chooseopponent", new ChooseOpponent());
        effectAppenderProducers.put("chooseyesorno", new ChooseYesOrNo());
        effectAppenderProducers.put("consumesurplusdamage", new ConsumeSurplusDamage());
        effectAppenderProducers.put("corruptringbearer", new CorruptRingBearer());
        effectAppenderProducers.put("disablearcherytotalcontribution", new DisableArcheryTotalContribution());
        effectAppenderProducers.put("disableskirmishassignment", new DisableSkirmishAssignment());
        effectAppenderProducers.put("disablewounds", new DisableWounds());
        effectAppenderProducers.put("disablewoundsover", new DisableWoundsOver());
        effectAppenderProducers.put("discard", new DiscardFromPlay());
        effectAppenderProducers.put("discardbottomcardsfromdeck", new DiscardBottomCardsFromDeck());
        effectAppenderProducers.put("discardcardatrandomfromhand", new DiscardCardAtRandomFromHand());
        effectAppenderProducers.put("discardcardsfromdrawdeck", new DiscardCardsFromDrawDeck());
        effectAppenderProducers.put("discardfromhand", new DiscardFromHand());
        effectAppenderProducers.put("discardstackedcards", new DiscardStackedCards());
        effectAppenderProducers.put("discardtopcardsfromdeck", new DiscardTopCardsFromDeck());
        effectAppenderProducers.put("drawcards", new DrawCards());
        effectAppenderProducers.put("enableparticipationinarcheryfireandskirmishes", new EnableParticipationInArcheryFireAndSkirmishes());
        effectAppenderProducers.put("enableparticipationinskirmishes", new EnableParticipationInSkirmishes());
        effectAppenderProducers.put("endphase", new EndPhase());
        effectAppenderProducers.put("exchangecardsinhandwithcardsindeadpile", new ExchangeCardsInHandWithCardsInDeadPile());
        effectAppenderProducers.put("exchangecardsinhandwithcardsindiscard", new ExchangeCardsInHandWithCardsInDiscard());
        effectAppenderProducers.put("exchangecardsinhandwithcardsstacked", new ExchangeCardsInHandWithCardsStacked());
        effectAppenderProducers.put("exchangesite", new ExchangeSite());
        effectAppenderProducers.put("exert", new Exert());
        effectAppenderProducers.put("exhaust", new Exhaust());
        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck());
        effectAppenderProducers.put("heal", new Heal());
        effectAppenderProducers.put("hinder", new HinderCardsInPlay());
        effectAppenderProducers.put("incrementperphaselimit", new IncrementPerPhaseLimit());
        effectAppenderProducers.put("incrementperturnlimit", new IncrementPerTurnLimit());
        effectAppenderProducers.put("incrementtoil", new IncrementToil());
        effectAppenderProducers.put("kill", new Kill());
        effectAppenderProducers.put("liberatesite", new LiberateSite());
        effectAppenderProducers.put("lookathand", new LookAtHand());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new LookAtRandomCardsFromHand());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new LookAtTopCardsOfDrawDeck());
        effectAppenderProducers.put("makeselfringbearer", new MakeSelfRingBearer());
        effectAppenderProducers.put("memorize", new MemorizeActive());
        effectAppenderProducers.put("memorizeactive", new MemorizeActive());
        effectAppenderProducers.put("memorizediscard", new MemorizeDiscard());
        effectAppenderProducers.put("memorizeinfo", new MemorizeInfo());
        effectAppenderProducers.put("memorizenumber", new MemorizeNumber());
        effectAppenderProducers.put("memorizestacked", new MemorizeStacked());
        effectAppenderProducers.put("memorizetitle", new MemorizeTitle());
        effectAppenderProducers.put("memorizetopofdeck", new MemorizeTopOfDeck());
        effectAppenderProducers.put("modifyarcherytotal", new ModifyArcheryTotal());
        effectAppenderProducers.put("modifyresistance", new ModifyResistance());
        effectAppenderProducers.put("modifysitenumber", new ModifySiteNumber());
        effectAppenderProducers.put("modifystrength", new ModifyStrength());
        effectAppenderProducers.put("negatewound", new NegateWound());
        effectAppenderProducers.put("placenowoundforexert", new PlaceNoWoundForExert());
        effectAppenderProducers.put("playcardfromhand", new PlayCardFromHand());
        effectAppenderProducers.put("playcardfromdeadpile", new PlayCardFromDeadPile());
        effectAppenderProducers.put("playcardfromdiscard", new PlayCardFromDiscard());
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck());
        effectAppenderProducers.put("playcardfromstacked", new PlayCardFromStacked());
        effectAppenderProducers.put("playnextsite", new PlayNextSite());
        effectAppenderProducers.put("playsite", new PlaySite());
        effectAppenderProducers.put("preventaddingallburdens", new PreventAddingAllBurdens());
        effectAppenderProducers.put("preventallwounds", new PreventAllWounds());
        effectAppenderProducers.put("preventburden", new PreventEffect());
        effectAppenderProducers.put("preventeffect", new PreventEffect());
        effectAppenderProducers.put("preventtwilight", new PreventEffect());
        effectAppenderProducers.put("preventdiscard", new PreventCardEffectAppender());
        effectAppenderProducers.put("preventexert", new PreventCardEffectAppender());
        effectAppenderProducers.put("preventheal", new PreventCardEffectAppender());
        effectAppenderProducers.put("preventspecialability", new PreventSpecialAbility());
        effectAppenderProducers.put("preventwound", new PreventWound());
        effectAppenderProducers.put("putcardsfromdeckintohand", new PutCardsFromDeckIntoHand());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new PutCardsFromDeckOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new PutCardsFromDeckOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromdiscardintohand", new PutCardsFromDiscardIntoHand());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new PutCardsFromDiscardOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new PutCardsFromDiscardOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new PutCardsFromHandOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new PutCardsFromHandOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new PutCardsFromPlayOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromplayontopofdeck", new PutCardsFromPlayOnTopOfDeck());
        effectAppenderProducers.put("putonring", new PutOnRing());
        effectAppenderProducers.put("putplayedeventintohand", new PutPlayedEventIntoHand());
        effectAppenderProducers.put("putplayedeventonbottomofdrawdeck", new PutPlayedEventOnBottomOfDrawDeck());
        effectAppenderProducers.put("putplayedeventontopofdrawdeck", new PutPlayedEventOnTopOfDrawDeck());
        effectAppenderProducers.put("putrandomcardfromhandbeneathdrawdeck", new PutRandomCardFromHandBeneathDrawDeck());
        effectAppenderProducers.put("putstackedcardsintohand", new PutStackedCardsIntoHand());
        effectAppenderProducers.put("reconcilehand", new ReconcileHand());
        effectAppenderProducers.put("reinforcetokens", new ReinforceTokens());
        effectAppenderProducers.put("removealltokens", new RemoveAllTokens());
        effectAppenderProducers.put("removeburdens", new RemoveBurdens());
        effectAppenderProducers.put("removecardsindeadpilefromgame", new RemoveCardsInDeadPileFromGame());
        effectAppenderProducers.put("removecardsindeckfromgame", new RemoveCardsInDeckFromGame());
        effectAppenderProducers.put("removecardsindiscardfromgame", new RemoveCardsInDiscardFromGame());
        effectAppenderProducers.put("removecardsinhandfromgame", new RemoveCardsInHandFromGame());
        effectAppenderProducers.put("removecharacterfromskirmish", new RemoveCharacterFromSkirmish());
        effectAppenderProducers.put("removeculturetokens", new RemoveCultureTokens());
        effectAppenderProducers.put("removefromthegame", new RemoveFromTheGame());
        effectAppenderProducers.put("removeplayedeventfromthegame", new RemovePlayedEventFromTheGame());
        effectAppenderProducers.put("removekeyword", new RemoveKeyword());
        effectAppenderProducers.put("removetext", new RemoveText());
        effectAppenderProducers.put("removethreats", new RemoveThreats());
        effectAppenderProducers.put("removetokenscumulative", new RemoveTokensCumulative());
        effectAppenderProducers.put("removetwilight", new RemoveTwilight());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new ReorderTopCardsOfDrawDeck());
        effectAppenderProducers.put("replaceinskirmish", new ReplaceInSkirmish());
        effectAppenderProducers.put("resetwhileinzonedata", new ResetWhileInZoneData());
        effectAppenderProducers.put("restore", new RestoreCardsInPlay());
        effectAppenderProducers.put("returntohand", new ReturnToHand());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new RevealBottomCardsOfDrawDeck());
        effectAppenderProducers.put("revealcards", new RevealCardsFromPlay());
        effectAppenderProducers.put("revealcardsfromdiscard", new RevealCardsFromDiscard());
        effectAppenderProducers.put("revealcardsfromdrawdeck", new RevealCardsFromDrawDeck());
        effectAppenderProducers.put("revealcardsfromadventuredeck", new RevealCardsFromAdventureDeck());
        effectAppenderProducers.put("revealcardsfromhand", new RevealCardsFromHand());
        effectAppenderProducers.put("revealhand", new RevealHand());
        effectAppenderProducers.put("revealrandomcardsfromhand", new RevealRandomCardsFromHand());
        effectAppenderProducers.put("revealtopcardsofdrawdeck", new RevealTopCardsOfDrawDeck());
        effectAppenderProducers.put("setfpstrengthoverride", new SetFPStrengthOverride());
        effectAppenderProducers.put("setshadowstrengthoverride", new SetShadowStrengthOverride());
        effectAppenderProducers.put("setupextraassignmentandskirmishes", new SetupExtraAssignmentAndSkirmishes());
        effectAppenderProducers.put("shadowcanthaveinitiative", new ShadowCantHaveInitiative());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new ShuffleCardsFromDiscardIntoDrawDeck());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new ShuffleCardsFromHandIntoDrawDeck());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new ShuffleCardsFromPlayIntoDrawDeck());
        effectAppenderProducers.put("shuffledeck", new ShuffleDeck());
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleHandIntoDrawDeck());
        effectAppenderProducers.put("spot", new Spot());
        effectAppenderProducers.put("stackcardsfromplay", new StackCardsFromPlay());
        effectAppenderProducers.put("stackcardsfromdeck", new StackCardsFromDeck());
        effectAppenderProducers.put("stackcardsfromdiscard", new StackCardsFromDiscard());
        effectAppenderProducers.put("stackcardsfromhand", new StackCardsFromHand());
        effectAppenderProducers.put("stackplayedevent", new StackPlayedEvent());
        effectAppenderProducers.put("stacktopcardsofdrawdeck", new StackTopCardsOfDrawDeck());
        effectAppenderProducers.put("startskirmish", new StartSkirmish());
        effectAppenderProducers.put("storeculture", new StoreCulture());
        effectAppenderProducers.put("storekeyword", new StoreKeyword());
        effectAppenderProducers.put("storeracefromcard", new StoreRaceFromCard());
        effectAppenderProducers.put("storewhileinzone", new StoreWhileInZone());
        effectAppenderProducers.put("takecontrolofsite", new TakeControlOfSite());
        effectAppenderProducers.put("takeoffring", new TakeOffRing());
        effectAppenderProducers.put("transfer", new Transfer());
        effectAppenderProducers.put("transferfromdiscard", new TransferFromDiscard());
        effectAppenderProducers.put("transfertosupport", new TransferToSupport());
        effectAppenderProducers.put("turnintominion", new TurnIntoMinion());
        effectAppenderProducers.put("wound", new Wound());
    }

    public EffectAppender getEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(effectObject.get("type"), "type");
        boolean ignoreCostCheckFailure = FieldUtils.getBoolean(effectObject.get("ignoreCostCheckFailure"), "ignoreCostCheckFailure", false);
        final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
        if (effectAppenderProducer == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        return wrapIgnoreCost(ignoreCostCheckFailure, effectAppenderProducer.createEffectAppender(cost, effectObject, environment));
    }

    public EffectAppender[] getEffectAppenders(boolean cost, JSONObject[] effectArray, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        EffectAppender[] result = new EffectAppender[effectArray.length];
        for (int i = 0; i < result.length; i++) {
            final String type = FieldUtils.getString(effectArray[i].get("type"), "type");
            boolean ignoreCostCheckFailure = FieldUtils.getBoolean(effectArray[i].get("ignoreCostCheckFailure"), "ignoreCostCheckFailure", false);
            final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
            if (effectAppenderProducer == null)
                throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
            result[i] = wrapIgnoreCost(ignoreCostCheckFailure, effectAppenderProducer.createEffectAppender(cost, effectArray[i], environment));
        }
        return result;
    }

    private EffectAppender wrapIgnoreCost(boolean ignoreCostCheckFailure, EffectAppender effectAppender) {
        if (ignoreCostCheckFailure) {
            return new EffectAppender() {
                @Override
                public void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    effectAppender.appendEffect(cost, action, actionContext);
                }

                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    try {
                        return effectAppender.isPlayableInFull(actionContext);
                    } catch (Exception e) {
                        // Ignore any errors, as we can't evaluate at this time
                        return true;
                    }
                }

                @Override
                public boolean canPreEvaluate() {
                    return effectAppender.canPreEvaluate();
                }

                @Override
                public boolean isPlayabilityCheckedForEffect() {
                    return effectAppender.isPlayabilityCheckedForEffect();
                }
            };
        } else {
            return effectAppender;
        }
    }
}
