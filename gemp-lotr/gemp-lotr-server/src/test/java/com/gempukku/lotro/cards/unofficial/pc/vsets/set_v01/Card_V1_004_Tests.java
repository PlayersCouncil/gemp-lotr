package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_004_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friend", "101_4");
					put("friend2", "101_4");

					// Arwen, Daughter of Elrond: Elf companion, vitality 3.  Her bonus is against a Nazgul, and
					// there is none here.
					put("arwen", "1_30");
					// Celeborn, Lord of Lorien: Elf ally, vitality 3.  His fellowship ability is never used.
					put("celeborn", "1_34");
					put("tiredElf", "1_53");    // Lorien Elf: Elf companion, vitality 2, no text in play

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1
					// Grimir, Dwarven Elder: Dwarf ally, strength 3, no damage bonus.  His fellowship ability is
					// never used.
					put("grimir", "1_17");

					// Goblin Patrol Troop: strength 13, vitality 3, no text.  Where it fights nobody it is only here
					// so that there is a maneuver phase.
					put("troop", "1_177");
					// Goblin Runner: strength 5, vitality 1.  It adds (2) when it is played, and here it is put on
					// the table without being played.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void INameYouElfFriendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: I Name You Elf-Friend
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an Elf to make a Dwarf strength +3 and damage +1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friend");

		assertEquals("I Name You Elf-Friend", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToMakeADwarfStrengthPlusThreeAndDamagePlusOneUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friend = scn.GetFreepsCard("friend");
		var friend2 = scn.GetFreepsCard("friend2");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(friend, friend2);
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();

		// A maneuver event: with the same Elf and Dwarf in play it cannot be played in the fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(friend));

		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(friend));
		// 3 companions moved + 2 for site 2.
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// The only Elf and the only Dwarf are chosen without asking.
		scn.FreepsPlayCard(friend);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(friend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Nor can the second copy be played in the archery phase, the next in which the Free Peoples player
		// is asked for actions.  Arwen, vitality 3 with 1 wound, could exert again, and Gimli is still there.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(friend2);
		assertFalse(scn.FreepsPlayAvailable(friend2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { arwen, runner },
				new PhysicalCardImpl[] { gimli, troop }
		);
		scn.ShadowDeclineAssignments();

		// Arwen's skirmish is fought first, so that one skirmish phase has already ended when Gimli fights.
		// Her 6 beats the Goblin Runner's 5.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(gimli);
		// The last decision before the regroup phase.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// 9 against the Goblin Patrol Troop's 13: he loses and takes a wound.  At his own 6 the Troop would
		// have had double his strength and overwhelmed him.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void AnyElfWhoCanExertPaysAndAnyDwarfCanBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friend = scn.GetFreepsCard("friend");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var tiredElf = scn.GetFreepsCard("tiredElf");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(friend);
		scn.MoveCompanionsToTable(arwen, tiredElf, gimli);
		scn.MoveCardsToSupportArea(celeborn, grimir);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(tiredElf, 1);
		// Vitality 3 with 2 wounds: exhausted as well.  The Dwarf pays nothing, so that does not matter.
		scn.AddWoundsToChar(grimir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(grimir));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));

		scn.FreepsPlayCard(friend);
		// An Elf companion and an Elf ally can exert; the exhausted Elf, the Dwarves and Frodo cannot pay.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tiredElf, gimli, grimir, frodo));

		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// A Dwarf companion and a Dwarf ally can be chosen; the Elves and Frodo cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, celeborn, tiredElf, frodo));

		scn.FreepsChooseCard(grimir);
		assertEquals(6, scn.GetStrength(grimir));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		// The Dwarf who was not chosen is as he was.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friend = scn.GetFreepsCard("friend");
		var tiredElf = scn.GetFreepsCard("tiredElf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(friend);
		scn.MoveCompanionsToTable(tiredElf, gimli);
		// Vitality 2 with 1 wound: exhausted, so the Elf cannot exert.
		scn.AddWoundsToChar(tiredElf, 1);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.IsExhausted(tiredElf));
		assertInHand(friend);
		assertFalse(scn.FreepsPlayAvailable(friend));
	}
}
