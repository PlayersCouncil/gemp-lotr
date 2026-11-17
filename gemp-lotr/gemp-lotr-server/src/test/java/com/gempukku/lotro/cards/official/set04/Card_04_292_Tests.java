package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_292_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "4_292");

					// Guma, Plains Farmer and Hlafwine, Village Farmhand: [rohan] allies, each discarded only if an
					// opponent controls site 4 of the Towers block (not on this path); abilities used only on purpose.
					put("guma", "4_277");
					put("hlafwine", "4_281");
					// Barliman Butterbur: a [gandalf] ally; a fellowship ability used only on purpose.
					put("barliman", "1_70");
					// Household Guard: a [rohan] companion who is no ally; Valiant only.
					put("hguard", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Théoden, Son of Thengel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Regroup:</b> Spot a [rohan] ally and exert Théoden to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Son of Thengel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void RegroupAbilityExertsTheodenToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The Shadow player controls sites 1 and 2, and the Free Peoples player the highest, site 3.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		// CR 4.2 "liberate": the highest-numbered site an opponent controls, with nothing asked.
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsItIsOfferedAndLiberatesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(hlafwine);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		// The only controlled site is his own player's.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.SkipToPhase(Phase.REGROUP);

		// The exertion is a cost and can be paid, so the ability is there to use.
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoRohanAllyToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var hguard = scn.GetFreepsCard("hguard");
		var barliman = scn.GetFreepsCard("barliman");
		// A [rohan] companion and an ally of another culture, and no [rohan] ally.
		scn.MoveCompanionsToTable(theoden, hguard);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		assertTrue(scn.ShadowControls(site1));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
	}

	@Test
	public void IsNotOfferedWhileTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		// Vitality 2 with 1 wound: exhausted, so he cannot exert.
		scn.AddWoundsToChar(theoden, 1);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.ShadowControls(site1));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
		assertTrue(scn.ShadowControls(site1));
	}
}
