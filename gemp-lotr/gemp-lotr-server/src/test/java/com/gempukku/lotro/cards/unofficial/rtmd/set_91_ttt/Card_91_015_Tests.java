package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_015_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, twilight 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, twilight 2
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, twilight 0
					put("wildmen", "4_4");      // Band of Wild Men: a [dunland] Man minion, twilight 5, site 3
					put("bree", "11_90");       // Man of Bree: a [men] Man minion, twilight 2, site 4
					// Ted Sandyman, Chief's Men's Ally: a minion of the [men] culture whose race is Hobbit, twilight 1, site 2
					put("ted", "18_75");
					put("runner", "1_178");     // Goblin Runner: an Orc minion, twilight 1, site 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_15", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, twilight 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, twilight 2
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, twilight 0
					put("wildmen", "4_4");      // Band of Wild Men: a [dunland] Man minion, twilight 5, site 3
					put("bree", "11_90");       // Man of Bree: a [men] Man minion, twilight 2, site 4
					// Ted Sandyman, Chief's Men's Ally: a minion of the [men] culture whose race is Hobbit, twilight 1, site 2
					put("ted", "18_75");
					put("runner", "1_178");     // Goblin Runner: an Orc minion, twilight 1, site 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_15"
		);
	}

	@Test
	public void CantPlayMenStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_15
		 * Type: MetaSite
		 * Intensity: 2
		 * Game Text: You may not play Men.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_15", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsCannotPlayManCompanionOrAlliesButCanPlayOtherRaces() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// FP has the modifier. Aragorn (Man) should not be playable.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");

		scn.MoveCardsToHand(aragorn, legolas, barliman);

		scn.StartGame();

		// Aragorn (Man) should NOT be playable
		assertFalse(scn.FreepsPlayAvailable(aragorn));
		// Barliman (Man ally) should NOT be playable
		assertFalse(scn.FreepsPlayAvailable(barliman));
		// Legolas (Elf) should be playable
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreepsCannotSelectManInStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// Man companions should not be selectable during starting fellowship.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Aragorn (Man) should NOT be available for starting fellowship,
		// though his cost of 4 is within what the rule allows
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));
		// Legolas (Elf) should be available
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
	}

	@Test
	public void ShadowCannotPlayManMinion() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// Shadow has the modifier. Band of Wild Men (Man minion) should not be playable.

		var scn = GetShadowScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var bree = scn.GetShadowCard("bree");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToShadowHand("wildmen", "runner", "bree", "ted");

		scn.StartGame();
		// Enough for any of them: the dearest is the Band, 5 + 2 roaming
		scn.SetTwilight(10);
		scn.FreepsPass(); // move to site 2

		// Band of Wild Men (Man) should NOT be playable
		assertFalse(scn.ShadowPlayAvailable(wildmen));
		// nor Man of Bree, a Man of another culture
		assertFalse(scn.ShadowPlayAvailable(bree));
		// Goblin Runner (Orc) should be playable
		assertTrue(scn.ShadowPlayAvailable(runner));
		// and so should Ted Sandyman: he is of the culture called Men, and a Hobbit
		assertTrue(scn.ShadowPlayAvailable(ted));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentCanStillPlayMen() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// Only FP has the modifier. Shadow should still be able to play Man minions.

		var scn = GetFreepsScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var bree = scn.GetShadowCard("bree");

		scn.MoveCardsToShadowHand("wildmen", "bree");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass(); // move to site 2

		// Shadow should still be able to play Band of Wild Men
		assertTrue(scn.ShadowPlayAvailable(wildmen));
		// and Man of Bree
		assertTrue(scn.ShadowPlayAvailable(bree));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreepsCanStillPlayMenWhenShadowHasMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: Only Shadow has the modifier. Freeps should still be able to play Man companions and allies.

		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");

		scn.MoveCardsToHand(aragorn, barliman);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(barliman));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
