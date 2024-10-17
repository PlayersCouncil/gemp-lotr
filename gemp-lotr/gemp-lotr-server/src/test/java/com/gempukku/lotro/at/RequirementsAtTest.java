package com.gempukku.lotro.at;

import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.effect.requirement.*;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.json.simple.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequirementsAtTest extends AbstractAtTest {
    private LotroCardBlueprintBuilder lotroCardBlueprintBuilder = new LotroCardBlueprintBuilder();

    @Test
    public void canSpotCultureTokens() throws Exception {
        initializeSimplestGame();

        PhysicalCard elvenDefender = addToZone(createCard(P1, "18_9"), Zone.FREE_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(5, getStrength(elvenDefender));
        addCultureTokens(elvenDefender, Token.ELVEN, 1);
        assertEquals(7, getStrength(elvenDefender));
    }

    @Test
    public void canSpotAnyCultureTokens() throws Exception {
        initializeSimplestGame();

        PhysicalCard cruelDunlending = addToZone(createCard(P2, "13_85"), Zone.SHADOW_CHARACTERS);
        PhysicalCard desertWind = addToZone(createCard(P2, "13_86"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(10);
        passUntil(Phase.SHADOW);
        selectCardAction(P2, desertWind);

        passUntil(Phase.ARCHERY);
        assertEquals(0, getArcheryTotal(Side.SHADOW));
        addCultureTokens(desertWind, Token.MEN, 5);
        assertEquals(2, getArcheryTotal(Side.SHADOW));
    }

    @Test
    public void cantSpotBurdens() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("amount", 2);
        Requirement requirement = new CantSpotBurdens().getPlayRequirement(obj, lotroCardBlueprintBuilder);

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertTrue(requirement.accepts(actionContext));
        addBurdens(1);
        assertFalse(requirement.accepts(actionContext));
    }

    @Test
    public void cantSpotThreats() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("amount", 2);
        Requirement requirement = new CantSpotThreats().getPlayRequirement(obj, lotroCardBlueprintBuilder);

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertTrue(requirement.accepts(actionContext));
        addThreats(P1, 2);
        assertFalse(requirement.accepts(actionContext));
    }

    @Test
    public void cardsInDeckCount() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("deck", "fp");
        obj.put("count", 1);

        Requirement requirement = new CardsInDeckCount().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertFalse(requirement.accepts(actionContext));
        putOnTopOfDeck(createCard(P1, "1_3"));
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void isAhead() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        Requirement requirement = new IsAhead().getPlayRequirement(new JSONObject(), lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertFalse(requirement.accepts(actionContext));
        passUntil(Phase.SHADOW);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void isEqual() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("firstNumber", 2);
        obj.put("secondNumber", "burdenCount");

        Requirement requirement = new IsEqual().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertFalse(requirement.accepts(actionContext));
        addBurdens(1);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void isGreaterThan() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("firstNumber", 2);
        obj.put("secondNumber", "burdenCount");

        Requirement requirement = new IsGreaterThan().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertTrue(requirement.accepts(actionContext));
        addBurdens(1);
        assertFalse(requirement.accepts(actionContext));
    }

    @Test
    public void isGreaterThanOrEqual() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("firstNumber", 2);
        obj.put("secondNumber", "burdenCount");

        Requirement requirement = new IsGreaterThanOrEqual().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertTrue(requirement.accepts(actionContext));
        addBurdens(1);
        assertTrue(requirement.accepts(actionContext));
        addBurdens(1);
        assertFalse(requirement.accepts(actionContext));
    }

    @Test
    public void isLessThan() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("firstNumber", 2);
        obj.put("secondNumber", "burdenCount");

        Requirement requirement = new IsLessThan().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertFalse(requirement.accepts(actionContext));
        addBurdens(1);
        assertFalse(requirement.accepts(actionContext));
        addBurdens(1);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void isLessThanOrEqual() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("firstNumber", 2);
        obj.put("secondNumber", "burdenCount");

        Requirement requirement = new IsLessThanOrEqual().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        assertFalse(requirement.accepts(actionContext));
        addBurdens(1);
        assertTrue(requirement.accepts(actionContext));
        addBurdens(1);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void isOwner() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();

        PhysicalCard ownerByP1 = createCard(P1, "1_3");
        PhysicalCard ownerByP2 = createCard(P2, "1_3");

        Requirement requirement = new IsOwnerRequirementProducer().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertTrue(requirement.accepts(new DefaultActionContext(P1, _game, ownerByP1, null, null)));
        assertFalse(requirement.accepts(new DefaultActionContext(P1, _game, ownerByP2, null, null)));
    }

    @Test
    public void didWinSkirmish() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard stillTwitching = attachTo(createCard(P1, "19_3"), gimli);

        passUntil(Phase.REGROUP);
        pass(P1);
        pass(P2);
        selectNo(P1);
        assertInZone(stillTwitching, Zone.DISCARD);
    }

    @Test
    public void hasCardInAdventureDeck() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("player", "fp");
        obj.put("filter", "any");

        Requirement requirement = new HasCardInAdventureDeck().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertTrue(requirement.accepts(new DefaultActionContext(P1, _game, null, null, null)));
    }

    @Test
    public void hasCardInRemoved() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("player", "fp");
        obj.put("filter", "any");

        Requirement requirement = new HasCardInRemoved().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(new DefaultActionContext(P1, _game, null, null, null)));
        addToZone(createCard(P1, "1_3"), Zone.REMOVED);
        assertTrue(requirement.accepts(new DefaultActionContext(P1, _game, null, null, null)));
    }

    @Test
    public void hasInMemory() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("memory", "m");

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new HasInMemory().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(actionContext));
        actionContext.setCardMemory("m", createCard(P1, "1_3"));
        assertTrue(requirement.accepts(actionContext));
    }


    @Test
    public void loseSkirmishThisTurn() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard citadelOfMinasTirith = addToZone(createCard(P1, "3_40"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        addWounds(gimli, 1);

        passUntil(Phase.REGROUP);
        pass(P1);
        pass(P2);
        selectNo(P1);
        selectCardAction(P1, citadelOfMinasTirith);
        assertEquals(0, getWounds(gimli));
    }

    @Test
    public void opponentDoesNotControlSite() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new OpponentDoesNotControlSite().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertTrue(requirement.accepts(actionContext));
        _game.getGameState().takeControlOfCard(P2, _game, _game.getGameState().getSite(1), Zone.SUPPORT);
        assertFalse(requirement.accepts(actionContext));
    }

    @Test
    public void playedCardThisPhase() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("filter", "companion");

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new PlayedCardThisPhase().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(actionContext));
        selectCardAction(P1, gimli);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void shadowPlayerReplacedCurrentSite() throws Exception {
        initializeSimplestGame();

        PhysicalCard nelya = addToZone(createCard(P2, "11_222"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.SHADOW);

        JSONObject obj = new JSONObject();

        replaceSite(_game.getGameState().getAdventureDeck(P1).get(0), 2);
        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new ShadowPlayerReplacedCurrentSite().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(actionContext));
        selectCardAction(P2, nelya);
        assertTrue(requirement.accepts(actionContext));
    }

    @Test
    public void wasAssignedToSkirmish() throws Exception {
        initializeSimplestGame();

        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);
        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);

        JSONObject obj = new JSONObject();
        obj.put("filter", "companion");

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new WasAssignedToSkirmish().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(actionContext));
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        assertTrue(requirement.accepts(actionContext));
    }
}
