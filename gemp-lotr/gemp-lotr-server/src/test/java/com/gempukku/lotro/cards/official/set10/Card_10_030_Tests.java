package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_030_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("endgame", "10_30");
                    put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability only
                    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3, vitality 4.  Enduring:
                    // strength +2 for each wound on him.  His skirmish ability exerts him twice, so it is never on
                    // offer while he is exhausted.
                    put("pippin", "103_31");
                    put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6, vitality 3; "Knight." is its whole text
                    // Gondorian Merchant: the only [gondor] ally, vitality 1 and so always exhausted; a maneuver
                    // ability that needs initiative
                    put("merchant", "7_97");

                    // Troop of Uruk-hai: strength 9, vitality 4, damage +1; a character skirmishing it gains no
                    // strength from weapons, and nobody here bears one
                    put("troop", "1_143");
                    put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
                    put("savage2", "1_151");
                    put("patrol", "1_177");     // Goblin Patrol Troop: strength 13, no text
                    // Black Breath: a [wraith] condition that a character skirmishing a Nazgul comes to bear:
                    // "Burdens and wounds may not be removed from bearer."
                    put("breath", "1_207");
                }}
		);
	}

	@Test
	public void EndoftheGameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: End of the Game
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
         * Game Text: Make an exhausted [gondor] companion strength +2. If that companion wins this skirmish, heal that
         *   companion or make him or her <b>damage +1</b>.
		*/

		var scn = GetScenario();

        var card = scn.GetFreepsCard("endgame");

		assertEquals("End of the Game", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

    @Test
    public void MakesAnExhaustedGondorCompanionStrengthPlus2AndWhenHeWinsTheSkirmishHeMayBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var pippin = scn.GetFreepsCard("pippin");
        var knight = scn.GetFreepsCard("knight");
        var merchant = scn.GetFreepsCard("merchant");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn, pippin, knight);
        scn.MoveAlliesToTable(merchant);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");

        scn.StartGame();
        // Exhausted: Aragorn and Pippin (3 wounds of 4 vitality) and Frodo, who is not [gondor]; the Merchant has
        // 1 vitality.  The Knight, with 1 wound of 3 vitality, is one wound short of exhausted.
        scn.AddWoundsToChar(aragorn, 3);
        scn.AddWoundsToChar(pippin, 3);
        scn.AddWoundsToChar(frodo, 3);
        scn.AddWoundsToChar(knight, 1);
        scn.MoveMinionsToTable(troop);

        // A skirmish event: not playable in the assignment phase.
        scn.SkipToPhase(Phase.ASSIGNMENT);
        assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
        assertFalse(scn.FreepsPlayAvailable(endgame));

        scn.PassAssignmentActions();
        scn.FreepsAssignAndResolve(aragorn, troop);
        assertTrue(scn.FreepsPlayAvailable(endgame));
        assertEquals(8, scn.GetStrength(aragorn));
        // 3 + 2 for each of his 3 wounds (enduring)
        assertEquals(9, scn.GetStrength(pippin));

        scn.FreepsPlayCard(endgame);
        // An exhausted [gondor] companion of any race, fighting or not: not the Knight, who is not exhausted, nor
        // Frodo, who is not [gondor], nor the Merchant, who is an ally.
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
        assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo, merchant));
        assertEquals(1, scn.FreepsGetChoiceMin());
        assertEquals(1, scn.FreepsGetChoiceMax());

        scn.FreepsChooseCard(aragorn);
        assertEquals(10, scn.GetStrength(aragorn));
        assertEquals(9, scn.GetStrength(pippin));
        assertInZone(Zone.DISCARD, endgame);

        scn.ShadowPass();
        assertEquals(10, scn.GetStrength(aragorn));
        assertEquals(3, scn.GetWoundsOn(aragorn));
        assertEquals(0, scn.GetWoundsOn(troop));

        // Aragorn, 10, beats the Troop, 9; at 8 he would have lost.  His win is answered beside the wound the
        // Troop takes for losing.
        scn.FreepsPass();
        scn.FreepsResolveActionOrder("End of the Game");
        assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(2, scn.FreepsGetMultipleChoiceCount());
        assertEquals(3, scn.GetWoundsOn(aragorn));

        scn.FreepsChooseOption("Heal");
        // One wound healed, and no damage bonus: the Troop takes the one wound of losing.
        assertEquals(2, scn.GetWoundsOn(aragorn));
        assertEquals(1, scn.GetWoundsOn(troop));
        // The other exhausted [gondor] companion is not healed.
        assertEquals(3, scn.GetWoundsOn(pippin));
        assertEquals(3, scn.GetWoundsOn(frodo));
        // The skirmish is over, and so is the bonus.
        assertEquals(8, scn.GetStrength(aragorn));
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
    }

    @Test
    public void WhenHeWinsTheSkirmishHeMayBeMadeDamagePlus1ForThatSkirmishInstead() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var pippin = scn.GetFreepsCard("pippin");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn, pippin);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        var savage = scn.GetShadowCard("savage1");

        scn.StartGame();
        scn.AddWoundsToChar(aragorn, 3);
        scn.AddWoundsToChar(pippin, 3);
        scn.MoveMinionsToTable(troop, savage);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{pippin, savage});
        scn.FreepsResolveSkirmish(aragorn);
        assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));

        scn.FreepsPlayCard(endgame);
        scn.FreepsChooseCard(aragorn);
        assertEquals(10, scn.GetStrength(aragorn));
        scn.ShadowPass();
        scn.FreepsPass();

        // His win is answered before the Troop's wounds are placed.
        scn.FreepsResolveActionOrder("End of the Game");
        assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(0, scn.GetWoundsOn(troop));

        scn.FreepsChooseOption("damage +1");
        // He is not healed, and the Troop takes 1 wound for losing + 1 for the damage bonus.
        assertEquals(3, scn.GetWoundsOn(aragorn));
        assertEquals(1 + 1, scn.GetWoundsOn(troop));
        assertEquals(3, scn.GetWoundsOn(pippin));

        // In the next skirmish neither bonus is left.
        scn.FreepsResolveSkirmish(pippin);
        assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
        assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
        assertFalse(scn.HasKeyword(pippin, Keyword.DAMAGE));
        assertEquals(8, scn.GetStrength(aragorn));
    }

    @Test
    public void AWinnerWhoseWoundsMayNotBeRemovedIsMadeDamagePlus1WithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var troop = scn.GetShadowCard("troop");
        var breath = scn.GetShadowCard("breath");

        scn.StartGame();
        scn.AddWoundsToChar(aragorn, 3);
        scn.AttachCardsTo(aragorn, breath);
        scn.MoveMinionsToTable(troop);

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(aragorn, troop);
        // Aragorn is the only exhausted [gondor] companion, so he is chosen without asking.
        scn.FreepsPlayCard(endgame);
        assertEquals(10, scn.GetStrength(aragorn));
        scn.ShadowPass();
        scn.FreepsPass();

        // He cannot be healed, so the choice is not his to make: the other thing is what happens.
        scn.FreepsResolveActionOrder("End of the Game");
        assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(3, scn.GetWoundsOn(aragorn));
        // 1 wound for losing + 1 for the damage bonus
        assertEquals(1 + 1, scn.GetWoundsOn(troop));
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
    }

    @Test
    public void ThatCompanionLosingTheSkirmishIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var patrol = scn.GetShadowCard("patrol");

        scn.StartGame();
        scn.AddWoundsToChar(aragorn, 3);
        scn.MoveMinionsToTable(patrol);

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(aragorn, patrol);
        scn.FreepsPlayCard(endgame);
        assertEquals(10, scn.GetStrength(aragorn));
        assertInZone(Zone.FREE_CHARACTERS, aragorn);

        // Aragorn, 10, loses to the Patrol Troop, 13, and the wound of losing kills an exhausted companion.
        scn.ShadowPass();
        scn.FreepsPass();
        assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertInZone(Zone.DEAD, aragorn);
        assertEquals(0, scn.GetWoundsOn(patrol));
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
    }

    @Test
    public void PlayedOnACompanionWhoIsNotFightingItLastsThatSkirmishAndNoWinOfTheTurnIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var aragorn = scn.GetFreepsCard("aragorn");
        var pippin = scn.GetFreepsCard("pippin");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn, pippin);
        scn.MoveCardsToHand(endgame);

        var aragornsSavage = scn.GetShadowCard("savage1");
        var pippinsSavage = scn.GetShadowCard("savage2");

        scn.StartGame();
        scn.AddWoundsToChar(aragorn, 3);
        scn.AddWoundsToChar(pippin, 3);
        scn.MoveMinionsToTable(aragornsSavage, pippinsSavage);

        scn.SkipToAssignments();
        scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, aragornsSavage}, new PhysicalCardImpl[]{pippin, pippinsSavage});
        scn.FreepsResolveSkirmish(aragorn);

        // Played in Aragorn's skirmish on Pippin, whose own skirmish has not begun.
        scn.FreepsPlayCard(endgame);
        assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
        scn.FreepsChooseCard(pippin);
        // 3 + 2 for each of his 3 wounds (enduring) + 2
        assertEquals(3 + 6 + 2, scn.GetStrength(pippin));
        assertEquals(8, scn.GetStrength(aragorn));

        // Aragorn, 8, beats his Savage, 5: he is not "that companion", and nothing is asked.
        scn.ShadowPass();
        scn.FreepsPass();
        assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
        assertEquals(3, scn.GetWoundsOn(aragorn));
        assertEquals(3, scn.GetWoundsOn(pippin));
        assertEquals(1, scn.GetWoundsOn(aragornsSavage));

        // Pippin's own skirmish is another skirmish: the bonus is gone, and his win there is not "this skirmish".
        scn.FreepsResolveSkirmish(pippin);
        assertEquals(3 + 6, scn.GetStrength(pippin));
        scn.FreepsPass();
        scn.ShadowPass();
        assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(3, scn.GetWoundsOn(pippin));
        assertEquals(1, scn.GetWoundsOn(pippinsSavage));
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
    }

    @Test
    public void WithNoExhaustedGondorCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        var endgame = scn.GetFreepsCard("endgame");
        scn.MoveCompanionsToTable(aragorn);
        scn.MoveCardsToHand(endgame);

        var savage = scn.GetShadowCard("savage1");

        scn.StartGame();
        // Frodo is exhausted and not [gondor]; Aragorn is [gondor] and one wound short of exhausted.
        scn.AddWoundsToChar(frodo, 3);
        scn.AddWoundsToChar(aragorn, 2);
        scn.MoveMinionsToTable(savage);

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(aragorn, savage);

        // An event may be played though it finds nobody to act on.
        assertTrue(scn.FreepsPlayAvailable(endgame));
        assertEquals(8, scn.GetStrength(aragorn));
        // 3 + 1 for The Ruling Ring
        assertEquals(4, scn.GetStrength(frodo));
        scn.FreepsPlayCard(endgame);
        assertInZone(Zone.DISCARD, endgame);
        assertEquals(8, scn.GetStrength(aragorn));
        assertEquals(4, scn.GetStrength(frodo));
        assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

        // Aragorn wins at his own 8, and his win is answered by nothing.
        scn.ShadowPass();
        scn.FreepsPass();
        assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
        assertEquals(2, scn.GetWoundsOn(aragorn));
        assertEquals(3, scn.GetWoundsOn(frodo));
        assertEquals(1, scn.GetWoundsOn(savage));
        assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
    }
}
