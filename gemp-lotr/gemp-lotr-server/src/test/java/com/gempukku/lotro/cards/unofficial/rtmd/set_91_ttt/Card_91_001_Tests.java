package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInPlay;
import static org.junit.Assert.*;

public class Card_91_001_Tests
{
	private final HashMap<String, String> companionDeck =  new HashMap<>()
	{{
		// Gimli: twilight 2
		put("gimli", "1_13");
		// Boromir: twilight 3
		put("boromir", "1_97");
		// Legolas: twilight 2
		put("legolas", "1_50");
		// Aragorn, Elessar Telcontar: twilight 5
		put("elessar", "10_25");
		// Merry: twilight 1
		put("merry", "1_302");
	}};
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_1", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_1"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StartingFellowshipCostStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_1
		 * Type: MetaSite
		 * Game Text: Your starting companions may have a total twilight cost of 6 instead of 4.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_1", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CanSelect5TwilightWorthWithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_1: "Your starting companions may have a total twilight cost of 6 instead of 4."
		// With the modifier, Gimli (2) + Boromir (3) = 5 should be selectable.

		var scn = GetFreepsScenario();

		var elessar = scn.GetFreepsCard("elessar");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");

		// Starting fellowship decision should be available
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		assertTrue(scn.FreepsHasCardChoicesAvailable(elessar, gimli, boromir, merry));

		// Select Gimli (cost 2), total = 2
		scn.FreepsChooseCard(gimli);

		// Boromir (cost 3) should still be available since 2 + 3 = 5 <= 6
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(boromir, merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(elessar));
		scn.FreepsChooseCard(boromir);

		// Merry (cost 1) still available
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(merry));
		scn.FreepsChooseCard(merry);

		// Done selecting, finish starting fellowship
		assertFalse(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.ShadowDecided("");

		// Now finish game setup
		scn.StartGame(true, false);

		// Both companions should be in play
		assertInPlay(gimli, boromir, merry);
	}

	@Test
	public void CanSelect6TwilightWorthWithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_1: With the modifier, Gimli (2) + Legolas (2) + Boromir (3) would be 7,
		// which exceeds 6. After selecting Gimli + Legolas (4 total), Boromir (3) should
		// NOT be available because 4 + 3 = 7 > 6.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Select Gimli (2) + Legolas (2) = 4
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(legolas);

		// Boromir (3) should NOT be available since 4 + 3 = 7 > 6
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertFalse(scn.FreepsHasCardChoicesAvailable(boromir));

		// Done
		scn.FreepsChoose("");
		scn.StartGame(true, false);
	}

	@Test
	public void CannotSelect5TwilightWorthWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier, Gimli (2) + Boromir (3) = 5 exceeds the normal limit of 4.
		// After selecting Gimli (2), Boromir (3) should NOT be available.

		var scn = GetNoModScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Select Gimli (cost 2), total = 2
		scn.FreepsChooseCard(gimli);

		// Boromir (cost 3) should NOT be available since 2 + 3 = 5 > 4
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertFalse(scn.FreepsHasCardChoicesAvailable(boromir));
	}

	@Test
	public void ShadowCopyDoesNotAffectFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_1: "Your starting companions" — Shadow's copy should not affect the FP
		// player's starting fellowship limit.

		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var elessar = scn.GetFreepsCard("elessar");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(boromir, gimli, merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(elessar));

		// Select Gimli (cost 2)
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Without FP having the modifier, Boromir (3) should NOT be available: 2+3=5 > 4
		assertFalse(scn.FreepsHasCardChoicesAvailable(boromir));
	}

	@Test
	public void ShadowCopyWorks() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_1: "Your starting companions may have a total twilight cost of 6 instead of 4."
		// With the modifier, Gimli (2) + Boromir (3) = 5 should be selectable.

		var scn = GetShadowScenario();

		var elessar = scn.GetShadowCard("elessar");
		var gimli = scn.GetShadowCard("gimli");
		var boromir = scn.GetShadowCard("boromir");
		var merry = scn.GetShadowCard("merry");

		scn.FreepsDecided("");

		// Starting fellowship decision should be available
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));

		assertTrue(scn.ShadowHasCardChoicesAvailable(elessar, gimli, boromir, merry));

		// Select Gimli (cost 2), total = 2
		scn.ShadowChooseCard(gimli);

		// Boromir (cost 3) should still be available since 2 + 3 = 5 <= 6
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(boromir, merry));
		assertTrue(scn.ShadowHasCardChoicesNotAvailable(elessar));
		scn.ShadowChooseCard(boromir);

		// Merry (cost 1) still available
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoicesAvailable(merry));
		scn.ShadowChooseCard(merry);

		// Done selecting, finish starting fellowship
		assertFalse(scn.ShadowDecisionAvailable("Starting fellowship"));


		// Now finish game setup
		scn.StartGame(true, false);

		// Both companions should be in play
		assertInPlay(gimli, boromir, merry);
	}
}
