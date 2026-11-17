package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pride", "7_45");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, strength 8, vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6, vitality 4; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only
					// Gandalf's Wisdom: another Free Peoples condition in the support area; a discount on items played on Gandalf only
					put("wisdom", "2_23");

					// Uruk-hai Raiding Party: strength 9, damage +1 and nothing else; beats Gandalf (8) and the Avenger (6)
					// without overwhelming either
					put("party1", "1_158");
					put("party2", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NumenorsPrideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Númenor's Pride
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Gandalf and add 3 threats.
		 * Gandalf is <b>defender +1</b> and cannot take wounds.
		 * Discard this condition at the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pride");

		assertEquals("Númenor's Pride", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpottedThenAddsThreeThreatsAndMakesHimDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(pride, gandalf);
		assertInHand(pride);

		scn.StartGame();

		// Three companions, so three threats can be added; a [gandalf] companion, but no Gandalf.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pride));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(pride));

		scn.FreepsPlayCard(pride);
		assertInZone(Zone.SUPPORT, pride);
		assertEquals(3, scn.GetThreats());

		// Gandalf alone is defender +1.
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileThreeThreatsCannotAllBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pride = scn.GetFreepsCard("pride");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(pride, aragorn);
		assertInHand(pride);

		scn.StartGame();
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		// Three companions and 1 threat: only 2 more can be added, and the cost is judged payable in full.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pride));

		// A fourth companion: room for 3 more.
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsPlayAvailable(pride));

		scn.FreepsPlayCard(pride);
		assertInZone(Zone.SUPPORT, pride);
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GandalfTakesNoWoundsForALostSkirmishUntilTheConditionIsDiscardedAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pride = scn.GetFreepsCard("pride");
		var wisdom = scn.GetFreepsCard("wisdom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(pride, wisdom);
		assertInZone(Zone.SUPPORT, pride);
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.SkipToAssignments();

		// Through the start of the maneuver, archery and assignment phases the condition stays.
		assertInZone(Zone.SUPPORT, pride);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, party1}, new PhysicalCardImpl[]{avenger, party2});

		// Gandalf (8) loses to the Raiding Party (9): neither the wound nor the damage bonus is placed.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The [gandalf] companion who is no Gandalf takes both.
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(avenger));

		// At the start of the regroup phase this condition is discarded, and another condition is not.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(pride);
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
	}

	@Test
	public void WithoutTheConditionGandalfTakesTheWoundsOfALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, party1);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
