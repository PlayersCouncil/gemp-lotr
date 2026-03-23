package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_015_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Aragorn, Ranger of the North (1_89): FP Man companion, twilight 4
		put("aragorn", "1_89");
		// Legolas, Greenleaf (1_50): FP Elf companion, twilight 2
		put("legolas", "1_50");
		// Barliman Butterbur (1_70): FP Man ally, twilight 0, allyHome 1F
		put("barliman", "1_70");
		// Band of Wild Men (4_4): Shadow Man minion, twilight 5
		put("wildmen", "4_4");
		// Goblin Runner (1_178): Shadow Orc minion, twilight 1
		put("runner", "1_178");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_15", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
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
		 * Game Text: You may not play Men.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_15", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsCannotPlayManCompanionOrAlliesButCanPlayOtherRaces() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// FP has the modifier. Aragorn (Man) should not be playable.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");

		scn.MoveCardsToHand(aragorn, legolas);

		scn.StartGame();

		// Aragorn (Man) should NOT be playable
		assertFalse(scn.FreepsPlayAvailable(aragorn));
		// Barliman (Man ally) should NOT be playable
		assertFalse(scn.FreepsPlayAvailable(barliman));
		// Legolas (Elf) should be playable
		assertTrue(scn.FreepsPlayAvailable(legolas));
	}


	@Test
	public void FreepsCannotSelectManInStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// Man companions should not be selectable during starting fellowship.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Aragorn (Man) should NOT be available for starting fellowship
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
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToShadowHand("wildmen", "runner");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass(); // move to site 2

		// Band of Wild Men (Man) should NOT be playable
		assertFalse(scn.ShadowPlayAvailable(wildmen));
		// Goblin Runner (Orc) should be playable
		assertTrue(scn.ShadowPlayAvailable(runner));
	}

	@Test
	public void OpponentCanStillPlayMen() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_15: "You may not play Men."
		// Only FP has the modifier. Shadow should still be able to play Man minions.

		var scn = GetFreepsScenario();

		var wildmen = scn.GetShadowCard("wildmen");

		scn.MoveCardsToShadowHand("wildmen");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass(); // move to site 2

		// Shadow should still be able to play Band of Wild Men
		assertTrue(scn.ShadowPlayAvailable(wildmen));
	}
}
