package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_341_Tests
{

	// The King path, Anduin Banks its site 4.  Site 3 is The Dimholt (a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability for a Hobbit, used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_341");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinBanksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Anduin Banks
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Skirmish:</b> Spot your minion and remove a burden to make that minion strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Anduin Banks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void SkirmishActionRemovesABurdenToMakeAMinionOfTheShadowPlayersStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banks = scn.GetShadowSite(4);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		scn.FreepsPass();

		assertSame(banks, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, chieftain});
		scn.FreepsResolveSkirmish(aragorn);

		// The Free Peoples player has burdens to remove, but no minion of his own to spot.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(banks));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(banks));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));

		scn.ShadowUseCardAction(banks);
		// The minion skirmishing now and the one waiting in the other skirmish; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, boromir, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(chieftain);
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish is resolved; the bonus was for that skirmish phase alone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoBurdenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banks = scn.GetShadowSite(4);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		assertSame(banks, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(banks));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(banks));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedWithOnlyAnOrcMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banks = scn.GetShadowSite(4);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(1);
		scn.FreepsPass();

		assertSame(banks, scn.GetCurrentSite());
		// A [sauron] Orc alone, where the other tests spot an [isengard] Uruk-hai alone or both.
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(banks));
	}
}
