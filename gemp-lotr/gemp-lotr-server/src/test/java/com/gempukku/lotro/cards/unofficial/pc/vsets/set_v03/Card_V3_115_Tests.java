package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_115_Tests
{

// ----------------------------------------
// SAM, UNDAUNTED TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "103_115");        // Sam, Undaunted

					// Shadow support conditions
					put("bladetip1", "1_209");    // Blade Tip - cost 0 (even) condition, support area
					put("bladetip2", "1_209");    // Second copy for attaching
					put("blackbreath1", "1_207"); // Black Breath - cost 1 (odd) condition
					put("blackbreath2", "1_207"); // Second copy for attaching

					// Shadow support possessions
					put("charger", "103_41");     // Bladetusk Charger - cost 3 (odd) possession
					put("ships", "8_65");         // Ships of Great Draught - cost 2 (even) possession

					// Shadow attached possessions (should NOT be hindered)
					put("raiderbow", "7_155");    // Raider Bow - cost 2 (even), attaches to Raider man
					put("spear", "4_255");        // Southron Spear - cost 1 (odd), attaches to Southron

					// Shadow artifacts (should NOT be hindered - not condition or possession)
					put("ithilstone", "9_47");    // Ithil Stone - cost 0 (even) artifact
					put("palecrown", "101_39");   // Pale Crown - cost 1 (odd) artifact

					put("southron", "4_222");     // Desert Warrior - for attaching possessions

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sam, Undaunted
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Ring-bound. Frodo is resistance +2.
		 * 		Maneuver: If you cannot spot more than 4 companions, add a burden to choose condition or possession,
		 * 		then choose odd or even.  Hinder all Shadow support cards with a type and twilight cost matching your
		 * 		choice. Limit once per turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Undaunted", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}



	@Test
	public void SamHindersEvenConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bladetip = scn.GetShadowCard("bladetip1");   // Cost 0 - even
		var blackbreath = scn.GetShadowCard("blackbreath1"); // Cost 1 - odd
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(bladetip, blackbreath);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(bladetip));
		assertFalse(scn.IsHindered(blackbreath));

		scn.FreepsUseCardAction(sam);

		// Choose condition
		scn.FreepsChoose("condition");
		// Choose even
		scn.FreepsChoose("even");

		// Blade Tip (0) should be hindered, Black Breath (1) should not
		assertTrue(scn.IsHindered(bladetip));
		assertFalse(scn.IsHindered(blackbreath));
	}

	@Test
	public void SamHindersOddConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bladetip = scn.GetShadowCard("bladetip1");   // Cost 0 - even
		var blackbreath = scn.GetShadowCard("blackbreath1"); // Cost 1 - odd
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(bladetip, blackbreath);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sam);

		// Choose condition
		scn.FreepsChoose("condition");
		// Choose odd
		scn.FreepsChoose("odd");

		// Black Breath (1) should be hindered, Blade Tip (0) should not
		assertFalse(scn.IsHindered(bladetip));
		assertTrue(scn.IsHindered(blackbreath));
	}

	@Test
	public void SamHindersEvenPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var charger = scn.GetShadowCard("charger");  // Cost 3 - odd
		var ships = scn.GetShadowCard("ships");      // Cost 2 - even
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(charger, ships);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(charger));
		assertFalse(scn.IsHindered(ships));

		scn.FreepsUseCardAction(sam);

		// Choose possession
		scn.FreepsChoose("possession");
		// Choose even
		scn.FreepsChoose("even");

		// Ships (2) should be hindered, Charger (3) should not
		assertFalse(scn.IsHindered(charger));
		assertTrue(scn.IsHindered(ships));
	}

	@Test
	public void SamHindersOddPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var charger = scn.GetShadowCard("charger");  // Cost 3 - odd
		var ships = scn.GetShadowCard("ships");      // Cost 2 - even
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(charger, ships);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sam);

		// Choose possession
		scn.FreepsChoose("possession");
		// Choose odd
		scn.FreepsChoose("odd");

		// Charger (3) should be hindered, Ships (2) should not
		assertTrue(scn.IsHindered(charger));
		assertFalse(scn.IsHindered(ships));
	}

	@Test
	public void SamDoesNotHinderAttachedCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bladetip1 = scn.GetShadowCard("bladetip1");  // Support area - cost 0 even
		var bladetip2 = scn.GetShadowCard("bladetip2");  // Attached to Aragorn
		var ships = scn.GetShadowCard("ships");          // Support area - cost 2 even
		var raiderbow = scn.GetShadowCard("raiderbow");  // Attached - cost 2 even
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(bladetip1, ships);
		scn.AttachCardsTo(aragorn, bladetip2);
		scn.AttachCardsTo(southron, raiderbow);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("condition");
		scn.FreepsChoose("even");

		// Support area condition hindered
		assertTrue(scn.IsHindered(bladetip1));
		// Attached condition NOT hindered
		assertFalse(scn.IsHindered(bladetip2));
	}

	@Test
	public void SamDoesNotHinderArtifacts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var ships = scn.GetShadowCard("ships");          // Possession - cost 2 even
		var ithilstone = scn.GetShadowCard("ithilstone"); // Artifact - cost 0 even
		var charger = scn.GetShadowCard("charger");      // Possession - cost 3 odd
		var palecrown = scn.GetShadowCard("palecrown");  // Artifact - cost 1 odd
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(ships, ithilstone, charger, palecrown);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(sam);
		scn.FreepsChoose("possession");
		scn.FreepsChoose("even");

		// Even possession hindered
		assertTrue(scn.IsHindered(ships));
		// Even artifact NOT hindered (not a possession)
		assertFalse(scn.IsHindered(ithilstone));
		// Odd cards not hindered
		assertFalse(scn.IsHindered(charger));
		assertFalse(scn.IsHindered(palecrown));
	}

	@Test
	public void SamRequiresFourOrFewerCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bladetip = scn.GetShadowCard("bladetip1");
		var southron = scn.GetShadowCard("southron");
		scn.MoveCompanionsToTable(sam, aragorn); // Frodo + Sam + Aragorn = 3
		scn.MoveCardsToSupportArea(bladetip);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// 3 companions - should be available
		assertTrue(scn.FreepsActionAvailable(sam));
	}
}
