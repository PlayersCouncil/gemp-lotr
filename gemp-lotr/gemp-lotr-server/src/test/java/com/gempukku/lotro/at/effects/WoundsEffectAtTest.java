package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.NegateWoundEffect;
import com.gempukku.lotro.logic.effects.PreventCardEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.SpecialFlagModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class WoundsEffectAtTest extends AbstractAtTest {
    @Test
    public void woundSuccessful() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));

        final AtomicInteger triggerCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.forEachWounded(game, effectResult, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        WoundCharactersEffect woundEffect = new WoundCharactersEffect(merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, woundEffect);

        assertEquals(1, _game.getGameState().getWounds(merry));
        assertTrue(woundEffect.wasCarriedOut());

        assertEquals(1, triggerCount.get());
    }

    @Test
    public void woundUnsuccessful() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));

        final AtomicInteger triggerCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.forEachWounded(game, effectResult, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        _game.getGameState().addCardToZone(_game, merry, Zone.DISCARD);

        WoundCharactersEffect woundEffect = new WoundCharactersEffect(merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, woundEffect);

        assertEquals(0, _game.getGameState().getWounds(merry));
        assertFalse(woundEffect.wasCarriedOut());

        assertEquals(0, triggerCount.get());
    }

    @Test
    public void woundPrevention() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));
        final PhysicalCardImpl pippin = new PhysicalCardImpl(102, "1_306", P1, _cardLibrary.getLotroCardBlueprint("1_306"));

        final AtomicInteger triggerCount = new AtomicInteger(0);
        final AtomicInteger preventCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
                        if (TriggerConditions.isGettingWounded(effect, game, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new PreventCardEffect((WoundCharactersEffect) effect, merry));
                            action.appendEffect(
                                    new IncrementEffect(preventCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.forEachWounded(game, effectResult, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);

        WoundCharactersEffect woundEffect = new WoundCharactersEffect(merry, merry, pippin);

        carryOutEffectInPhaseActionByPlayer(P1, woundEffect);

        assertEquals(0, _game.getGameState().getWounds(merry));
        assertEquals(1, _game.getGameState().getWounds(pippin));
        assertFalse(woundEffect.wasCarriedOut());

        assertEquals(0, triggerCount.get());
        assertEquals(1, preventCount.get());
    }

    @Test
    public void insteadOfWoundWorksIfCantPrevent() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));

        final AtomicInteger triggerCount = new AtomicInteger(0);
        final AtomicInteger negateCount = new AtomicInteger(0);

        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new SpecialFlagModifier(null, null, ModifierFlag.CANT_PREVENT_WOUNDS));

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
                        if (TriggerConditions.isGettingWounded(effect, game, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new NegateWoundEffect((WoundCharactersEffect) effect, merry));
                            action.appendEffect(
                                    new IncrementEffect(negateCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.forEachWounded(game, effectResult, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        WoundCharactersEffect woundEffect = new WoundCharactersEffect(merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, woundEffect);

        assertEquals(0, _game.getGameState().getWounds(merry));
        assertFalse(woundEffect.wasCarriedOut());

        assertEquals(0, triggerCount.get());
        assertEquals(1, negateCount.get());
    }

    @Test
    public void cantPrevent() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl merry = new PhysicalCardImpl(101, "1_303", P1, _cardLibrary.getLotroCardBlueprint("1_303"));

        final AtomicInteger triggerCount = new AtomicInteger(0);
        final AtomicInteger preventCount = new AtomicInteger(0);

        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new SpecialFlagModifier(null, null, ModifierFlag.CANT_PREVENT_WOUNDS));

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
                        if (TriggerConditions.isGettingWounded(effect, game, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new PreventCardEffect((WoundCharactersEffect) effect, merry));
                            action.appendEffect(
                                    new IncrementEffect(preventCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.forEachWounded(game, effectResult, merry)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        WoundCharactersEffect woundEffect = new WoundCharactersEffect(merry, merry);

        carryOutEffectInPhaseActionByPlayer(P1, woundEffect);

        assertEquals(1, _game.getGameState().getWounds(merry));
        assertTrue(woundEffect.wasCarriedOut());

        assertEquals(1, triggerCount.get());
        assertEquals(1, preventCount.get());
    }

    @Test
    public void disableWoundsOver() throws Exception {
        initializeSimplestGame();

        PhysicalCard valiantManOfTheWest = addToZone(createCard(P1, "1_118"), Zone.HAND);
        PhysicalCard aragorn = addToZone(createCard(P1, "1_89"), Zone.FREE_CHARACTERS);
        PhysicalCard lurtz = addToZone(createCard(P2, "1_127"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, valiantManOfTheWest);
        pass(P2);
        pass(P1);
        passUntil(Phase.ARCHERY);
        pass(P1);
        pass(P2);
        selectCard(P1, aragorn);
        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, aragorn.getCardId() + " " + lurtz.getCardId());
        selectCard(P1, aragorn);
        pass(P1);
        assertEquals(1, getWounds(aragorn));
        pass(P2);
        assertEquals(2, getWounds(aragorn));
    }

    @Test
    public void preventAllWounds() throws Exception {
        Map<String, LotroDeck> decks = new HashMap<>();
        decks.put(P1, createDeckWithMrUnderhill());
        decks.put(P2, createDeckWithMrUnderhill());
        initializeGameWithDecks(decks);

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard keepYourForkedTongue = addToZone(createCard(P1, "4_96"), Zone.HAND);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(5);
        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        selectCard(P1, ringBearer);
        selectCardAction(P1, keepYourForkedTongue);
        pass(P2);
        pass(P1);
        assertEquals(0, getWounds(ringBearer));
    }

    public LotroDeck createDeckWithMrUnderhill() {
        LotroDeck lotroDeck = new LotroDeck("Some deck");
        // 10_121,1_2
        lotroDeck.setRingBearer("0_67");
        lotroDeck.setRing("1_2");
        // 7_330,7_336,8_117,7_342,7_345,7_350,8_120,10_120,7_360
        lotroDeck.addSite("7_330");
        lotroDeck.addSite("7_335");
        lotroDeck.addSite("8_117");
        lotroDeck.addSite("7_342");
        lotroDeck.addSite("7_345");
        lotroDeck.addSite("7_350");
        lotroDeck.addSite("8_120");
        lotroDeck.addSite("10_120");
        lotroDeck.addSite("7_360");
        return lotroDeck;
    }
}
