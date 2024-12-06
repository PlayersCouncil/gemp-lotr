package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChooseAtTest extends AbstractAtTest {
    @Test
    public void chooseHowManyBurdensToSpot() throws Exception {
        initializeSimplestGame();

        PhysicalCard enduringEvil = addToZone(createCard(P2, "1_246"), Zone.HAND);
        PhysicalCard orgInquisitor = addToZone(createCard(P2, "1_268"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + orgInquisitor.getCardId());
        selectCard(P1, ringBearer);
        pass(P1);
        selectCardAction(P2, enduringEvil);
        AwaitingDecision awaitingDecision = getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.INTEGER, awaitingDecision.getDecisionType());
        validateContents(new String[]{"0"}, awaitingDecision.getDecisionParameters().get("min"));
        validateContents(new String[]{"1"}, awaitingDecision.getDecisionParameters().get("max"));
        playerDecided(P2, "1");
    }

    @Test
    public void chooseHowManyFPCulturesToSpot() throws Exception {
        initializeSimplestGame();

        PhysicalCard mercilessBerserker = addToZone(createCard(P2, "15_165"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, mercilessBerserker);
        AwaitingDecision awaitingDecision = getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.INTEGER, awaitingDecision.getDecisionType());
        validateContents(new String[]{"0"}, awaitingDecision.getDecisionParameters().get("min"));
        validateContents(new String[]{"1"}, awaitingDecision.getDecisionParameters().get("max"));
        playerDecided(P2, "1");
    }

    @Test
    public void chooseHowManyThreatsToSpot() throws Exception {
        initializeSimplestGame();

        PhysicalCard gollum = addToZone(createCard(P2, "7_58"), Zone.DISCARD);
        PhysicalCard soPolite = addToZone(createCard(P2, "7_74"), Zone.HAND);

        addThreats(P1, 1);

        passUntil(Phase.FELLOWSHIP);
        setTwilightPool(10);

        passUntil(Phase.SHADOW);
        selectCardAction(P2, soPolite);
        AwaitingDecision awaitingDecision = getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.INTEGER, awaitingDecision.getDecisionType());
        validateContents(new String[]{"0"}, awaitingDecision.getDecisionParameters().get("min"));
        validateContents(new String[]{"1"}, awaitingDecision.getDecisionParameters().get("max"));
        playerDecided(P2, "1");
    }

    @Test
    public void chooseHowManyTwilightTokensToSpot() throws Exception {
        initializeSimplestGame();

        PhysicalCard underTheLivingEarth = addToZone(createCard(P1, "4_105"), Zone.HAND);
        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gandalf.getCardId() + " " + goblinRunner.getCardId());
        selectCard(P1, gandalf);
        selectCardAction(P1, underTheLivingEarth);
        AwaitingDecision awaitingDecision = getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.INTEGER, awaitingDecision.getDecisionType());
        validateContents(new String[]{"0"}, awaitingDecision.getDecisionParameters().get("min"));
        validateContents(new String[]{"4"}, awaitingDecision.getDecisionParameters().get("max"));
        playerDecided(P1, "4");
    }
}
