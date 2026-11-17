package com.gempukku.lotro.cards.official.set16;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_16_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker", "16_1");

					put("undead", "16_6");      // Undead of Angmar: [wraith] Wraith, cost 4, site 2; wounded companions resistance -2, a Shadow ability
					put("dread", "16_5");       // Spirit of Dread: [wraith] Wraith of another title, cost 7, site 2; no heals, a Skirmish ability
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] minion who is no Wraith (a Nazgûl), cost 4, site 3
					put("lights", "6_99");      // Corpse Lights: a Wraith who is not [wraith] ([sauron]), cost 3, site 4; a Skirmish ability
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; strength and fierce only at 2 wounds on the Ring-bearer

					put("gimli", "1_13");       // Gimli, Son of Glóin: the Dwarf who exerts for Baruk Khazâd
					put("baruk", "5_5");        // Baruk Khazâd: Maneuver event, "Exert a Dwarf to wound a minion"
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("fell", "4_97");        // Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarrowwightStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Barrow-wight Stalker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 2
		 * Game Text: <b>Enduring</b>. Each [wraith] Wraith cannot take wounds (except during skirmishes).<br><b>Shadow:</b> Remove a burden to play a [wraith] Wraith. That minion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var stalker = scn.GetFreepsCard("stalker");

		assertEquals("Barrow-wight Stalker", stalker.getBlueprint().getTitle());
		assertNull(stalker.getBlueprint().getSubtitle());
		assertTrue(stalker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, stalker.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, stalker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, stalker.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, stalker.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(stalker, Keyword.ENDURING));
		assertEquals(5, stalker.getBlueprint().getTwilightCost());
		assertEquals(11, stalker.getBlueprint().getStrength());
		assertEquals(4, stalker.getBlueprint().getVitality());
		assertEquals(2, stalker.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWraithWraithCannotTakeWoundsOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var stalker = scn.GetShadowCard("stalker");
		var undead = scn.GetShadowCard("undead");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, undead, lemenya, lights, hound);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(lights));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Barred: the Stalker himself and another [wraith] Wraith.  Offered: a [wraith] Nazgûl, a [sauron] Wraith
		// and a [wraith] Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, lights, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stalker, undead));
		scn.FreepsChooseCard(lights);

		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(undead));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithoutTheStalkerAWraithWraithTakesTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var undead = scn.GetShadowCard("undead");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(undead, lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(undead));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(undead, lemenya));
		scn.FreepsChooseCard(undead);

		assertEquals(1, scn.GetWoundsOn(undead));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DuringASkirmishAWraithWraithInNoSkirmishTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(fell);

		var stalker = scn.GetShadowCard("stalker");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, undead);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, undead}, new PhysicalCardImpl[]{gimli, stalker});

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(undead));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();

		// The Stalker waits for Gimli's skirmish and is in none now; during a skirmish phase he may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stalker, undead));
		scn.FreepsChooseCard(stalker);

		assertEquals(1, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(undead));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesABurdenToPlayAWraithWraithFromHandAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var undead = scn.GetShadowCard("undead");
		var dread = scn.GetShadowCard("dread");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveCardsToHand(undead, dread, lemenya, lights);
		scn.MoveMinionsToTable(stalker);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.HAND, undead, dread, lemenya, lights);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertEquals(10, scn.GetTwilight());
		// Lemenya (4 + 2 roaming at site 2) and Corpse Lights (3 + 2 roaming) could be paid for out of this pool.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		assertTrue(scn.ShadowPlayAvailable(lights));

		assertTrue(scn.ShadowActionAvailable(stalker));
		scn.ShadowUseCardAction(stalker);
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.ShadowHasCardChoiceAvailable(undead, dread));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, lights));
		scn.ShadowChooseCard(undead);

		// Undead of Angmar costs 4 - 2, site 2 and not roaming.
		assertInZone(Zone.SHADOW_CHARACTERS, undead);
		assertEquals(8, scn.GetTwilight());

		// Spirit of Dread played from hand in the usual way pays its whole 7: the -2 was for that minion alone.
		scn.ShadowPlayCard(dread);
		assertInZone(Zone.SHADOW_CHARACTERS, dread);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveTheShadowAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var undead = scn.GetShadowCard("undead");

		scn.StartGame();
		scn.MoveCardsToHand(undead);
		scn.MoveMinionsToTable(stalker);
		assertInZone(Zone.HAND, undead);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, undead);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stalker));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(stalker));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWraithWraithInHandTheShadowAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker");
		var undead = scn.GetShadowCard("undead");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.AddBurdens(1);
		// A [wraith] Nazgul and a [sauron] Wraith are in hand and could be paid for; neither is a [wraith] Wraith.
		scn.MoveCardsToHand(lemenya, lights);
		scn.MoveMinionsToTable(stalker);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		assertTrue(scn.ShadowPlayAvailable(lights));
		assertFalse(scn.ShadowActionAvailable(stalker));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToHand(undead);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(stalker));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
