package com.gempukku.lotro.at;

import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.effect.requirement.*;
import com.gempukku.lotro.common.Phase;
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
    public void hasInMemory() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("memory", "m");

        DefaultActionContext actionContext = new DefaultActionContext(P1, _game, null, null, null);
        Requirement requirement = new HasCardInMemory().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(actionContext));
        actionContext.setCardMemory("m", createCard(P1, "1_3"));
        assertTrue(requirement.accepts(actionContext));
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
    public void playerLeaguePlacementOutsideALeague() throws Exception {
        initializeSimplestGame();

        passUntil(Phase.FELLOWSHIP);

        JSONObject obj = new JSONObject();
        obj.put("player", "you");
        obj.put("percentage", 10);

        Requirement requirement = new PlayerLeaguePlacement().getPlayRequirement(obj, lotroCardBlueprintBuilder);
        assertFalse(requirement.accepts(new DefaultActionContext(P1, _game, null, null, null)));
    }
}
