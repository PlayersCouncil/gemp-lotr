package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_014_Tests
{
	private final HashMap<String, String> companionDeck = new HashMap<>()
	{{
		// Gimli: twilight 2
		put("gimli", "1_13");
		// Boromir: twilight 3
		put("boromir", "1_97");
		// Legolas: twilight 2
		put("legolas", "1_50");
		// Merry: twilight 1
		put("merry", "1_302");
		// Aragorn: twilight 4
		put("aragorn", "1_89");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_14", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_14"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(companionDeck,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StartingFellowshipCostStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_14
		 * Type: MetaSite
		 * Game Text: Your starting companions must have a total twilight cost of 3 or less.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_14", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CannotSelect4TwilightWorthWithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_14: "Your starting companions must have a total twilight cost of 3 or less."
		// With the modifier, Gimli (2) + Legolas (2) = 4 should NOT be selectable.
		// In addition, the 4-cost Aragorn should not be an option either.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas, gimli, merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(aragorn));

		// Select Gimli (cost 2), total = 2
		scn.FreepsChooseCard(gimli);

		// Legolas (cost 2) should NOT be available since 2 + 2 = 4 > 3
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(legolas, aragorn));
	}

	@Test
	public void CanSelect3TwilightWorthWithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_14: Gimli (2) + Merry (1) = 3 should be selectable.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		// Select Gimli (cost 2), total = 2
		scn.FreepsChooseCard(gimli);

		// Merry (cost 1) should still be available since 2 + 1 = 3 <= 3
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(merry));
		scn.FreepsChooseCard(merry);

		// Done — no more companions fit under 3
		assertFalse(scn.FreepsDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void CanSelect4TwilightWorthWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier, Gimli (2) + Legolas (2) = 4 should be selectable (normal limit).

		var scn = GetNoModScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas, gimli, aragorn));

		scn.FreepsChooseCard(gimli);

		// Legolas (cost 2) should be available since 2 + 2 = 4 <= 4
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(aragorn));
	}

	@Test
	public void ShadowCopyDoesNotAffectFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_14: Shadow's copy should not reduce FP's starting fellowship limit.

		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas, gimli, aragorn));

		scn.FreepsChooseCard(gimli);

		// Without FP having the modifier, normal limit of 4 applies: 2+2=4 <= 4
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(aragorn));
	}
}
