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

public class Card_07_357_Tests
{

	// The King path, Morgul Vale its site 8.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability for a Hobbit, used only on purpose
					put("household", "5_83");   // Household Guard: [rohan] Man companion, strength 6; Valiant is his whole text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it
					put("chieftain", "1_266");
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					// Banner of the Eye: [sauron] hand weapon, strength +1, on a [sauron] Orc; answers a skirmish won
					put("banner", "6_98");
					// Sauron's Hatred: [sauron] support-area condition; its abilities are Shadow and regroup ones
					put("hatred", "7_310");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_357");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulValeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Vale
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>Skirmish:</b> Spot your [sauron] minion and remove 2 threats to make your [sauron] minion strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Morgul Vale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void SkirmishActionRemovesTwoThreatsToMakeASauronMinionStrengthPlus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vale = scn.GetShadowSite(8);
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var household = scn.GetFreepsCard("household");
		scn.MoveCompanionsToTable(boromir, household);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var cuguard = scn.GetShadowCard("cuguard");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.SkipToSite(7);
		// Three companions: room for three threats.
		scn.AddThreats(3);
		scn.FreepsPass();

		assertSame(vale, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage, chieftain, cuguard);
		scn.AttachCardsTo(chieftain, banner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, savage}, new PhysicalCardImpl[]{household, chieftain});
		// The Cirith Ungol Guard is left over and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		// The Free Peoples player has threats to remove, but no [sauron] minion of his own to spot.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(vale));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(vale));
		assertEquals(5, scn.GetStrength(savage));
		// 7 + 1 for the Banner
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(cuguard));

		scn.ShadowUseCardAction(vale);
		// A [sauron] Orc waiting in the other skirmish and a [sauron] Uruk-hai in none; not the [isengard]
		// Uruk-hai skirmishing now, not the [sauron] possession, no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, cuguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, banner, boromir, household, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(chieftain);
		assertEquals(1, scn.GetThreats());
		assertEquals(11, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(cuguard));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Boromir's skirmish is resolved; the bonus was for that skirmish phase alone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(household);
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionMayStrengthenTheSauronMinionInTheSkirmishBeingResolved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vale = scn.GetShadowSite(8);
		var boromir = scn.GetFreepsCard("boromir");
		var household = scn.GetFreepsCard("household");
		scn.MoveCompanionsToTable(boromir, household);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.SkipToSite(7);
		// Three companions: room for three threats.
		scn.AddThreats(3);
		scn.FreepsPass();

		assertSame(vale, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage, chieftain, cuguard);
		scn.SkipToAssignments();
		// The Chieftain is left over and stays unassigned; the [isengard] Uruk-hai waits in the other skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, cuguard}, new PhysicalCardImpl[]{household, savage});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(vale));
		assertEquals(3, scn.GetThreats());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(cuguard));

		scn.ShadowUseCardAction(vale);
		// The [sauron] Uruk-hai fighting now and the [sauron] Orc in none; not the [isengard] Uruk-hai waiting, no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cuguard, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, boromir, household));

		scn.ShadowChooseCard(cuguard);
		assertEquals(1, scn.GetThreats());
		assertEquals(12, scn.GetStrength(cuguard));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneThreatToRemove()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vale = scn.GetShadowSite(8);
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AddThreats(1);
		scn.FreepsPass();

		assertSame(vale, scn.GetCurrentSite());
		// A [sauron] Uruk-hai alone is the minion spotted.
		scn.MoveMinionsToTable(cuguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, cuguard);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(vale));

		// Frodo and Boromir: room for a second threat.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(vale));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutASauronMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vale = scn.GetShadowSite(8);
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var hatred = scn.GetShadowCard("hatred");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AddThreats(2);
		scn.FreepsPass();

		assertSame(vale, scn.GetCurrentSite());
		// An [isengard] Uruk-hai minion and a [sauron] card that is no minion.
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(hatred);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(vale));

		// A [sauron] Orc alone is then the minion spotted.
		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(vale));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
