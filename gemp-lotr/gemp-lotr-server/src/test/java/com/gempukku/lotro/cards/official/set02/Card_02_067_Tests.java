package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "2_67");
					put("troop2", "2_67");
					// Goblin Marksman: a [moria] Orc archer, cost 3; Archer is its whole text.
					put("marksman", "1_176");
					put("marksman2", "1_176");
					// Goblin Runner: a [moria] Orc who is no archer; "may add (2)" when played, and it is cheated in.
					put("runner", "1_178");
					// Orthanc Assassin: an [isengard] Uruk-hai archer; "Archer. Damage +1" is its whole text.
					put("assassin", "1_131");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaArcherTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Moria Archer Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>While you can spot another [moria] archer, add 1 to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Moria Archer Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnotherMoriaArcherOneIsAddedToTheMinionArcheryTotalAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troop, marksman);
		// 1 for the Troop + 1 for the Marksman, both archers, + 1 for the Troop's text.  The fellowship's total is
		// untouched.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// A third archer adds its own 1, and the Troop's text still adds 1: it is 1, not 1 for each [moria] archer.
		scn.MoveMinionsToTable(marksman2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, marksman, marksman2);
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NothingIsAddedBesideAMoriaOrcWhoIsNoArcherAndAnIsengardArcherUntilASecondTroopIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var runner = scn.GetShadowCard("runner");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToHand(troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, assassin);
		// 10 set + 1 for Frodo + 2 for Ettenmoors: enough for the second Troop, 6 and 2 for roaming.
		scn.SetTwilight(10);
		assertInHand(troop2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troop, runner, assassin);
		// A [moria] Orc who is no archer, an archer of another culture, and the Troop itself, which is not
		// "another": 1 for the Troop + 1 for the Assassin, and nothing from the Troop's text.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// A second copy is another [moria] archer, and each Troop spots the other: 3 archers + 1 + 1.
		scn.ShadowPlayCard(troop2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		assertEquals(5, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(5, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
