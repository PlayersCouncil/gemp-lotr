package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_270_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band1", "1_270");
					put("band2", "1_270");   // not unique: "this minion" is read beside other copies
					put("band3", "1_270");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Scouting Band: cost 3, site number 6
					put("band1", "1_270");
					put("band2", "1_270");
					put("band3", "1_270");
					// Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), cost 3, site number 6;
					// Damage +1 is its whole text
					put("guard1", "10_81");
					put("guard2", "10_81");
					put("guard3", "10_81");
					// Uruk Savage: an [isengard] minion, cost 2, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcScoutingBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Scouting Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.<br>The roaming penalty for each [sauron] minion you play is -1.<br><b>Skirmish:</b> Exert this minion to wound a character it is skirmishing.
		*/

		var scn = GetScenario();

		var band = scn.GetFreepsCard("band1");

		assertEquals("Orc Scouting Band", band.getBlueprint().getTitle());
		assertNull(band.getBlueprint().getSubtitle());
		assertFalse(band.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, band.getBlueprint().getSide());
		assertEquals(Culture.SAURON, band.getBlueprint().getCulture());
		assertEquals(CardType.MINION, band.getBlueprint().getCardType());
		assertEquals(Race.ORC, band.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(band, Keyword.TRACKER));
		assertEquals(3, band.getBlueprint().getTwilightCost());
		assertEquals(8, band.getBlueprint().getStrength());
		assertEquals(2, band.getBlueprint().getVitality());
		assertEquals(6, band.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheNextSauronMinionPaysOneLessForRoamingAndTheBandItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");
		var band3 = scn.GetShadowCard("band3");
		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var guard3 = scn.GetShadowCard("guard3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(band1, band2, band3, guard1, guard2, guard3, savage);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// Site 2: every minion here is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 27 set + 1 companion + 2 for Ettenmoors
		assertEquals(30, scn.GetTwilight());

		scn.ShadowPlayCard(band1);
		// 3 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(25, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, band1);

		scn.ShadowPlayCard(guard1);
		// 3 to play + 2 roaming - 1 for the Band.  The penalty is less; the minion is no less roaming.
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard1, Keyword.ROAMING));

		scn.ShadowPlayCard(savage);
		// An [isengard] minion: 2 to play + 2 roaming
		assertEquals(17, scn.GetTwilight());

		scn.ShadowPlayCard(band2);
		// 3 to play + 2 roaming - 1 for the first Band, and nothing for itself
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, band2);

		scn.ShadowPlayCard(guard2);
		// Two Bands in play: 3 to play + 2 roaming - 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard2, Keyword.ROAMING));

		scn.ShadowPlayCard(band3);
		// 3 to play + 2 roaming - 2
		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(guard3);
		// Three Bands in play take the penalty of 2 to nothing and no lower: 3 to play, not 3 - 1
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard3, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var band1 = scn.GetShadowCard("band1");
		var guard1 = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();

		// Site 6, the site number of the Cirith Ungol Guard: he pays no roaming penalty for the Band to make less.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(band1);
		scn.SetShadowHand(guard1);

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(guard1));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(guard1));

		scn.ShadowPlayCard(guard1);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(guard1, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishExertsThisMinionToWoundTheCharacterItIsSkirmishingAndNobodyForOneWhoseSkirmishHasNotBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");
		var band3 = scn.GetShadowCard("band3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(band1, band2, band3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, band1}, new PhysicalCardImpl[]{boromir, band3});
		// A second copy in the same skirmish, put there by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, band2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(band1));
		assertTrue(scn.ShadowActionAvailable(band2));
		// The copy assigned to Boromir is skirmishing nobody yet; the text names no skirmish, and the exertion can be paid.
		assertTrue(scn.ShadowActionAvailable(band3));
		assertEquals(0, scn.GetWoundsOn(band3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.ShadowUseCardAction(band3);

		// Paid for nothing: neither Aragorn, in the skirmish being fought, nor Boromir, whose skirmish is to come.
		assertEquals(1, scn.GetWoundsOn(band3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(band1));
		assertEquals(0, scn.GetWoundsOn(band2));
		scn.ShadowUseCardAction(band1);

		// This minion exerts, not the copy beside it, and Aragorn, whom it is skirmishing, is wounded.
		assertEquals(1, scn.GetWoundsOn(band1));
		assertEquals(0, scn.GetWoundsOn(band2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(band3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyItIsSkirmishingAndAWoundKillsHimWithOneVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(band1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);

		// Thrarin has exerted to take part in skirmishes: vitality 2, one left.
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, band1);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(band1));
		assertTrue(scn.ShadowActionAvailable(band1));
		scn.ShadowUseCardAction(band1);

		// A wound, not an exertion: an exhausted ally takes it and is killed.
		assertEquals(1, scn.GetWoundsOn(band1));
		assertInZone(Zone.DEAD, thrarin);
		// His skirmish is over with him gone, and it was the only one.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedScoutingBandIsNotOfferedTheAbilityWhileTheCopyBesideItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band1 = scn.GetShadowCard("band1");
		var band2 = scn.GetShadowCard("band2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(band1, band2);
		scn.AddWoundsToChar(band1, 1);
		assertEquals(1, scn.GetWoundsOn(band1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, band1);
		scn.ShadowAssignToMinions(aragorn, band2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 2 with one wound: he cannot exert.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(band1));
		assertTrue(scn.ShadowActionAvailable(band2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
