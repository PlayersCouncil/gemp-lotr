package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("towers", "7_173");

					// Easterling Infantry: [raider] Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); his text needs 3 burdens
					put("khamul", "103_61");
					// Great Beasts: a [raider] card that is no minion (a support-area condition); a regroup ability only
					put("beasts", "7_149");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, of another culture; no text

					// Companions beside Frodo, to count over 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: strength +3 only while skirmishing a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his own strength only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarTowersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: War Towers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a [raider] minion to add a threat. Add an additional threat for each companion over 4.
		*/

		var scn = GetScenario();

		var towers = scn.GetFreepsCard("towers");

		assertEquals("War Towers", towers.getBlueprint().getTitle());
		assertNull(towers.getBlueprint().getSubtitle());
		assertFalse(towers.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, towers.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, towers.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, towers.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(towers, Timeword.MANEUVER));
		assertEquals(1, towers.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingARaiderManWithFiveCompanionsAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers = scn.GetShadowCard("towers");
		var infantry = scn.GetShadowCard("infantry");
		scn.MoveCardsToHand(towers);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		assertEquals(0, scn.GetThreats());
		assertInHand(towers);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(towers));

		scn.ShadowPlayCard(towers);

		// Frodo and four more: one threat, and one more for the fifth companion.
		assertEquals(2, scn.GetThreats());
		assertInDiscard(towers);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SpottingARaiderMinionWhoIsNoManWithSixCompanionsAddsThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers = scn.GetShadowCard("towers");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(towers);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"));

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		assertEquals(0, scn.GetThreats());
		assertInHand(towers);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(towers));

		scn.ShadowPlayCard(towers);

		// Six companions: one threat, and two more for the two companions over 4.
		assertEquals(3, scn.GetThreats());
		assertInDiscard(towers);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFourCompanionsAndAnAllyAddsOnlyTheOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers = scn.GetShadowCard("towers");
		var infantry = scn.GetShadowCard("infantry");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(towers);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"));
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, grimir);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPlayCard(towers);

		// Four companions and an ally: the ally is no companion, so nobody is over 4.
		assertEquals(1, scn.GetThreats());
		assertInDiscard(towers);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutARaiderMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var towers = scn.GetShadowCard("towers");
		var beasts = scn.GetShadowCard("beasts");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(towers);
		scn.MoveCardsToSupportArea(beasts);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInHand(towers);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [raider] card that is no minion, and a minion of another culture: neither is spotted.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(towers));
		assertEquals(0, scn.GetThreats());
		assertInHand(towers);
	}
}
