package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("streaming", "8_78");
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc, strength 7; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, no Orc; strength 9; text only on bearers of [wraith] conditions
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; text only on a weapon played on him
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6; skirmish ability only
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only held in the Free Peoples hand to make it up
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StreamingtotheFieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Streaming to the Field
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: For each [wraith] Orc you can spot, the Free Peoples player must have an additional card in hand to have initiative.
		 * 	<b>Skirmish:</b> Discard this condition to make a [wraith] Orc strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("streaming");

		assertEquals("Streaming to the Field", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ANazgulAndASauronOrcAskForNoExtraCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var streaming = scn.GetShadowCard("streaming");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		scn.MoveCardsToSupportArea(streaming);

		scn.StartGame();
		// A [wraith] minion who is no Orc, and an Orc who is not [wraith].
		scn.MoveMinionsToTable(lemenya, chieftain);

		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.SetFreepsHand(filler1, filler2, filler3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWraithOrcAsksForOneMoreCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var streaming = scn.GetShadowCard("streaming");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var filler6 = scn.GetFreepsCard("filler6");
		scn.MoveCardsToSupportArea(streaming);
		scn.MoveCardsToHand(mongrel);

		scn.StartGame();
		// The Nazgul is no Orc and adds nothing to the count.
		scn.MoveMinionsToTable(hound, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One [wraith] Orc: the Free Peoples player needs 5 cards.
		scn.SetFreepsHand(filler1, filler2, filler3, filler4);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		scn.SetFreepsHand(filler1, filler2, filler3, filler4, filler5);
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// A second [wraith] Orc, of another title, played: he needs 6.
		scn.ShadowPlayCard(mongrel);
		assertInPlay(mongrel);
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		scn.SetFreepsHand(filler1, filler2, filler3, filler4, filler5, filler6);
		assertEquals(6, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeAWraithOrcStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var streaming = scn.GetShadowCard("streaming");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(streaming);
		assertInZone(Zone.SUPPORT, streaming);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya, chieftain);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(streaming));
		scn.ShadowPass();

		// The Hound and the Chieftain are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{gimli, lemenya});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, streaming);
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.ShadowActionAvailable(streaming));

		scn.ShadowUseCardAction(streaming);
		// The text names no skirmish: the [wraith] Orc in this skirmish and the one in none are both offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInDiscard(streaming);
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Mongrel (7), who lives with one wound; in the next skirmish the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoWraithOrcOnTheTableIsStillOfferedAndItsCostPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var streaming = scn.GetShadowCard("streaming");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(streaming);
		assertInZone(Zone.SUPPORT, streaming);

		scn.StartGame();
		// A [wraith] minion who is no Orc, and an Orc who is not [wraith]: no [wraith] Orc anywhere.
		scn.MoveMinionsToTable(lemenya, chieftain);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(gimli, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, streaming);
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(streaming));

		scn.ShadowUseCardAction(streaming);
		assertInDiscard(streaming);
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
