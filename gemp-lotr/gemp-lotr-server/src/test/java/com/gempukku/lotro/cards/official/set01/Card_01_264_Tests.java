package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_264_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bowmen", "1_264");
					// Orc Soldier: a [sauron] Orc, strength 7, vitality 2; a skirmish ability never used
					put("soldier", "1_271");
					// Orc Chieftain: a [sauron] Orc, cost 2, strength 7, vitality 2; his text answers only a weapon
					// played on him
					put("chieftain", "1_266");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; "may add (2)" when played, and it is
					// cheated in
					put("runner", "1_178");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, no Orc; damage +1 is its whole text
					put("uruk", "10_81");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcBowmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Bowmen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 		While you can spot a [sauron] Orc, add 1 to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bowmen");

		assertEquals("Orc Bowmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileASauronOrcCanBeSpottedOneIsAddedToTheMinionArcheryTotalAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetShadowCard("bowmen");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();

		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// No archer on either side: 0 + 1 for the Bowmen. The fellowship's total is untouched.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// A second [sauron] Orc adds nothing: it is 1, not 1 for each Orc.
		scn.MoveMinionsToTable(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, chieftain);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// The one wound is placed on the lone companion.
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NothingIsAddedBesideAMoriaOrcAndASauronUrukHaiUntilASauronOrcIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetShadowCard("bowmen");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(bowmen);
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();

		scn.MoveMinionsToTable(runner, uruk);
		scn.SetTwilight(10);
		assertInHand(chieftain);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// An Orc of another culture and a [sauron] minion who is no Orc: nothing is added.
		assertInZone(Zone.SHADOW_CHARACTERS, runner, uruk);
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
