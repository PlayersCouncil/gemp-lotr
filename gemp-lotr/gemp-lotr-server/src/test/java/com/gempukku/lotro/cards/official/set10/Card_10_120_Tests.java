package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_120_Tests
{

	// The King path, Watchers of Cirith Ungol its site 8.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("household", "5_83");   // Household Guard: [rohan] Man companion, strength 6; Valiant is his whole text

					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");
					// Morgul Mongrel: [wraith] Orc, strength 7; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("mongrel", "7_194");
					// Ulaire Lemenya: [wraith] Nazgul, no Orc, strength 9; her text reaches only a bearer of a [wraith] condition
					put("lemenya", "1_232");
					// Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
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
	public void WatchersofCirithUngolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Watchers of Cirith Ungol
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8K
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>Skirmish:</b> Spot your [wraith] Orc and remove 2 threats to make that [wraith] Orc strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Watchers of Cirith Ungol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void SkirmishActionRemovesTwoThreatsToMakeAWraithOrcStrengthPlus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchers = scn.GetShadowSite(8);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var household = scn.GetFreepsCard("household");
		scn.MoveCompanionsToTable(aragorn, household);

		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(7);
		// Three companions: room for three threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.FreepsPass();

		assertSame(watchers, scn.GetCurrentSite());
		scn.MoveMinionsToTable(hound, mongrel, lemenya, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{household, hound});
		// Lemenya and the Chieftain are left over and stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// The Free Peoples player has threats to remove, but no [wraith] Orc of his own to spot.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(watchers));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(watchers));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));

		scn.ShadowUseCardAction(watchers);
		// A [wraith] Orc waiting in the other skirmish and one of another title skirmishing now; not the [wraith]
		// Nazgul, not the [sauron] Orc, no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, chieftain, aragorn, household, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(hound);
		assertEquals(1, scn.GetThreats());
		assertEquals(8, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish is resolved; the bonus was for that skirmish phase alone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(household);
		assertTrue(scn.IsCharSkirmishingAgainst(household, hound));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchers = scn.GetShadowSite(8);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertSame(watchers, scn.GetCurrentSite());
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(watchers));

		// Frodo and Aragorn: room for a second threat.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(watchers));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutAWraithOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchers = scn.GetShadowSite(8);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		assertSame(watchers, scn.GetCurrentSite());
		// A [wraith] minion who is no Orc and an Orc of another culture.
		scn.MoveMinionsToTable(lemenya, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(watchers));

		// A [wraith] Orc is then there to spot.
		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(watchers));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
