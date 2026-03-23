package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInPlay;
import static org.junit.Assert.*;

public class Card_92_036_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli, Dwarf of Erebor (1_13): unique Dwarven companion, twilight 2
		put("gimli", "1_13");
		// Dwarf Guard (1_7): non-unique Dwarven companion, twilight 1, requires a Dwarf
		put("guard1", "1_7");
		put("guard2", "1_7");
		put("guard3", "1_7");
		put("guard4", "1_7");
		put("guard5", "1_7");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_36", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_36"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_36
		 * Type: MetaSite
		 * Game Text: You cannot have more than 5 companions in your fellowship.
		 *   Fellowship: Exert the Ring-bearer to discard another companion.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_36", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CanPlayUpTo5CompanionsButNot6th() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");

		// Frodo is companion #1. Gimli + 3 guards on table = 5 total.
		scn.MoveCompanionsToTable(gimli, guard1, guard2, guard3);
		scn.MoveCardsToHand(guard4, guard5);

		scn.StartGame();

		// 5 companions in play (Frodo + Gimli + 3 guards). Can play guard4 to reach 5... wait.
		// Frodo + Gimli + guard1 + guard2 + guard3 = 5. Already at 5.
		// guard4 should NOT be playable — already spotting 5 companions.
		assertFalse(scn.FreepsPlayAvailable(guard4));
	}

	@Test
	public void CanPlayThe5thCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");

		// Frodo + Gimli + 2 guards on table = 4, guard3 in hand to play as #5
		scn.MoveCompanionsToTable(gimli, guard1, guard2);
		scn.MoveCardsToHand(guard3);

		scn.StartGame();

		// 4 companions in play, should be able to play the 5th
		assertTrue(scn.FreepsPlayAvailable(guard3));
		scn.FreepsPlayCard(guard3);

		assertInPlay(guard3);
	}

	@Test
	public void FellowshipActionExertsRingBearerToDiscardCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var mod = scn.GetFreepsCard("mod");

		scn.MoveCompanionsToTable(gimli, guard1, guard2);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(mod));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(mod);

		// Ring-bearer should be exerted
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Should be choosing a companion to discard — ring-bearer should NOT be a choice
		assertFalse(scn.FreepsHasCardChoicesAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoicesAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoicesAvailable(guard1));

		scn.FreepsChooseCard(guard1);

		assertInDiscard(guard1);
	}

	@Test
	public void OpponentNotAffectedByCompanionLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");

		// Frodo + Gimli + 3 guards = 5 companions already
		scn.MoveCompanionsToTable(gimli, guard1, guard2, guard3);
		scn.MoveCardsToHand(guard4);

		scn.StartGame();

		// Shadow owns the mod, so FP should not be restricted — can play 6th companion
		assertTrue(scn.FreepsPlayAvailable(guard4));
	}

	@Test
	public void OpponentCantUseFellowshipAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var mod = scn.GetShadowCard("mod");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(mod));
	}
}
