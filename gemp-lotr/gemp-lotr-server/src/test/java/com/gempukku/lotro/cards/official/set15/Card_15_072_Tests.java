package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "15_72");
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man minion, strength 8, vitality 1, site 3, no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally whose home is site 3 of the Fellowship path, so he may be assigned
					// there; a maneuver ability used only on purpose.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillFernyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Bill Ferny, Agent of Saruman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: If Bill Ferny is unwounded, the Free Peoples player cannot assign him to skirmish a companion.
		 * 		<b>Shadow:</b> Exert Bill Ferny twice and spot a site you control to add (2).
		*/

		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");

		assertEquals("Bill Ferny", bill.getBlueprint().getTitle());
		assertEquals("Agent of Saruman", bill.getBlueprint().getSubtitle());
		assertTrue(bill.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, bill.getBlueprint().getSide());
		assertEquals(Culture.MEN, bill.getBlueprint().getCulture());
		assertEquals(CardType.MINION, bill.getBlueprint().getCardType());
		assertEquals(Race.MAN, bill.getBlueprint().getRace());
		assertEquals(2, bill.getBlueprint().getTwilightCost());
		assertEquals(4, bill.getBlueprint().getStrength());
		assertEquals(3, bill.getBlueprint().getVitality());
		assertEquals(4, bill.getBlueprint().getSiteNumber());
	}

	@Test
	public void UnwoundedHeCannotBeAssignedACompanionUntilHisShadowAbilityExertsHimTwiceToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bill = scn.GetShadowCard("bill");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Both markers at site 2, so site 1 may be taken.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(bill, brigand);
		assertEquals(0, scn.GetWoundsOn(bill));

		// To site 3.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Unwounded: neither companion may be assigned to him by the Free Peoples player; either may to the
		// Brigand, who is unwounded too.
		assertFalse(scn.FreepsCanAssignAgainst(frodo, bill));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, bill));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, brigand));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, brigand));

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(bill));
		scn.ShadowUseCardAction(bill);
		assertEquals(2, scn.GetWoundsOn(bill));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.ShadowControls(site1));

		// Wounded, he may be.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, bill));
		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, bill},
				new PhysicalCardImpl[]{frodo, brigand});
		assertTrue(scn.IsCharAssignedAgainst(aragorn, bill));
		assertTrue(scn.IsCharAssignedAgainst(frodo, brigand));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void UnwoundedHeMayStillBeAssignedAnAllyByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var bill = scn.GetShadowCard("bill");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(bill, brigand);
		assertEquals(0, scn.GetWoundsOn(bill));

		// To site 3, Thrarin's home.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		assertEquals(0, scn.GetWoundsOn(bill));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, bill));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, bill));
		assertTrue(scn.FreepsCanAssignAgainst(thrarin, bill));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thrarin, bill},
				new PhysicalCardImpl[]{aragorn, brigand});
		assertTrue(scn.IsCharAssignedAgainst(thrarin, bill));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void UnwoundedHeMayStillBeAssignedACompanionByTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bill = scn.GetShadowCard("bill");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(bill, brigand);
		assertEquals(0, scn.GetWoundsOn(bill));

		scn.FreepsPass();
		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssignAgainst(frodo, bill));

		// The Free Peoples player assigns the Brigand only; Bill is left over for the Shadow player.
		scn.FreepsAssignToMinions(aragorn, brigand);
		assertTrue(scn.ShadowCanAssignAgainst(frodo, bill));
		scn.ShadowAssignToMinions(frodo, bill);
		assertEquals(0, scn.GetWoundsOn(bill));
		assertTrue(scn.IsCharAssignedAgainst(frodo, bill));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithOneWoundOnHimHeMayBeAssignedACompanionByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(bill);
		// One wound is enough: he is no longer unwounded.
		scn.AddWoundsToChar(bill, 1);
		assertEquals(1, scn.GetWoundsOn(bill));

		scn.FreepsPass();
		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssignAgainst(frodo, bill));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, bill));
		scn.FreepsAssignToMinions(aragorn, bill);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, bill));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileOnlyTheFreePeoplesPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		// Both markers at site 3, so sites 1 and 2 may be taken.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Site 1 is controlled, but by the Free Peoples player, as he might have taken it on a turn of his as the
		// Shadow player.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		assertFalse(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(bill);
		assertEquals(0, scn.GetWoundsOn(bill));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bill));

		// Site 2, the Shadow player's own.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site2));
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bill));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileBillFernyCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		// Vitality 3 with a wound: 2 left, enough to exert once and not twice.
		scn.MoveMinionsToTable(bill);
		scn.AddWoundsToChar(bill, 1);
		assertEquals(1, scn.GetWoundsOn(bill));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bill));
		assertEquals(1, scn.GetWoundsOn(bill));
	}
}
