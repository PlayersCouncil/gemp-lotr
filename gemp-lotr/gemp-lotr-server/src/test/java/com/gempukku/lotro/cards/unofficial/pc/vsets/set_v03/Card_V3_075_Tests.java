package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V3_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "103_75");
					put("rider1", "12_161");
					put("rider2", "12_161");
					put("witchking", "1_237");

					put("aragorn", "1_89");    // Strength 8
					put("boromir", "1_96");    // Strength 7
					put("gandalf", "1_364");    // Strength 7
					put("gimli", "1_13");      // Strength 6
					put("sam", "1_311");       // Strength 3
					put("merry", "1_302");     // Need ID - hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Lemenya, Anointed with Terror
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each character skirmishing this minion is strength -1 for each other Nazgul you can spot
		*   (or -1 for each other companion you can spot if you can spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Ulaire Lemenya", card.getBlueprint().getTitle());
		assertEquals("Anointed with Terror", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	// ======== NAZGUL COUNT MODE (< 6 COMPANIONS) ========

	@Test
	public void LemenyaReducesStrengthByNazgulCountWhenFewerThan6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Frodo + Aragorn = 2 companions (under threshold)
		scn.MoveMinionsToTable(lemenya, rider1, rider2);  // 3 Nazgul
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn should be -2 strength (2 Nazgul besides Lemenya spotted)
		assertEquals(aragornBaseStrength - 2, scn.GetStrength(aragorn));
	}


// ======== COMPANION COUNT MODE (>= 6 COMPANIONS) ========

	@Test
	public void LemenyaSwitchesToCompanionCountAt6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var rider1 = scn.GetShadowCard("rider1");  // 2 Nazgul total
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		// Frodo (RB) + 5 = 6 companions

		scn.MoveMinionsToTable(lemenya, rider1);
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf, gimli, sam);

		scn.StartGame();
		scn.SkipToAssignments();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 6 companions spotted, so -5 strength (not -2 for Nazgul)
		assertEquals(aragornBaseStrength - 5, scn.GetStrength(aragorn));
	}

	@Test
	public void LemenyaUsesNazgulCountAt5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");  // 3 Nazgul
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");

		var gimli = scn.GetFreepsCard("gimli");
		// Frodo (RB) + 4 = 5 companions (under threshold)

		scn.MoveMinionsToTable(lemenya, rider1, rider2);
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf, gimli);

		scn.StartGame();
		scn.SkipToAssignments();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 5 companions (under threshold), so -3 for Nazgul count
		assertEquals(aragornBaseStrength - 2, scn.GetStrength(aragorn));
	}

	@Test
	public void LemenyaCompanionModeScalesWithMoreCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		// Frodo (RB) + 6 = 7 companions

		scn.MoveMinionsToTable(lemenya);  // Just 1 Nazgul
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf, gimli, sam, merry);

		scn.StartGame();
		scn.SkipToAssignments();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 7 companions spotted, so -6 strength (even though only 1 Nazgul)
		// Errata makes us not consider the companion we are skirmishing against.
		assertEquals(aragornBaseStrength - 6, scn.GetStrength(aragorn));
	}
}
