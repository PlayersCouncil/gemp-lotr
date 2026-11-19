package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hills", "5_16");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4, the Frodo signet; strength +1 for each race in the fellowship
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion of vitality 4 who is no Gandalf; +2 only beside 3 Ents
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, Gandalf signet, damage +1; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, Gandalf signet; a maneuver ability used only on purpose
					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer and nothing else
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text at all
					put("troop2", "1_177");
					put("slayer", "3_93");      // Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)"
				}},
				VirtualTableScenario.FellowshipSites,
				"1_289",                        // Frodo, Old Bilbo's Heir: the Ring-bearer, with the Gandalf signet
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DownFromtheHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Down From the Hills
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Exert Gandalf 3 times to make an unbound companion with the Gandalf signet strength +5, <b>defender +1</b>, <b>damage +2</b>, and unable to take wounds until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hills");

		assertEquals("Down From the Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfThreeTimesToMakeAnUnboundCompanionWithTheGandalfSignetStrongerAndUnableToTakeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hills = scn.GetFreepsCard("hills");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn);
		scn.MoveCardsToHand(hills);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(hills));
		scn.FreepsPlayCard(hills);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		// Unbound companions with the Gandalf signet, of two races and cultures; Gandalf himself has the Frodo
		// signet, and Frodo, Old Bilbo's Heir has the Gandalf signet but is the Ring-bearer, Ring-bound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		scn.FreepsChooseCard(gimli);

		// 6 printed + 5
		assertEquals(11, scn.GetStrength(gimli));
		// 1 printed + 2
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertInZone(Zone.DISCARD, hills);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The minions' archery wound is the Free Peoples player's to place, and cannot go on Gimli.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EverythingLastsThroughALaterSkirmishAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hills = scn.GetFreepsCard("hills");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, gimli, aragorn);
		scn.MoveCardsToHand(hills);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, slayer);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(hills);
		scn.FreepsChooseCard(gimli);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{gimli, troop2});
		// The Morgul Slayer is left over and unassigned, for the regroup phase.
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish first: he loses to 13 and takes his wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The later skirmish: still strength +5, damage +2 and defender +1, and Gimli, 11 against 13, loses and
		// takes no wound.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, troop2));
		assertEquals(11, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The regroup phase: all four are gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(slayer));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileGandalfCannotExertThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hills = scn.GetFreepsCard("hills");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf, avenger, gimli);
		scn.MoveCardsToHand(hills);
		// Vitality 4: with one wound he can exert twice, not three times.
		scn.AddWoundsToChar(gandalf, 1);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);

		// A [gandalf] companion of vitality 4 who is no Gandalf could exert three times, and does not count.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInZone(Zone.HAND, hills);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hills));
	}

	@Test
	public void WithNoUnboundCompanionWithTheGandalfSignetItIsPlayedAndOnlyGandalfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hills = scn.GetFreepsCard("hills");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(hills);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);

		int gandalfStrength = scn.GetStrength(gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// 3 printed + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(hills));
		scn.FreepsPlayCard(hills);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertInZone(Zone.DISCARD, hills);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
