package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_338_Tests
{

    // The Ford is site 3, after Bree Gate at site 2 (Shadow number 2; its text needs a ranger).
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    // The Witch-king, Lord of Angmar: [wraith] Nazgûl, cost 8, site number 3; fierce, and strength for other Nazgûl
                    put("twk", "1_237");
                    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, cost 4, site number 3; her text needs a [wraith] condition
                    put("lemenya", "1_232");
                    // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, cost 4, site number 3; when played, for each
                    // companion over 4 her owner may play a minion from his discard pile
                    put("nertea", "1_234");
                    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, cost 6, site number 3; his text needs 3 burdens
                    put("khamul", "103_61");
                    // Morgul Creeper: a [wraith] Orc, cost 3, site number 4; its text needs 3 burdens
                    put("creeper", "8_75");

                    // Dwarf Guard: a companion with no text in play; four of them and Frodo are five companions
                    put("guard1", "1_7");
                    put("guard2", "1_7");
                    put("guard3", "1_7");
                    put("guard4", "1_7");
                }},
                new HashMap<>() {{
                    put("site1", "1_319");
                    put("site2", "1_327");
                    put("site3", "1_338");
                    put("site4", "1_343");
                    put("site5", "1_349");
                    put("site6", "1_351");
                    put("site7", "1_353");
                    put("site8", "1_356");
                    put("site9", "1_360");
                }},
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }

    // The Ford is site 3, after Bree Gate at site 2 (Shadow number 2; its text needs a ranger).
    protected VirtualTableScenario GetTwoMarkersScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    // Úlairë Attëa, Keeper of Dol Guldur: a Nazgûl of cost 6, site number 3; a skirmish ability only
                    put("attea", "1_229");
                }},
                new HashMap<>() {{
                    put("site1", "1_319");
                    put("site2", "1_327");
                    put("site3", "1_338");
                    put("site4", "1_343");
                    put("site5", "1_349");
                    put("site6", "1_351");
                    put("site7", "1_353");
                    put("site8", "1_356");
                    put("site9", "1_360");
                }},
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.RulingRing
        );
    }

    @Test
    public void FordofBruinenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Name: Ford of Bruinen
         * Unique: False
         * Type: Site
         * Block: Fellowship
         * Site Number: 3
         * Shadow Number: 0
         * Direction: Right
         * Game Text: <b>River</b>. <b>Sanctuary</b>. The twilight cost of the first Nazgûl played to Ford of Bruinen each turn is -5.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsSite(3);

        assertEquals("Ford of Bruinen", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(CardType.SITE, card.getBlueprint().getCardType());
        assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
        assertEquals(3, card.getBlueprint().getSiteNumber());
        assertEquals(0, card.getBlueprint().getTwilightCost());
        assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
        assertTrue(scn.HasKeyword(card, Keyword.RIVER));
        assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
    }

    @Test
    public void FordofBruinenOnlyDiscountsTheFirstNazgul() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var ford = scn.GetShadowSite(3);

        var twk = scn.GetShadowCard("twk");
        var lemenya = scn.GetShadowCard("lemenya");

        scn.StartGame();
        // The turn begins at site 2, so that the move to the Ford is its first.
        scn.SkipToSite(2);
        scn.SetShadowHand(twk, lemenya);

        scn.SetTwilight(9);
        scn.FreepsPass();

        assertSame(ford, scn.GetCurrentSite());
        // 9 set + 1 companion + 0 for the Ford
        assertEquals(10, scn.GetTwilight());
        assertTrue(scn.AwaitingShadowPhaseActions());
        // 8 - 5
        assertEquals(3, scn.GetTwilightCost(twk));
        // 4 - 5, which reads 0
        assertEquals(0, scn.GetTwilightCost(lemenya));

        scn.ShadowPlayCard(twk);
        // 3 to play, and no roaming: his site number is 3
        assertEquals(7, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, twk);

        // The second Nazgûl of the turn costs her printed 4.
        assertEquals(4, scn.GetTwilightCost(lemenya));
        scn.ShadowPlayCard(lemenya);
        assertEquals(3, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
        assertTrue(scn.AwaitingShadowPhaseActions());
    }

    @Test
    public void ANazgulOfAnotherCultureIsDiscountedAndAWraithMinionThatIsNoNazgulIsNeitherDiscountedNorCounted() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var twk = scn.GetShadowCard("twk");
        var khamul = scn.GetShadowCard("khamul");
        var creeper = scn.GetShadowCard("creeper");

        scn.StartGame();
        scn.SkipToSite(2);
        scn.SetShadowHand(twk, khamul, creeper);

        scn.SetTwilight(9);
        scn.FreepsPass();

        assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
        // 9 set + 1 companion + 0 for the Ford
        assertEquals(10, scn.GetTwilight());
        assertTrue(scn.AwaitingShadowPhaseActions());
        // 8 - 5 and 6 - 5; the Orc its printed 3
        assertEquals(3, scn.GetTwilightCost(twk));
        assertEquals(1, scn.GetTwilightCost(khamul));
        assertEquals(3, scn.GetTwilightCost(creeper));

        scn.ShadowPlayCard(creeper);
        // 3 to play + 2 roaming (site number 4, played at site 3)
        assertEquals(5, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, creeper);
        // No Nazgûl has been played yet.
        assertEquals(3, scn.GetTwilightCost(twk));
        assertEquals(1, scn.GetTwilightCost(khamul));

        scn.ShadowPlayCard(khamul);
        // 1 to play, and no roaming: his site number is 3
        assertEquals(4, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, khamul);
        // He was the first Nazgûl played to the Ford this turn.
        assertEquals(8, scn.GetTwilightCost(twk));
        assertTrue(scn.AwaitingShadowPhaseActions());
    }

    @Test
    public void FordDiscountsNerteaButNotNerteasTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var ford = scn.GetShadowSite(3);

        var twk = scn.GetShadowCard("twk");
        var nertea = scn.GetShadowCard("nertea");

        scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");

        scn.StartGame();
        // The turn begins at site 2, so that the move to the Ford is its first.
        scn.SkipToSite(2);
        scn.SetShadowHand(nertea);
        scn.MoveCardsToDiscard(twk);

        scn.SetTwilight(5);
        scn.FreepsPass();

        assertSame(ford, scn.GetCurrentSite());
        // 5 set + 5 companions + 0 for the Ford
        assertEquals(10, scn.GetTwilight());
        assertTrue(scn.AwaitingShadowPhaseActions());
        // 4 - 5, which reads 0
        assertEquals(0, scn.GetTwilightCost(nertea));
        assertInZone(Zone.DISCARD, twk);

        scn.ShadowPlayCard(nertea);
        assertEquals(10, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, nertea);

        // Five companions: Nertëa's own text offers one minion from the discard pile.  Taking the offer is the
        // play, and the Witch-king is the one minion there.  Nertëa was the first Nazgûl of the turn, so he costs
        // his printed 8.
        assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
        scn.ShadowAcceptOptionalTrigger();

        assertInZone(Zone.SHADOW_CHARACTERS, twk);
        assertEquals(2, scn.GetTwilight());
        assertTrue(scn.AwaitingShadowPhaseActions());
    }

    @Test
    public void TheFirstNazgulPlayedToTheFordOnTheNextTurnIsFiveCheaperAgain() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        // Each player's own Nazgûl, for the turn on which that player is the Shadow player
        var firstPlayersKing = scn.GetFreepsCard("twk");
        var firstPlayersLemenya = scn.GetFreepsCard("lemenya");
        var secondPlayersKing = scn.GetShadowCard("twk");
        var secondPlayersLemenya = scn.GetShadowCard("lemenya");

        // The first player's fellowship moves twice in its first turn and comes to rest on the Ford.
        scn.StartGame();
        scn.FreepsPass();
        scn.SkipToMovementDecision();
        scn.FreepsChooseToMove();
        assertTrue(scn.AwaitingShadowPhaseActions());
        assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());

        scn.SetShadowHand(secondPlayersLemenya, secondPlayersKing);
        scn.ShadowRefreshDecision();
        assertEquals(3, scn.GetTwilightCost(secondPlayersKing));
        int pool = scn.GetTwilight();

        // Lemenya is this turn's first: 4 - 5, played for nothing.
        scn.ShadowPlayCard(secondPlayersLemenya);
        assertEquals(pool, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, secondPlayersLemenya);
        assertEquals(8, scn.GetTwilightCost(secondPlayersKing));

        // The second player's turn.  His fellowship moves from site 1 to site 2 and on to the same Ford.
        scn.SkipToOtherPlayersTurn();
        assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
        scn.ShadowPass();
        scn.SkipToMovementDecision();
        scn.SetFreepsHand(firstPlayersKing, firstPlayersLemenya);
        scn.SetTwilight(5);
        scn.ShadowChooseToMove();

        assertEquals(3, scn.GetCurrentSiteNumber());
        assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
        assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
        // 5 set + 1 companion + 0 for the Ford
        assertEquals(6, scn.GetTwilight());
        // A new turn: 8 - 5 once more.
        assertEquals(3, scn.GetTwilightCost(firstPlayersKing));

        scn.FreepsPlayCard(firstPlayersKing);
        assertEquals(3, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, firstPlayersKing);
        assertEquals(4, scn.GetTwilightCost(firstPlayersLemenya));
        assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
    }

    @Test
    public void ANazgulPlayedWhileTheFellowshipIsAtAnotherSiteIsNotDiscountedThoughTheFordIsOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetTwoMarkersScenario();

        // Each player's own Attëa, for the turn on which that player is the Shadow player
        var firstPlayersAttea = scn.GetFreepsCard("attea");
        var secondPlayersAttea = scn.GetShadowCard("attea");

        // The first player's fellowship moves twice in its first turn and comes to rest on the Ford.
        scn.StartGame();
        scn.FreepsPass();
        scn.SkipToMovementDecision();
        scn.FreepsChooseToMove();
        assertTrue(scn.AwaitingShadowPhaseActions());
        assertEquals(3, scn.GetCurrentSiteNumber());
        assertEquals("Ford of Bruinen", scn.GetCurrentSite().getBlueprint().getTitle());
        // The control: played to the Ford, a Nazgûl would cost 6 - 5.
        scn.MoveCardsToHand(secondPlayersAttea);
        assertEquals(1, scn.GetTwilightCost(secondPlayersAttea));

        // The second player's turn.  His fellowship moves from site 1 to site 2; the first player's marker
        // stands on the Ford, a site ahead.
        scn.SkipToOtherPlayersTurn();
        assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
        scn.MoveCardsToHand(firstPlayersAttea);
        scn.SetTwilight(7);
        scn.ShadowPass();

        assertEquals(2, scn.GetCurrentSiteNumber());
        // 7 set + 1 companion + 2 for Bree Gate
        assertEquals(10, scn.GetTwilight());
        assertEquals(6, scn.GetTwilightCost(firstPlayersAttea));
        assertTrue(scn.FreepsPlayAvailable(firstPlayersAttea));

        scn.FreepsPlayCard(firstPlayersAttea);
        // 6 to play + 2 roaming (site number 3, played at site 2), and nothing off for the Ford
        assertEquals(2, scn.GetTwilight());
        assertInZone(Zone.SHADOW_CHARACTERS, firstPlayersAttea);
        assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
    }
}
