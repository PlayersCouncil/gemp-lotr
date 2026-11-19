package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dawn", "4_163");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text.
					put("guard", "10_81");
					// Gríma, Wormtongue: an [isengard] minion who is a Man, vitality 3; a maneuver ability never used.
					put("grima", "4_154");
					// Aragorn, Ranger of the North: an unbound [gondor] Man, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: an unbound [dwarven] Dwarf, vitality 3; a skirmish ability never used.
					put("gimli", "1_13");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4.
					put("merry", "1_303");
					// Boromir, Son of Denethor: an unbound [gondor] Man, vitality 3, to be exhausted; a skirmish
					// ability never used.
					put("boromir", "1_97");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Farmer Maggot, Chaser of Rascals: a Hobbit ally, vitality 3; a fellowship ability never used.
					put("maggot", "1_288");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoDawnforMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: No Dawn for Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an Uruk-hai to exert X unbound companions, where X is the number of sites you control.
		*/

		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");

		assertEquals("No Dawn for Men", dawn.getBlueprint().getTitle());
		assertNull(dawn.getBlueprint().getSubtitle());
		assertFalse(dawn.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, dawn.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, dawn.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, dawn.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(dawn, Timeword.MANEUVER));
		assertEquals(2, dawn.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiToExertTwoUnboundCompanionsWithTwoSitesControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, gimli, merry, boromir, sam);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.HAND, dawn);
		scn.FreepsPass();
		// Sites 1 and 2, behind both markers.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(savage, guard, grima);
		// Vitality 3 with 2 wounds: Boromir cannot exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dawn));
		scn.ShadowPlayCard(dawn);

		// Uruk-hai of two cultures; not an [isengard] minion who is no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grima));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(grima));

		// Two, for two sites: unbound companions of three cultures and races; not Boromir (exhausted), nor Sam
		// (Ring-bound), nor Frodo, nor the ally.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, sam, frodo, maggot));
		scn.ShadowChooseCards(aragorn, gimli);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsIsNotCountedAndOneSiteExertsOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, merry);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.HAND, dawn);
		scn.FreepsPass();
		// Site 1 is the Free Peoples player's (taken on a turn of his own as the Shadow player), site 2 the
		// Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(dawn);
		// The one Uruk-hai exerts unasked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, merry));
		scn.ShadowChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSiteControlledOnlyTheUrukHaiExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.HAND, dawn);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dawn));
		scn.ShadowPlayCard(dawn);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, dawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetShadowCard("dawn");
		var savage = scn.GetShadowCard("savage");
		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(dawn);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.HAND, dawn);
		scn.FreepsPass();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(savage, grima);
		// Vitality 3 with 2 wounds: the Uruk-hai cannot exert; Gríma could.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dawn));
	}
}
